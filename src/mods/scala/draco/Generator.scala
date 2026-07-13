package draco

import io.circe.{Json, parser}
import io.circe.syntax.EncoderOps
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.StoreReporter
import java.io.{File, PrintWriter}
import java.nio.file.Files

trait Generator {

}

object Generator extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Generator",
      _namePackage = Seq ("draco")
    ),
    _dracoAspect = DracoAspect (
      _factory = Factory (
        "Generator",
        _parameters = Seq (
          Parameter ("typeDictionary", "TypeDictionary", Json.Null)
        )
      )
    )
  )
  lazy val dracoType: Type[Generator] = Type[Generator] (typeDefinition)

  // --- Type loading ---

  /** Parse a JSON resource as a TypeDefinition. JSON is the normative source form
    * for type definitions at load time. */
  private def loadFromResource (resourcePath: String) : Option[TypeDefinition] = {
    val stream = getClass.getResourceAsStream(resourcePath)
    if (stream == null) return None
    val source = scala.io.Source.fromInputStream(stream)
    try {
      val text = source.mkString
      parser.parse(text).flatMap(_.as[TypeDefinition]).toOption
    } finally source.close()
  }

  private def resourcePath (typeName: TypeName, aspect: String = "") : String = {
    val np = typeName.namePackage.mkString("/")
    val n = typeName.name
    if (aspect.isEmpty) s"/$np/$n.json" else s"/$np/$n.$aspect.json"
  }

  private def tryLoad (typeName: TypeName, aspect: String = "") : Option[TypeDefinition] =
    loadFromResource(resourcePath(typeName, aspect))

  def loadType (typeName: TypeName) : TypeDefinition =
    tryLoad(typeName).getOrElse(TypeDefinition(typeName))

  def loadRuleType (typeName: TypeName) : TypeDefinition =
    tryLoad(typeName, "rule").getOrElse(TypeDefinition(typeName))

  def loadActorType (typeName: TypeName) : TypeDefinition =
    tryLoad(typeName, "actor").getOrElse(TypeDefinition(typeName))

  def loadAll (typeName: TypeName) : Seq[TypeDefinition] =
    Seq("", "rule", "actor").flatMap(aspect => tryLoad(typeName, aspect))

  /** True iff `td` itself, or any transitive ancestor via `draco.derivation`, has a
    * typeName matching `targetName`. Used to decide whether emitted factory bodies
    * must override `typeDefinition` / `dracoType` (only required when the chain
    * actually reaches the trait that declares those abstract members). */
  private def chainHits (td: TypeDefinition, targetName: String, seen: Set[String] = Set.empty) : Boolean = {
    if (seen.contains(td.typeName.name)) return false
    if (td.typeName.name == targetName) return true
    val nextSeen = seen + td.typeName.name
    td.dracoAspect.derivation.exists { tn =>
      if (tn.name == targetName) true
      else {
        try chainHits(loadType(tn), targetName, nextSeen)
        catch { case _: Throwable => false }
      }
    }
  }

  // --- Expression rendering ---

  /** Render a TypeElement `value` to its Scala surface form. A string value is
    * host-opaque source text, passed through verbatim; Json.Null is the absent
    * value (""). An object value is a structured expression tree: a single-key
    * object {op: [operands]} applying the operator symbol to its operands.
    * Operators: "." (n-ary path join), "->" (binary function/type arrow —
    * Haskell form; ScalaSource renders " => "), "()"
    * (application — first operand applied to the rest), "\" (lambda — Haskell
    * form \p1 p2 -> body: leading operands are parameters, last is the body),
    * "if" (Haskell form if c then t else e: [cond, then, else]), and the infix
    * set ("*", "==", "!=") joining operands with the operator. A string-literal
    * leaf keeps its embedded quotes ("\"Primes\"") and passes through verbatim.
    * This renderer is the ScalaSource projection: lambda renders (p1, p2) =>
    * body (bare param when single), if renders if (c) t else e.
    * Rendering is FLAT — no parenthesization; minimal parens arrive with the
    * surface fixity model (drake.dlt EXPRESSIONS), so authored trees must be
    * ones whose flat rendering reads back correctly under Scala precedence. */
  def expression (value: Json) : String = {
    if (value == null || value.isNull) ""
    else value.asString.getOrElse {
      value.asObject.map(_.toList) match {
        case Some((op, operands) :: Nil) =>
          val args = operands.asArray.getOrElse(Vector(operands)).map(expression)
          op match {
            case "."        => args.mkString(".")
            case "->"       => args.mkString(" => ")
            case "()"       => s"${args.head}(${args.tail.mkString(", ")})"
            case "\\"       =>
              val params = if (args.size == 2) args.head else args.init.mkString("(", ", ", ")")
              s"$params => ${args.last}"
            case "if"       => s"if (${args(0)}) ${args(1)} else ${args(2)}"
            case "*" | "==" | "!=" => args.mkString(s" $op ")
            case _          => sys.error(s"Generator.expression: unknown operator '$op' in ${value.noSpaces}")
          }
        case _ => sys.error(s"Generator.expression: unrenderable value ${value.noSpaces}")
      }
    }
  }

  /** Render a `value` destined for a typed slot (`name: <valueType> = <init>`).
    * An expression tree denotes surface text; when the slot's runtime type is
    * String the rendering is emitted as a string literal (e.g. a type expression
    * stored in a String-typed field). String values already carry their own
    * quoting and pass through expression() verbatim. */
  private def initializer (valueType: String, value: Json) : String = {
    val rendered = expression(value)
    if (value != null && value.isObject && valueType == "String") "\"" + rendered + "\"" else rendered
  }

  // --- Literal generation helpers ---

  private def wildcardName (name: String): String = {
    val idx = name.indexOf('[')
    if (idx < 0) name else name.substring(0, idx) + "[_]"
  }

  private def baseName (name: String): String = {
    val idx = name.indexOf('[')
    if (idx < 0) name else name.substring(0, idx)
  }

  /** Parameterized name from TypeName: "Primal[T]" or "Primal" if no typeParameters.
    * Strips `.rule`/`.actor` aspect suffix so the result is a valid Scala identifier base. */
  private def parameterizedName (tn: TypeName) : String = {
    val base = stripAspect(tn.name)
    if (tn.typeParameters.isEmpty) base
    else s"$base[${tn.typeParameters.mkString(", ")}]"
  }

  /** Wildcard name from TypeName: "Primal[_]" or "Primal" if no typeParameters.
    * Strips `.rule`/`.actor` aspect suffix. */
  private def wildcardTypeName (tn: TypeName) : String = {
    val base = stripAspect(tn.name)
    if (tn.typeParameters.isEmpty) base
    else s"$base[${tn.typeParameters.map(_ => "_").mkString(", ")}]"
  }

  private def typeNameLiteralOf (name: String, tn: TypeName, omitTypeParameters: Boolean = false) : String = {
    val args = Seq(
      Some(s""""$name""""),
      if (tn.namePackage.nonEmpty) Some(s"_namePackage = Seq (${tn.namePackage.map(s => s""""$s"""").mkString(", ")})") else None,
      if (!omitTypeParameters && tn.typeParameters.nonEmpty) Some(s"_typeParameters = Seq (${tn.typeParameters.map(s => s""""$s"""").mkString(", ")})") else None
    ).flatten
    s"TypeName (${args.mkString(", ")})"
  }

  private def typeNameLiteral (tn: TypeName) : String =
    typeNameLiteralOf(tn.name, tn)

  /** TypeName literal for use in a `Generator.loadType(...)` call. Omits
    * typeParameters because resource resolution uses only name + namePackage,
    * and parametric types resolve their parameters from the loaded JSON. */
  private def typeNameLiteralForLoad (name: String, tn: TypeName) : String =
    typeNameLiteralOf(name, tn, omitTypeParameters = true)

  private def typeDefinitionLoad (td: TypeDefinition) : String = {
    s"""Generator.loadType(${typeNameLiteralForLoad(td.typeName.name, td.typeName)})"""
  }

  /** Emit `loadRuleType(TypeName(baseName, ...))` — the `.rule` aspect is added back
    * by `loadRuleType` when resolving the JSON resource. */
  private def ruleDefinitionLoad (td: TypeDefinition) : String = {
    s"""Generator.loadRuleType(${typeNameLiteralForLoad(stripAspect(td.typeName.name), td.typeName)})"""
  }



  // --- Rule generation helpers ---

  private def factVariables (
    _variables: Seq[Variable]
  ) : String = {
    if (_variables.isEmpty) ""
    else _variables.map { v =>
      s"""      "$$${v.name}", classOf[${v.valueType}]"""
    }.mkString(",\n")
  }
  private def conditionFunctions (
    _conditions: Seq[Condition]
  ) : String = {
    if (_conditions.isEmpty) ""
    else _conditions.zipWithIndex.map { case (c, idx) =>
      val params = c.parameters.map(p => s"${p.name}: ${p.valueType}").mkString(", ")
      s"  def w$idx($params): Boolean = ${expression(c.value)}"
    }.mkString("\n")
  }

  private def whereConditions (
    conditions: Seq[Condition],
    qualifiedObjectName: String
  ) : String = {
    if (conditions.isEmpty) ""
    else conditions.zipWithIndex.map { case (c, idx) =>
      // Reference the condition function with fully qualified class name and $-prefixed parameters
      val params = c.parameters.map(p => s"$$${p.name}").mkString(", ")
      s"""    .where("$qualifiedObjectName.w$idx($params)")"""
    }.mkString("\n")
  }

  private def actionBody (
     action: Action,
     variables: Seq[Variable],
     values: Seq[Value]
  ) : String = {
    // Extract rule variables from context
    val varBindings = variables.map { v =>
      s"""      val ${v.name}: ${v.valueType} = ctx.get[${v.valueType}]("$$${v.name}")"""
    }.mkString("\n")

    // Generate body statements from Action.body
    val bodyStatements = action.body.map {
      case f: Fixed =>
        if (f.name.nonEmpty) s"      val ${f.name}: ${f.valueType} = ${initializer(f.valueType, f.value)}"
        else s"      ${expression(f.value)}"
      case m: Mutable =>
        if (m.name.nonEmpty) s"      var ${m.name}: ${m.valueType} = ${initializer(m.valueType, m.value)}"
        else s"      ${expression(m.value)}"
      case d: Dynamic =>
        if (d.name.nonEmpty) s"      def ${d.name}: ${d.valueType} = ${initializer(d.valueType, d.value)}"
        else s"      ${expression(d.value)}"
      case v: Variable =>
        s"      val ${v.name}: ${v.valueType} = ctx.get[${v.valueType}](\"$$${v.name}\")"
      case te: BodyElement =>
        s"      ${expression(te.value)}"
    }.mkString("\n")

    if (bodyStatements.isEmpty) varBindings
    else s"$varBindings\n$bodyStatements"
  }

  // --- Type generation helpers ---

  private def typeModifier (
    modules: Seq[TypeName]
  ) : String = {
    if (modules.isEmpty) "" else "sealed "
  }

  private def typeExtends (
    td: TypeDefinition
  ) : String = {
    val ext = td.dracoAspect.extensible
    val derivation = td.dracoAspect.derivation
    if (ext.name.nonEmpty) {
      val head = parameterizedName(ext)
      if (derivation.isEmpty) s"extends $head"
      else s"extends $head with ${derivation.map(parameterizedName).mkString(" with ")}"
    } else if (derivation.nonEmpty) {
      val head = parameterizedName(derivation.head)
      val tail = derivation.tail.map(parameterizedName)
      if (tail.isEmpty) s"extends $head"
      else s"extends $head with ${tail.mkString(" with ")}"
    } else ""
  }

  private def methodParameters (
    parameters: Seq[Parameter]
  ) : String = {
    if (parameters.isEmpty) ""
    else {
      val params = parameters.map { p =>
        val d = initializer(p.valueType, p.value)
        val default = if (d.isEmpty) "" else s" = $d"
        s"${p.name}: ${p.valueType}$default"
      }
      s"(${params.mkString(", ")})"
    }
  }

  /** Emit a method body. `body` holds only STATEMENTS (Local/Fixed bindings,
    * Monadic effects); `value` is the method's RESULT expression — empty for
    * Unit methods. `methodIndent` is the column where the `def` itself
    * starts; body lines indent two further, and the closing brace aligns
    * with the `def`. */
  private def methodBody (
    body: Seq[BodyElement],
    value: String,
    methodIndent: Int = 2
  ) : String = {
    if (body.isEmpty && value.isEmpty) "???"
    else if (body.isEmpty) value
    else if (body.size == 1 && value.isEmpty) {
      // Single statement - just its value (a Unit method's lone effect)
      val v = expression(body.head.value)
      if (v.isEmpty) "???" else v
    } else {
      val bodyPad  = " " * (methodIndent + 2)
      val bracePad = " " * methodIndent
      // Prefix every non-empty line of `v` with bodyPad. Lets multi-line
      // Monadic values (e.g. `if/else` blocks) align correctly inside the
      // method body — caller's value supplies relative indent, this adds
      // the absolute body-indent prefix.
      def indentBlock(v: String): String =
        v.linesIterator
          .map(line => if (line.isEmpty) "" else s"$bodyPad$line")
          .mkString("\n")
      val statements = body.map {
        case f: Fixed    => s"${bodyPad}val ${f.name}: ${f.valueType} = ${initializer(f.valueType, f.value)}"
        case m: Mutable  => s"${bodyPad}var ${m.name}: ${m.valueType} = ${initializer(m.valueType, m.value)}"
        case l: Local    => s"${bodyPad}val ${l.name}: ${l.valueType} = ${initializer(l.valueType, l.value)}"
        case mo: Monadic => indentBlock(expression(mo.value))
        case be: BodyElement => s"${bodyPad}val ${be.name}: ${be.valueType} = ${initializer(be.valueType, be.value)}"
      }
      val result = if (value.isEmpty) Seq.empty else Seq(indentBlock(value))
      s"{\n${(statements ++ result).mkString("\n")}\n$bracePad}"
    }
  }

  private def typeBody (
    elements: Seq[TypeElement]
  ) : String = {
    if (elements.isEmpty) ""
    else {
      val members = elements.map {
        case f: Fixed =>
          // `lazy val` (not plain `val`) when a default is present so subtypes
          // can override with `lazy val` — concrete non-lazy vals cannot be
          // overridden by `lazy val`.
          val init = initializer(f.valueType, f.value)
          if (init.nonEmpty) s"  lazy val ${f.name}: ${f.valueType} = $init"
          else s"  val ${f.name}: ${f.valueType}"
        case m: Mutable =>
          val init = initializer(m.valueType, m.value)
          if (init.nonEmpty) s"  var ${m.name}: ${m.valueType} = $init"
          else s"  var ${m.name}: ${m.valueType}"
        case d: Dynamic =>
          val result = expression(d.value)
          if (d.body.nonEmpty || result.nonEmpty) s"  def ${d.name}${methodParameters(d.parameters)}: ${d.valueType} = ${methodBody(d.body, result)}"
          else s"  def ${d.name}${methodParameters(d.parameters)}: ${d.valueType}"
        case mo: Monadic =>
          // Verbatim Scala source — for declarations that exceed the
          // Fixed/Mutable/Dynamic vocabulary (method type params, implicit
          // parameter lists, multi-line bodies, etc.). Each line indented
          // by the trait body's two spaces.
          expression(mo.value).linesIterator.map(l => s"  $l").mkString("\n")
        case p: Parameter => s"  val ${p.name}: ${p.valueType}"
        case te: TypeElement => s"  val ${te.name}: ${te.valueType}"
      }
      s"{\n${members.mkString("\n")}\n}"
    }
  }

  private def factoryParameters (
    parameters: Seq[Parameter]
  ) : String = {
    if (parameters.isEmpty) ""
    else {
      val params = parameters.map { p =>
        val d = initializer(p.valueType, p.value)
        val default = if (d.isEmpty) "" else s" = $d"
        s"_${p.name}: ${p.valueType}$default"
      }
      s"\n    ${params.mkString(",\n    ")}\n  "
    }
  }

  private def factoryBody (
    td: TypeDefinition
  ) : String = {
    val factory = td.dracoAspect.factory
    val objName = baseName(factory.valueType)
    val hasTypeDefinitionOverride =
      if (factory.body.nonEmpty) factory.body.exists(_.name == "typeDefinition")
      else factory.parameters.exists(_.name == "typeDefinition")
    val instanceOverrides = Seq(
      if (chainHits(td, "DracoType") && !hasTypeDefinitionOverride)
        Some(s"    override lazy val typeDefinition: TypeDefinition = $objName.typeDefinition")
      else None
    ).flatten
    val overrides =
      if (factory.body.nonEmpty) factory.body.map {
        case f: Fixed   => s"    override lazy val ${f.name}: ${f.valueType} = ${initializer(f.valueType, f.value)}"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = ${initializer(m.valueType, m.value)}"
        case d: Dynamic => s"    override def ${d.name}${methodParameters(d.parameters)}: ${d.valueType} = ${methodBody(d.body, expression(d.value), methodIndent = 4)}"
        case mo: Monadic => s"    ${expression(mo.value)}"
        case l: Local   => s"    val ${l.name}: ${l.valueType} = ${initializer(l.valueType, l.value)}"
        case be: BodyElement => s"    override lazy val ${be.name}: ${be.valueType} = ${initializer(be.valueType, be.value)}"
      }
      else factory.parameters.map { p =>
        s"    override lazy val ${p.name}: ${p.valueType} = _${p.name}"
      }
    s"{\n${(overrides ++ instanceOverrides).mkString("\n")}\n  }"
  }

  /** Check if globalElements contain an explicit main method (Dynamic named "main").
    * When present, the companion omits App and lets globalElementsDeclaration emit def main. */
  private def hasExplicitMain (globalElements: Seq[BodyElement]) : Boolean =
    globalElements.exists { case d: Dynamic => d.name == "main"; case _ => false }

  private def globalElementsDeclaration (
    globalElements: Seq[BodyElement]
  ) : String = {
    if (globalElements.isEmpty) ""
    else {
      val globals = globalElements.map {
        case f: Fixed =>
          val rendered = initializer(f.valueType, f.value)
          val init = if (rendered.isEmpty) s"null.asInstanceOf[${f.valueType}]" else rendered
          s"  lazy val ${f.name}: ${f.valueType} = $init"
        case m: Mutable =>
          val rendered = initializer(m.valueType, m.value)
          val init = if (rendered.isEmpty) s"null.asInstanceOf[${m.valueType}]" else rendered
          s"  var ${m.name}: ${m.valueType} = $init"
        case d: Dynamic =>
          s"  def ${d.name}${methodParameters(d.parameters)}: ${d.valueType} = ${methodBody(d.body, expression(d.value))}"
        case mo: Monadic =>
          // Indent every line so multi-line global blocks (encoder/decoder/etc.)
          // emit at the correct object-body indent level.
          expression(mo.value).linesIterator
            .map(line => if (line.isEmpty) "" else s"  $line")
            .mkString("\n")
        case be: BodyElement =>
          val rendered = initializer(be.valueType, be.value)
          val init = if (rendered.isEmpty) s"null.asInstanceOf[${be.valueType}]" else rendered
          s"  val ${be.name}: ${be.valueType} = $init"
      }
      globals.mkString("\n")
    }
  }

  /** True if the value type denotes a function-like value (no circe codec). */
  private def isFunctionLikeType (valueType: String) : Boolean = {
    val t = valueType.trim
    t.contains("=>") ||
      t.startsWith("Consumer[") ||
      t.startsWith("Function[") ||
      t.startsWith("Function0[") ||
      t.startsWith("Function1[") ||
      t.startsWith("Function2[") ||
      t.startsWith("Behavior[")
  }

  private def nullValueFor (valueType: String, defaultValue: String) : String = {
    if (defaultValue.nonEmpty) defaultValue
    else valueType match {
      case "String"                   => "\"\""
      case s if s.startsWith("Seq[")  => "Seq.empty"
      case s if s.startsWith("Map[")  => "Map.empty"
      case "Int" | "Long"             => "0"
      case "Double" | "Float"         => "0.0"
      case "Boolean"                  => "false"
      case "Json"                     => "Json.Null"
      case _                          => s"null.asInstanceOf[$valueType]"
    }
  }

  // --- Codec generation helpers ---

  private def elisionCheck (p: Parameter) : Option[String] = {
    if (expression(p.value).isEmpty) None  // required field — always encode
    else p.valueType match {
      case "String"                    => Some(s"x.${p.name}.nonEmpty")
      case s if s.startsWith("Seq[")   => Some(s"x.${p.name}.nonEmpty")
      case s if s.startsWith("Map[")   => Some(s"x.${p.name}.nonEmpty")
      case "Json"                      => Some(jsonNonEmpty(p.name))
      case "Factory"                   => Some(s"x.${p.name}.valueType.nonEmpty")
      case "Action"                    => Some(s"x.${p.name}.body.nonEmpty")
      case "Pattern"                   => Some(s"x.${p.name}.variables.nonEmpty")
      case "TypeName"                  => Some(s"x.${p.name}.name.nonEmpty")
      case s if s.endsWith("Aspect")   => Some(s"!$s.isEmpty(x.${p.name})")
      case _                           => None  // no natural emptiness — always encode
    }
  }

  /** Wire-emptiness test for a Json-typed field: absent (Json.Null) or a
    * present-empty string value elides, everything else — including an
    * expression tree — encodes. Mirrors the String field's `.nonEmpty`. */
  private def jsonNonEmpty (field: String) : String =
    s"!x.$field.isNull && x.$field.asString.forall(_.nonEmpty)"

  /** Default wire-elision test for a field with a natural emptiness, used in
    * discriminated unions when `elisionCheck` (default-bearing params) does
    * not apply. */
  private def defaultElision (p: Parameter) : Option[String] = p.valueType match {
    case "String"                  => Some(s"x.${p.name}.nonEmpty")
    case s if s.startsWith("Seq[") => Some(s"x.${p.name}.nonEmpty")
    case s if s.startsWith("Map[") => Some(s"x.${p.name}.nonEmpty")
    case "Json"                    => Some(jsonNonEmpty(p.name))
    case _                         => None
  }

  /** Find the root discriminated union parent by walking up the derivation chain.
    * Returns the topmost ancestor that has modules (e.g., TypeElement, not BodyElement). */
  private def findDiscriminatedParent (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition]
  ) : Option[String] = {
    td.dracoAspect.derivation.map(tn => baseName(tn.name)).flatMap { name =>
      familyMap.get(name) match {
        case Some(parentTd) if parentTd.dracoAspect.modules.nonEmpty =>
          // Parent has modules — but check if it in turn derives from a higher discriminated parent
          findDiscriminatedParent(parentTd, familyMap).orElse(Some(name))
        case Some(parentTd) =>
          // Parent has no modules — but its parent might
          findDiscriminatedParent(parentTd, familyMap)
        case None => None
      }
    }.headOption
  }

  private def collectLeafModules (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition]
  ) : Seq[String] = {
    td.dracoAspect.modules.flatMap { moduleTn =>
      val name = baseName(moduleTn.name)
      familyMap.get(name) match {
        case Some(moduleTd) if moduleTd.dracoAspect.modules.nonEmpty =>
          // Intermediate sealed trait — recurse
          collectLeafModules(moduleTd, familyMap)
        case _ =>
          // Leaf (concrete) or not in familyMap
          Seq(name)
      }
    }
  }

  /** Element names a type inherits by composition — walk `derivation` transitively
    * and union each ancestor's own elements. Lets the simple-codec gate treat a
    * factory param backed by an inherited field (e.g. TypeDefinition's aspects,
    * declared on its `Aspects` parent) as accessible, without re-listing it on the
    * child. Ancestors are read from `familyMap` when present, else loaded from the
    * classpath; a not-found parent contributes nothing (empty elements). */
  private def inheritedElementNames (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition],
    seen: Set[String] = Set.empty
  ) : Set[String] = {
    td.dracoAspect.derivation.flatMap { tn =>
      val name = baseName(tn.name)
      if (seen.contains(name)) Set.empty[String]
      else {
        val parentTd = familyMap.getOrElse(name, loadType(tn))
        parentTd.dracoAspect.elements.map(_.name).toSet ++
          inheritedElementNames(parentTd, familyMap, seen + name)
      }
    }.toSet
  }

  private def fieldElisionEncoder (params: Seq[Parameter]) : String = {
    val fields = params.map { p =>
      elisionCheck(p) match {
        case Some(check) =>
          s"""      if ($check) Some("${p.name}" -> x.${p.name}.asJson) else None"""
        case None =>
          s"""      Some("${p.name}" -> x.${p.name}.asJson)"""
      }
    }
    s"""Encoder.instance { x =>
       |    val fields = Seq(
       |${fields.mkString(",\n")}
       |    ).flatten
       |    Json.obj(fields: _*)
       |  }""".stripMargin
  }

  /** Emit a decoder `for` line for one parameter. Mirrors the encoder's elision
    * rules: String/Seq/Map fields are always tolerated when missing (encoder
    * elides them when empty), defaulting to the type's "zero". Non-elidable
    * types use the parameter's explicit default if present, else strict-require. */
  private def decoderForLine (p: Parameter, indent: String) : String = {
    val typeZero: Option[String] = p.valueType match {
      case "String"                    => Some("\"\"")
      case s if s.startsWith("Seq[")   => Some("Seq.empty")
      case s if s.startsWith("Map[")   => Some("Map.empty")
      case "Json"                      => Some("Json.Null")
      case _                           => None
    }
    val explicitDefault = initializer(p.valueType, p.value)
    val defaultOpt: Option[String] = typeZero.orElse(if (explicitDefault.nonEmpty) Some(explicitDefault) else None)
    defaultOpt match {
      case Some(d) =>
        s"""${indent}_${p.name} <- cursor.downField("${p.name}").as[Option[${p.valueType}]].map(_.getOrElse($d))"""
      case None =>
        s"""${indent}_${p.name} <- cursor.downField("${p.name}").as[${p.valueType}]"""
    }
  }

  private def fieldElisionDecoder (params: Seq[Parameter], typeName: String) : String = {
    val objName = baseName(typeName)
    val forLines = params.map(p => decoderForLine(p, "      "))
    val yieldArgs = params.map(p => s"_${p.name}").mkString(", ")
    s"""Decoder.instance { cursor =>
       |    for {
       |${forLines.mkString("\n")}
       |    } yield $objName ($yieldArgs)
       |  }""".stripMargin
  }

  private def simpleCodecDeclaration (td: TypeDefinition) : String = {
    val name = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    val params = td.dracoAspect.factory.parameters
    s"""  implicit lazy val encoder: Encoder[$wName] = ${fieldElisionEncoder(params)}
       |  implicit lazy val decoder: Decoder[$wName] = ${fieldElisionDecoder(params, name)}""".stripMargin
  }

  private def discriminatedCodecDeclaration (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition]
  ) : String = {
    val name = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    val leaves = collectLeafModules(td, familyMap)
    // The wire key for the type tag: authored via CodecAspect.discriminator,
    // defaulting to "kind" when unset so existing emission is unchanged.
    val discriminator = if (td.codecAspect.discriminator.nonEmpty) td.codecAspect.discriminator else "kind"

    // Encoder: pattern match on each leaf subtype.
    // The parent trait is sealed (discriminatedCodecDeclaration only runs when
    // modules.nonEmpty, which forces `sealed`), so the leaf cases are
    // exhaustive — no parent fallback case is emitted (would be unreachable).
    val matchArms = leaves.map { leaf =>
      s"""      case _: $leaf => "$leaf""""
    }.mkString("\n")

    // For each leaf, build encoder fields from its factory parameters
    val encoderBody =
      s"""  implicit lazy val encoder: Encoder[$wName] = Encoder.instance { x =>
         |    val kind = x match {
         |$matchArms
         |    }
         |    val fields = Seq(
         |      Some("$discriminator" -> Json.fromString(kind)),
         |${encoderFieldLines(td, leaves, familyMap)}
         |    ).flatten${subtypeExtraFields(td, leaves, familyMap)}
         |    Json.obj(fields: _*)
         |  }""".stripMargin

    // Decoder: dispatch on kind field
    val decoderCases = leaves.map { leaf =>
      val leafTd = familyMap.get(leaf)
      val params = leafTd.map(_.dracoAspect.factory.parameters).getOrElse(Seq.empty)
      val forLines = params.map(p => decoderForLine(p, "          "))
      val yieldArgs = params.map(p => s"_${p.name}").mkString(", ")
      if (params.isEmpty) {
        s"""      case "$leaf" =>
           |        Right($leaf.Null)""".stripMargin
      } else {
        s"""      case "$leaf" =>
           |        for {
           |${forLines.mkString("\n")}
           |        } yield $leaf ($yieldArgs)""".stripMargin
      }
    }.mkString("\n\n")

    // Fallback case: decode as parent type with base fields
    val fallbackParams = td.dracoAspect.factory.parameters
    val fallbackForLines = fallbackParams.map(p => decoderForLine(p, "          "))
    val fallbackYieldArgs = fallbackParams.map(p => s"_${p.name}").mkString(", ")
    val fallbackCase = if (fallbackParams.isEmpty) {
      s"""      case other =>
         |        Left(io.circe.DecodingFailure(s"Unknown $name $discriminator: $$other", cursor.history))""".stripMargin
    } else {
      s"""      case _ =>
         |        for {
         |${fallbackForLines.mkString("\n")}
         |        } yield $name ($fallbackYieldArgs)""".stripMargin
    }

    val decoderBody =
      s"""  implicit lazy val decoder: Decoder[$wName] = Decoder.instance { cursor =>
         |    cursor.downField("$discriminator").as[String].flatMap {
         |$decoderCases
         |
         |$fallbackCase
         |    }
         |  }""".stripMargin

    s"""$encoderBody
       |$decoderBody""".stripMargin
  }

  /** Build the shared encoder field lines for a discriminated union.
    * Only includes fields accessible on the parent trait (elements + factory params).
    * Subtype-specific fields (like Pattern.variables) are handled by the parent's
    * accessor if declared in elements, or omitted (decoded per-case in the decoder). */
  private def encoderFieldLines (
    parentTd: TypeDefinition,
    leaves: Seq[String],
    familyMap: Map[String, TypeDefinition]
  ) : String = {
    // Fields accessible on the parent: its elements and factory parameters
    val parentFieldNames = (parentTd.dracoAspect.elements.map(_.name) ++ parentTd.dracoAspect.factory.parameters.map(_.name)).distinct

    // Find representative Parameter for each parent field (from factory params or synthesize from elements)
    val parentParams: Seq[Parameter] = parentFieldNames.flatMap { name =>
      parentTd.dracoAspect.factory.parameters.find(_.name == name).orElse {
        parentTd.dracoAspect.elements.find(_.name == name).map(e => Parameter(e.name, e.valueType, Json.Null))
      }
    }

    parentParams.map { p =>
      // For discriminated union fields, defaultElision supplies the natural
      // emptiness test (String/Seq/Map .nonEmpty, Json null-or-empty-string)
      // when elisionCheck (default-bearing params) does not apply.
      elisionCheck(p).orElse(defaultElision(p)) match {
        case Some(check) =>
          s"""      if ($check) Some("${p.name}" -> x.${p.name}.asJson) else None"""
        case None =>
          s"""      Some("${p.name}" -> x.${p.name}.asJson)"""
      }
    }.mkString(",\n")
  }

  /** Per-subtype encoder fields for a discriminated union: each leaf's factory
    * params that are NOT declared on the parent trait (e.g. Pattern.variables/
    * conditions, Action.variables/values). Without these the shared parent encoder
    * silently drops subtype-only fields that the decoder reads back, breaking
    * round-trip. Returns "" when no leaf carries extra fields. */
  private def subtypeExtraFields (
    td: TypeDefinition,
    leaves: Seq[String],
    familyMap: Map[String, TypeDefinition]
  ) : String = {
    val parentFieldNames = (td.dracoAspect.elements.map(_.name) ++ td.dracoAspect.factory.parameters.map(_.name)).distinct
    val arms = leaves.flatMap { leaf =>
      val params = familyMap.get(leaf).map(_.dracoAspect.factory.parameters).getOrElse(Seq.empty)
      val extras = params.filterNot(p => parentFieldNames.contains(p.name))
      if (extras.isEmpty) None
      else {
        val lines = extras.map { p =>
          elisionCheck(p).orElse(defaultElision(p)) match {
            case Some(check) => s"""        if ($check) Some("${p.name}" -> x.${p.name}.asJson) else None"""
            case None        => s"""        Some("${p.name}" -> x.${p.name}.asJson)"""
          }
        }.mkString(",\n")
        Some(s"      case x: $leaf => Seq(\n$lines\n      ).flatten")
      }
    }
    if (arms.isEmpty) ""
    else s" ++ (x match {\n${arms.mkString("\n")}\n      case _ => Seq.empty\n    })"
  }

  private def subtypeCodecDeclaration (td: TypeDefinition, parentName: String) : String = {
    val name = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    s"""  private lazy val codec = Codec.sub[$parentName, $wName]($parentName.encoder, $parentName.decoder)
       |  implicit def encoder: Encoder[$wName] = codec.encoder
       |  implicit def decoder: Decoder[$wName] = codec.decoder""".stripMargin
  }

  private def codecDeclaration (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty) : String = {
    val familyMap = familyContext.map(t => baseName(t.typeName.name) -> t).toMap

    // Check for discriminated parent first — intermediate sealed traits (like BodyElement)
    // that derive from a discriminated parent get Codec.sub, not their own discriminated union
    findDiscriminatedParent(td, familyMap) match {
      case Some(parentName) =>
        // Pattern 3: Codec.sub wiring (includes intermediate sealed traits)
        subtypeCodecDeclaration(td, parentName)
      case None =>
        if (td.dracoAspect.modules.nonEmpty) {
          // Pattern 2: discriminated union (top-level sealed trait)
          discriminatedCodecDeclaration(td, familyMap)
        } else if (td.dracoAspect.factory.valueType.nonEmpty && td.dracoAspect.factory.parameters.nonEmpty) {
          // Pattern 1: simple field-based — only when the type declares at least one
          // element of its own AND every factory param is accessible as a trait element
          // (own or inherited via derivation) AND no param has a function-like type
          // (those have no circe codec). The own-element requirement keeps a pure
          // inheritance wrapper (every param delegated to a parent's field — e.g.
          // Meters/Radians on Distance/Rotation, Type on DracoType) codec-less, while a
          // record that composes inherited fields onto its own (TypeDefinition: typeName
          // + the Aspects) derives its codec.
          val ownElementNames = td.dracoAspect.elements.map(_.name).toSet
          val elementNames = ownElementNames ++ inheritedElementNames(td, familyMap)
          val paramNames = td.dracoAspect.factory.parameters.map(_.name).toSet
          val anyFunctionLike = td.dracoAspect.factory.parameters.exists(p => isFunctionLikeType(p.valueType))
          if (ownElementNames.nonEmpty && paramNames.subsetOf(elementNames) && !anyFunctionLike) simpleCodecDeclaration(td)
          else ""
        } else {
          // No codec (abstract type)
          ""
        }
    }
  }

  private def nullInstance (
    typeName: TypeName,
    elements: Seq[TypeElement],
    factory: Factory,
    nameSuffix: String = ""
  ) : String = {
    val name = typeName.name + nameSuffix
    val wName = wildcardTypeName(typeName) + nameSuffix
    val typeParams = if (typeName.typeParameters.isEmpty) "" else s"[${typeName.typeParameters.map(_ => "Nothing").mkString(", ")}]"
    if (factory.valueType.nonEmpty) {
      val allHaveDefaults = factory.parameters.nonEmpty && factory.parameters.forall(p => expression(p.value).nonEmpty)
      if (allHaveDefaults) {
        // All parameters carry default values — emit a bare `apply()` and let
        // Scala's default-argument resolution provide each one.
        s"lazy val Null: $wName = apply$typeParams()"
      } else {
        val nullArgs = factory.parameters.map { p =>
          s"    _${p.name} = ${nullValueFor(p.valueType, initializer(p.valueType, p.value))}"
        }
        if (nullArgs.isEmpty) s"lazy val Null: $wName = apply$typeParams()"
        else s"lazy val Null: $wName = apply$typeParams(\n${nullArgs.mkString(",\n")}\n  )"
      }
    } else if (elements.isEmpty) {
      s"lazy val Null: $wName = new $wName {}"
    } else {
      val nullMembers = elements.map {
        case f: Fixed => s"    override val ${f.name}: ${f.valueType} = null.asInstanceOf[${f.valueType}]"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = null.asInstanceOf[${m.valueType}]"
        case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = null.asInstanceOf[${d.valueType}]"
        case p: Parameter => s"    override val ${p.name}: ${p.valueType} = null.asInstanceOf[${p.valueType}]"
        case te: TypeElement => s"    override val ${te.name}: ${te.valueType} = null.asInstanceOf[${te.valueType}]"
      }
      s"lazy val Null: $wName = new $wName {\n${nullMembers.mkString("\n")}\n  }"
    }
  }

  // --- Companion object generation ---

  private def typeGlobal (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty, nameSuffix: String = "") : String = {
    val factory = td.dracoAspect.factory
    val hasFactory = factory.valueType.nonEmpty
    val hasGlobalElements = td.dracoAspect.globalElements.nonEmpty
    val objName = stripAspect(td.typeName.name) + nameSuffix
    val wName = wildcardTypeName(td.typeName) + nameSuffix
    val typeParams = if (td.typeName.typeParameters.isEmpty) "" else s"[${td.typeName.typeParameters.mkString(", ")}]"

    val parents = Seq(
      if (hasExplicitMain(td.dracoAspect.globalElements)) None else Some("App"),
      if (chainHits(td, "DracoType")) Some("DracoType") else None
    ).flatten
    val header = if (parents.isEmpty) s"object $objName" else s"object $objName extends ${parents.mkString(" with ")}"
    val tdOverride = if (chainHits(td, "DracoType")) "override " else ""
    val tdLiteral = s"  ${tdOverride}lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}"
    val tiLiteral = s"  lazy val dracoType: Type[$wName] = Type[$wName] (typeDefinition)"
    val container = containerName(td)
    val dtBlock = if (container.nonEmpty) s"\n${domainTypeLiteral(container)}" else ""
    val codec = codecDeclaration(td, familyContext)
    val codecBlock = if (codec.nonEmpty) s"\n$codec\n" else ""

    if (!hasFactory && !hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral$dtBlock
         |$codecBlock}""".stripMargin
    } else if (!hasFactory && hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral$dtBlock
         |$codecBlock
         |${globalElementsDeclaration(td.dracoAspect.globalElements)}
         |}""".stripMargin
    } else {
      s"""$header {
         |$tdLiteral
         |$tiLiteral$dtBlock
         |$codecBlock
         |  def apply$typeParams (${factoryParameters(factory.parameters)}) : ${factory.valueType} = new ${factory.valueType} ${factoryBody(td)}
         |
         |  ${nullInstance(td.typeName, td.dracoAspect.elements, td.dracoAspect.factory, nameSuffix)}
         |
         |${globalElementsDeclaration(td.dracoAspect.globalElements)}
         |}""".stripMargin
    }
  }

  // --- Object-only companion generation (no trait, extends DracoType) ---

  private def objectGlobal (td: TypeDefinition) : String = {
    val objName = td.typeName.name
    val container = containerName(td)
    val dtBlock = if (container.nonEmpty) s"\n  ${domainTypeLiteral(container).trim}" else ""
    s"""object $objName extends DracoType {
       |  override lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}
       |  lazy val dracoType: DracoType = this$dtBlock
       |
       |${globalElementsDeclaration(td.dracoAspect.globalElements)}
       |}""".stripMargin
  }

  // --- Trait declaration helper ---

  private def traitDeclaration (td: TypeDefinition, nameSuffix: String = "") : String = {
    val ext  = typeExtends(td)
    val body = typeBody(td.dracoAspect.elements)
    val extPart  = if (ext.nonEmpty)  s" $ext"  else ""
    val bodyPart = if (body.nonEmpty) s" $body" else ""
    s"${typeModifier(td.dracoAspect.modules)}trait ${parameterizedName(td.typeName)}$nameSuffix$extPart$bodyPart"
  }

  // --- Domain companion generation ---

  /** Emit `lazy val domainType: Domain[<name>] = Domain[<name>] (typeDefinition)`.
    * For a self-domain pass the type's own object name; for a leaf pass its
    * containing domain's simple name (resolved via `domainAspect.typeName`). */
  private def domainTypeLiteral (containerName: String) : String =
    s"  lazy val domainType: Domain[$containerName] = Domain[$containerName] (typeDefinition)"

  /** Container name to use when emitting `domainType` for a non-domain (leaf) type.
    * Returns the simple Scala name from `domainAspect.typeName`, or empty if not set
    * (in which case the caller should skip emission to permit partial migration). */
  private def containerName (td: TypeDefinition) : String = {
    val tn = td.domainAspect.typeName
    if (tn != null && tn.name.nonEmpty) stripAspect(tn.name) else ""
  }

  /** Emit a Scala-visible mirror of the domain's elementTypeNames list. JSON remains
    * the runtime authority (via `typeDefinition.elementTypeNames`); this val is for
    * developer readability — see at-a-glance what's in the domain without opening JSON. */
  private def elementTypeNamesLiteral (td: TypeDefinition) : String = {
    val items = td.domainAspect.elementTypeNames.map(n => s""""$n"""").mkString(", ")
    s"  lazy val elementTypeNames: Seq[String] = Seq ($items)"
  }

  private def domainGlobal (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty) : String = {
    val objName = td.typeName.name
    val hasGlobalElements = td.dracoAspect.globalElements.nonEmpty
    val parents = Seq(
      if (hasExplicitMain(td.dracoAspect.globalElements)) None else Some("App"),
      if (chainHits(td, "DracoType")) Some("DracoType") else None
    ).flatten
    val header = if (parents.isEmpty) s"object $objName" else s"object $objName extends ${parents.mkString(" with ")}"
    val tdOverride = if (chainHits(td, "DracoType")) "override " else ""
    val tdLiteral = s"  ${tdOverride}lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}"
    val etnLiteral = elementTypeNamesLiteral(td)
    val codec = codecDeclaration(td, familyContext)
    val codecBlock = if (codec.nonEmpty) s"\n$codec\n" else ""
    val wName = wildcardTypeName(td.typeName)
    val dtLiteral = domainTypeLiteral(wName)
    val tiLiteral = s"  lazy val dracoType: Type[$wName] = Type[$wName] (typeDefinition)"
    val globals = if (hasGlobalElements) s"\n${globalElementsDeclaration(td.dracoAspect.globalElements)}" else ""

    s"""$header {
       |$tdLiteral
       |$tiLiteral
       |
       |$etnLiteral
       |$codecBlock
       |$dtLiteral$globals
       |}""".stripMargin
  }

  // --- Rule companion generation ---

  private def ruleGlobal (td: TypeDefinition) : String = {
    val name = stripAspect(td.typeName.name) + "Rule"
    val parents = Seq(
      if (hasExplicitMain(td.dracoAspect.globalElements)) None else Some("App"),
      if (chainHits(td, "DracoType")) Some("DracoType") else None
    ).flatten
    val header = if (parents.isEmpty) s"object $name" else s"object $name extends ${parents.mkString(" with ")}"
    val tdOverride = if (chainHits(td, "DracoType")) "override " else ""
    val tiLiteral = s"  lazy val dracoType: Type[$name] = Type[$name] (typeDefinition)"
    val container = containerName(td)
    val dtBlock = if (container.nonEmpty) s"\n${domainTypeLiteral(container)}" else ""
    s"""$header {
       |  ${tdOverride}lazy val typeDefinition: TypeDefinition = ${ruleDefinitionLoad(td)}
       |$tiLiteral$dtBlock
       |${conditionFunctions(td.ruleAspect.pattern.conditions)}
       |  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
       |${actionBody(td.ruleAspect.action, td.ruleAspect.pattern.variables, td.ruleAspect.values)}
       |  }
       |
       |  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
       |    knowledge
       |    .builder()
       |    .newRule ("${td.typeName.namePath}")
       |    .forEach (
       |${factVariables(td.ruleAspect.pattern.variables)}
       |    )
       |${whereConditions(td.ruleAspect.pattern.conditions, stripAspect(td.typeName.namePath) + "Rule")}
       |    .execute (action)
       |    .build()
       |  }
       |
       |  lazy val ruleType: RuleType = Rule[$name] (
       |    _pattern = pattern,
       |    _action = action
       |  )
       |}""".stripMargin
  }

  // --- Actor companion generation ---

  /** True when a TypeDefinition carries actor behavior (a non-empty messageAction
    * or signalAction). Such a type is emitted as an actor — a companion whose
    * `actorType` is a live `Actor[T]` with generated receive/receiveSignal — even
    * when it also carries a domainAspect (it then owns a rule-domain: its
    * `elementTypeNames` are the rules its session loads). Detected ahead of
    * `isDomain` in `generate`, so an actor-that-owns-rules routes here rather than
    * to plain domain emission. Empty for every non-actor type, so existing
    * emission is unaffected. */
  private def hasActorBehavior (td: TypeDefinition) : Boolean =
    !ActorAspect.isEmpty(td.actorAspect)

  /** The actor's message type — the type argument of its `Actor[T]` derivation
    * (e.g. `draco.format.json.Json`). Falls back to `Any` if unspecified. */
  private def actorMessageType (td: TypeDefinition) : String =
    td.dracoAspect.derivation
      .find(_.name == "Actor")
      .flatMap(_.typeParameters.headOption)
      .getOrElse("Any")

  private def actorImports (td: TypeDefinition) : String = {
    val pkg = packageHierarchyImports(td.typeName.namePackage)
    val refs = referencedPackageImports(td)
    // Fold ActorRef into the typed brace import when a construction param references it,
    // matching the hand-written convention (one import line, not a trailing separate one).
    val needsActorRef = td.dracoAspect.factory.parameters.exists(_.valueType.contains("ActorRef"))
    val typedImport =
      if (needsActorRef) "import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}"
      else               "import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}"
    val frame = Seq(typedImport, "import org.apache.pekko.actor.typed.scaladsl.Behaviors", "import org.evrete.api.Knowledge")
    val codec = if (referencesCirce(td)) circeImports else Seq.empty
    val external = externalImports(td)
    val all = (pkg ++ refs).distinct ++ frame ++ codec ++ external
    if (all.isEmpty) "" else s"\n${all.mkString("\n")}\n"
  }

  /** Build the actor's private Knowledge by walking its own `elementTypeNames`
    * (its rule set) and accepting each rule's pattern. This reuses domain
    * membership for the actor->rules binding — there is no separate rule-list
    * field. Rule object names carry the Generator's `Rule` suffix. */
  private def actorKnowledge (td: TypeDefinition) : String = {
    val rules = td.domainAspect.elementTypeNames
    val tag = td.typeName.name
    if (rules.isEmpty)
      s"""  private lazy val knowledge: Knowledge = Rule.knowledgeService.newKnowledge("$tag")"""
    else {
      val accepts = rules.map(r => s"    ${stripAspect(r)}Rule.ruleType.pattern.accept(k)").mkString("\n")
      s"""  private lazy val knowledge: Knowledge = {
         |    val k = Rule.knowledgeService.newKnowledge("$tag")
         |$accepts
         |    k
         |  }""".stripMargin
    }
  }

  /** Emit an actor Action body from its body elements, at the given indent.
    * `messageAction` is emitted into `receive` (6-space indent); `signalAction` is
    * emitted once at actor construction (4-space indent) so its bindings — the
    * session, any seeded refs — persist and are in scope for `receive`. No Evrete
    * ctx variable bindings: the body operates on `knowledge`, the message, and `ctx`. */
  private def actorActionBody (action: Action, indent: String = "      ") : String =
    action.body.map {
      case f: Fixed if f.name.nonEmpty   => s"${indent}val ${f.name}: ${f.valueType} = ${initializer(f.valueType, f.value)}"
      case m: Mutable if m.name.nonEmpty => s"${indent}var ${m.name}: ${m.valueType} = ${initializer(m.valueType, m.value)}"
      case be: BodyElement               => s"${indent}${expression(be.value)}"
    }.mkString("\n")

  /** `signalAction` runs ONCE at actor construction — session creation (stateful or
    * stateless), rule/data loading, downstream-ref seeding — so its `session` (and
    * any other bindings) persist for `receive` to reuse. `messageAction` runs per
    * message in `receive` (typically insert + fire). `receiveSignal` is a no-op for
    * now; PostStop session cleanup is a later refinement. Actors with an empty
    * `signalAction` emit exactly as before. */
  private def actorBehavior (td: TypeDefinition, msgType: String) : String = {
    val objName = td.typeName.name
    val setup = actorActionBody(td.actorAspect.setupAction, "    ")
    val recv  = actorActionBody(td.actorAspect.messageAction)
    val setupSection = if (setup.nonEmpty) s"$setup\n\n" else ""
    val recvBlock    = if (recv.nonEmpty)  s"$recv\n"     else ""
    // signalAction is the actor's receiveSignal handler: at PostStop the author reads the
    // accumulated working memory (post-rule-execution harvest) and runs cleanup. Empty ⇒
    // a bare `Behaviors.same`, byte-identical to a no-op receiveSignal.
    val signal = actorActionBody(td.actorAspect.signalAction, "          ")
    val signalBody =
      if (signal.isEmpty) s"      Behaviors.same[$msgType]"
      else
        s"""      signal match {
           |        case org.apache.pekko.actor.typed.PostStop =>
           |$signal
           |          Behaviors.same[$msgType]
           |        case _ => Behaviors.same[$msgType]
           |      }""".stripMargin
    val params = td.dracoAspect.factory.parameters
    // Every actor is a mintable factory (`def actorType`), never a shared `val`:
    // a `def` carries no multiplicity commitment, so Orion alone chooses cardinality
    // (call once = single instance, call N = many, each with its own session).
    // Construction params (e.g. a downstream ref) pass through; nullary when none.
    val actorDecl =
      s"def actorType(${params.map(p => s"${p.name}: ${p.valueType}").mkString(", ")}): ActorType"
    s"""  $actorDecl = new Actor[$msgType] {
       |    override lazy val typeDefinition: TypeDefinition = $objName.typeDefinition
       |
       |${setupSection}    override def receive(ctx: TypedActorContext[$msgType], msg: $msgType): Behavior[$msgType] = {
       |$recvBlock      Behaviors.same[$msgType]
       |    }
       |
       |    override def receiveSignal(ctx: TypedActorContext[$msgType], signal: Signal): Behavior[$msgType] = {
       |$signalBody
       |    }
       |  }""".stripMargin
  }

  private def actorGlobal (td: TypeDefinition) : String = {
    val objName = td.typeName.name
    val msgType = actorMessageType(td)
    val wName = wildcardTypeName(td.typeName)
    val tdLiteral = s"  override lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}"
    val tiLiteral = s"  lazy val dracoType: Type[$wName] = Type[$wName] (typeDefinition)"
    val etn = elementTypeNamesLiteral(td)
    val knowledge = actorKnowledge(td)
    val behavior = actorBehavior(td, msgType)
    s"""object $objName extends App with DracoType {
       |$tdLiteral
       |$tiLiteral
       |
       |$etn
       |
       |$knowledge
       |
       |$behavior
       |}""".stripMargin
  }

  // --- Module ordering (topological sort) ---

  private def moduleOrder (tds: Seq[TypeDefinition]) : Seq[TypeDefinition] = {
    val byName = tds.map(td => baseName(td.typeName.name) -> td).toMap
    val ordered = scala.collection.mutable.LinkedHashSet[String]()
    def walk (td: TypeDefinition) : Unit = {
      val name = baseName(td.typeName.name)
      if (!ordered.contains(name)) {
        ordered += name
        td.dracoAspect.modules.flatMap(tn => byName.get(baseName(tn.name))).foreach(walk)
      }
    }
    val referencedAsModule = tds.flatMap(_.dracoAspect.modules.map(tn => baseName(tn.name))).toSet
    tds.filterNot(td => referencedAsModule.contains(baseName(td.typeName.name))).foreach(walk)
    tds.foreach(walk) // safety net for unreachable types
    ordered.toSeq.map(byName)
  }

  // --- Detection helpers ---

  /** Strip aspect suffix (`.rule` / `.actor`) from a raw TypeName.name or namePath
    * so it can be used as a Scala identifier base before appending `Rule`/`Actor`. */
  private def stripAspect (name: String) : String =
    name.stripSuffix(".rule").stripSuffix(".actor")

  /** A TypeDefinition is a domain when its `domainAspect.typeName` self-loops
    * (i.e., declares itself as the domain). Compared on name + package only, not
    * full `TypeName` equality: a parameterized self-domain (e.g. `Format[T]`) need
    * not restate its type parameters in the self-reference. The
    * `elementTypeNames.nonEmpty` and `(source && target)` clauses are transitional
    * fallbacks for hand-constructed fixtures or transform-domain JSONs that haven't
    * yet had `domainAspect.typeName = self` populated. */
  private def isDomain (td: TypeDefinition) : Boolean =
    (td.domainAspect.typeName.name.nonEmpty &&
     td.domainAspect.typeName.name == td.typeName.name &&
     td.domainAspect.typeName.namePackage == td.typeName.namePackage) ||
    td.domainAspect.elementTypeNames.nonEmpty ||
    (td.dracoAspect.source.name.nonEmpty && td.dracoAspect.target.name.nonEmpty)

  private def isRule (td: TypeDefinition) : Boolean =
    td.typeName.name.endsWith(".rule") || td.ruleAspect.pattern.variables.nonEmpty

  private def isActor (td: TypeDefinition) : Boolean =
    td.typeName.name.endsWith(".actor") || td.dracoAspect.derivation.exists(tn => Set("ActorType", "ExtensibleBehavior").contains(tn.name))

  /** Object-only type: no trait, no factory, no derivation, but has globalElements.
    * Emits object extending DracoType with dracoType = this. */
  private def isObjectOnly (td: TypeDefinition) : Boolean =
    td.dracoAspect.elements.isEmpty && td.dracoAspect.factory.valueType.isEmpty && td.dracoAspect.derivation.isEmpty && td.dracoAspect.globalElements.nonEmpty

  /** Leaf type: none of the structural categories above. Names the negative case
    * so `generate()` reads as a flat dispatch table rather than a fall-through.
    * Leaves and actors share the same emission template (differentiated by
    * `nameSuffix` and `instanceType`), so the dispatcher pairs `isLeaf || isActor`. */
  private def isLeaf (td: TypeDefinition) : Boolean =
    !isDomain(td) && !isRule(td) && !isActor(td) && !isObjectOnly(td)

  // --- Main generate methods ---

  lazy val main: Main = Main.roots
  lazy val test: Test = Test.roots
  lazy val generated: Generated = Generated.roots

  private def hasCodec (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty) : Boolean =
    codecDeclaration(td, familyContext).nonEmpty

  private lazy val externalTypeImports: Map[String, String] = Map(
    "URI"                 -> "import java.net.URI",
    "URL"                 -> "import java.net.URL",
    "BufferedSource"      -> "import scala.io.BufferedSource",
    "KnowledgeService"    -> "import org.evrete.KnowledgeService",
    "Consumer"            -> "import java.util.function.Consumer",
    "Knowledge"           -> "import org.evrete.api.Knowledge",
    "RhsContext"          -> "import org.evrete.api.RhsContext",
    "ExtensibleBehavior"  -> "import org.apache.pekko.actor.typed.ExtensibleBehavior"
  )

  /** Extract all simple type names referenced in a string like "Consumer[Knowledge]" or "Seq[URI]" */
  private def extractTypeNames (valueType: String) : Seq[String] = {
    valueType.split("[\\[\\],\\s]+").iterator.filter(_.nonEmpty).filterNot(Set("Seq", "Map", "Option", "Set")).toSeq
  }

  /** Collect external type imports needed by a TypeDefinition */
  private def externalImports (td: TypeDefinition) : Seq[String] = {
    val allValueTypes = td.dracoAspect.elements.map(_.valueType) ++
      td.dracoAspect.factory.parameters.map(_.valueType) ++
      td.dracoAspect.factory.body.map(_.valueType) ++
      td.dracoAspect.globalElements.map(_.valueType) ++
      td.dracoAspect.derivation.map(_.name) ++
      Seq(td.dracoAspect.extensible.name)
    val typeNames = allValueTypes.flatMap(extractTypeNames).distinct
    val standard = typeNames.flatMap(externalTypeImports.get)
    val mutableRef = if (allValueTypes.exists(_.contains("mutable."))) Seq("import scala.collection.mutable") else Seq.empty
    (standard ++ mutableRef).distinct.sorted
  }

  private lazy val circeImports: Seq[String] = Seq(
    "import io.circe.{Decoder, Encoder, Json}",
    "import io.circe.syntax.EncoderOps"
  )

  private lazy val pekkoImports: Seq[String] = Seq(
    "import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}",
    "import org.apache.pekko.actor.typed.scaladsl.Behaviors"
  )

  private lazy val ruleFrameworkImports: Seq[String] = Seq(
    "import org.evrete.api.{Knowledge, RhsContext}",
    "import java.util.function.Consumer"
  )

  private def packageHierarchyImports (namePackage: Seq[String]) : Seq[String] = {
    val packageImports = namePackage.inits.toSeq.tail.init.map(p => s"import ${p.mkString(".")}._")
    // Skip the `import draco._` self-import when the type is itself in package draco.
    if (namePackage == Seq("draco")) packageImports
    else if (packageImports.contains("import draco._")) packageImports
    else "import draco._" +: packageImports
  }

  /** Imports for packages referenced by the TypeDefinition's cross-type TypeNames
    * (derivation, modules, superDomain, source, target) that are not already
    * covered by packageHierarchyImports.
    *
    * Without this, a type whose derivation points at another package (e.g.
    * Aerial extends World where World lives in domains.world) compiles to
    * "not found: type World". */
  /** Namespaces that always have specific imports emitted via the static
    * lists (pekkoImports, circeImports, ruleFrameworkImports) or via
    * externalTypeImports. Wildcard imports for these are redundant. */
  private lazy val wellKnownExternalPackages: Set[Seq[String]] = Set(
    Seq("org", "apache", "pekko", "actor", "typed"),
    Seq("org", "apache", "pekko", "actor", "typed", "scaladsl"),
    Seq("io", "circe"),
    Seq("io", "circe", "syntax"),
    Seq("org", "evrete"),
    Seq("org", "evrete", "api"),
    Seq("java", "util", "function"),
    Seq("java", "net"),
    Seq("scala", "io")
  )

  private def referencedPackageImports (td: TypeDefinition) : Seq[String] = {
    val ownInits: Set[Seq[String]] = td.typeName.namePackage.inits.toSet
    val covered: Set[Seq[String]] = ownInits + Seq("draco") ++ wellKnownExternalPackages
    val referenced: Seq[TypeName] =
      (td.dracoAspect.derivation
        ++ td.dracoAspect.modules
        ++ Seq(td.dracoAspect.extensible, td.dracoAspect.superDomain, td.dracoAspect.source, td.dracoAspect.target, td.domainAspect.typeName))
        .filter(tn => tn != null && tn.name.nonEmpty)
    referenced
      .map(_.namePackage)
      .filter(_.nonEmpty)
      .filterNot(covered.contains)
      .distinct
      .map(p => s"import ${p.mkString(".")}._")
  }

  /** True if any globalElement body references circe types (Encoder/Decoder/Json/.asJson).
    * Drives circeImports emission for types that hand-roll codecs as Monadic globals
    * rather than receiving an auto-generated codec block. */
  private def referencesCirce (td: TypeDefinition) : Boolean = {
    val texts: Iterator[String] =
      td.dracoAspect.globalElements.iterator.map(e => expression(e.value)) ++
      td.dracoAspect.globalElements.iterator.flatMap(_.body.map(b => expression(b.value))) ++
      td.dracoAspect.factory.body.iterator.map(e => expression(e.value))
    val markers = Seq("Encoder[", "Decoder[", "Encoder.instance", "Decoder.instance", "Json.obj", "Json.fromString", ".asJson", "io.circe")
    texts.exists(t => t != null && markers.exists(t.contains))
  }

  private def typeImports (td: TypeDefinition, hasCodec: Boolean, instanceType: String = "") : String = {
    val pkg = packageHierarchyImports(td.typeName.namePackage)
    val refs = referencedPackageImports(td)
    val codec = if (hasCodec || referencesCirce(td)) circeImports else Seq.empty
    val instance = instanceType match {
      case "actor" => pekkoImports
      case _ => Seq.empty
    }
    val external = externalImports(td)
    val allImports = (pkg ++ refs).distinct ++ codec ++ instance ++ external
    if (allImports.isEmpty) "" else s"\n${allImports.mkString("\n")}\n"
  }

  def generate (td: TypeDefinition) : String = {
    if (isRule(td)) {
      val imports = ruleImports(td.typeName.namePackage)
      val ruleName = stripAspect(td.typeName.name) + "Rule"
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |
         |$imports
         |
         |trait $ruleName
         |
         |${ruleGlobal(td)}
         |""".stripMargin
    } else if (hasActorBehavior(td)) {
      val imports = actorImports(td)
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |$imports
         |${traitDeclaration(td)}
         |
         |${actorGlobal(td)}
         |""".stripMargin
    } else if (isDomain(td)) {
      val imports = typeImports(td, hasCodec(td), "domain")
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |$imports
         |${traitDeclaration(td)}
         |
         |${domainGlobal(td)}
         |""".stripMargin
    } else if (isObjectOnly(td)) {
      val imports = typeImports(td, hasCodec = false)
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |$imports
         |${objectGlobal(td)}
         |""".stripMargin
    } else if (isLeaf(td) || isActor(td)) {
      // Leaves and actors share the unified trait+global emission;
      // `nameSuffix` ("Actor" iff td.typeName.name ends in ".actor") and `instanceType`
      // ("actor" iff isActor) differentiate the two.
      val isActorType = isActor(td)
      // pekko behaviour imports (Behavior/Signal/TypedActorContext/Behaviors) are only
      // used by factory-emitted receive/receiveSignal bodies; a factory-less actor
      // container (e.g. the base Actor[T] trait) needs none.
      val instanceType = if (isActorType && td.dracoAspect.factory.valueType.nonEmpty) "actor" else ""
      // Suffix is only applied to actor-aspect TDs (name ending in `.actor`),
      // not to every type that uses the Pekko actor framework. Without this
      // restriction the base `Actor` type itself becomes `ActorActor`.
      val nameSuffix = if (td.typeName.name.endsWith(".actor")) "Actor" else ""
      val imports = typeImports(td, hasCodec(td), instanceType)
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |$imports
         |${traitDeclaration(td, nameSuffix)}
         |
         |${typeGlobal(td, nameSuffix = nameSuffix)}
         |""".stripMargin
    } else {
      // Unreachable: the four predicates above partition the type space.
      // This branch exists only to satisfy if-chain exhaustivity; if it fires,
      // a predicate has drifted and Generator.generate has lost a case.
      throw new IllegalStateException(s"Generator.generate: no branch matched ${td.typeName.name}")
    }
  }

  def generate (typeDefinitions: Seq[TypeDefinition]) : String = {
    if (typeDefinitions.isEmpty) return ""
    if (typeDefinitions.size == 1) return generate(typeDefinitions.head)
    val ordered = moduleOrder(typeDefinitions)
    val pkg = ordered.head.typeName.namePackage.mkString(".")
    val anyCodec = ordered.exists(td => hasCodec(td, typeDefinitions))
    // Merge external imports across all types in the family
    val mergedTd = TypeDefinition(
      _typeName = ordered.head.typeName,
      _dracoAspect = DracoAspect(
        _derivation = ordered.flatMap(_.dracoAspect.derivation) ++
          ordered.map(_.dracoAspect.extensible).filter(_.name.nonEmpty),
        _elements = ordered.flatMap(_.dracoAspect.elements),
        _factory = Factory("", _parameters = ordered.flatMap(_.dracoAspect.factory.parameters)),
        _globalElements = ordered.flatMap(_.dracoAspect.globalElements)
      )
    )
    val instanceType = if (ordered.exists(isActor)) "actor" else ""
    val imports = typeImports(mergedTd, anyCodec, instanceType)
    val typeBlocks = ordered.map { td =>
      s"${traitDeclaration(td)}\n\n${typeGlobal(td, typeDefinitions)}"
    }
    s"\npackage $pkg\n$imports\n${typeBlocks.mkString("\n\n")}\n"
  }

  private def ruleImports (namePackage: Seq[String]) : String = {
    val allImports = packageHierarchyImports(namePackage) ++ ruleFrameworkImports
    allImports.mkString("\n")
  }

  def apply (typeDictionary: TypeDictionary) : Generator = {
    new Generator {}
  }

  // --- Runtime compilation (moved from RuntimeCompiler) ---

  def compile(source: String, fileName: String): Either[Seq[String], File] = {
    val tempDir = Files.createTempDirectory("draco-gen").toFile
    val sourceFile = new File(tempDir, fileName)
    val writer = new PrintWriter(sourceFile)
    writer.write(source)
    writer.close()

    val settings = new Settings()
    settings.usejavacp.value = true
    settings.outputDirs.setSingleOutput(tempDir.getAbsolutePath)

    val reporter = new StoreReporter(settings)
    val compiler = new Global(settings, reporter)
    val run = new compiler.Run()
    run.compile(List(sourceFile.getAbsolutePath))

    if (reporter.hasErrors) {
      Left(reporter.infos.collect {
        case info if info.severity == reporter.ERROR => info.msg
      }.toSeq)
    } else {
      Right(tempDir)
    }
  }

  def compileMulti(sources: Seq[(String, String)]): Either[Seq[String], File] = {
    val tempDir = Files.createTempDirectory("draco-gen").toFile
    val sourceFiles = sources.map { case (source, fileName) =>
      val sourceFile = new File(tempDir, fileName)
      val writer = new PrintWriter(sourceFile)
      writer.write(source)
      writer.close()
      sourceFile.getAbsolutePath
    }

    val settings = new Settings()
    settings.usejavacp.value = true
    settings.outputDirs.setSingleOutput(tempDir.getAbsolutePath)

    val reporter = new StoreReporter(settings)
    val compiler = new Global(settings, reporter)
    val run = new compiler.Run()
    run.compile(sourceFiles.toList)

    if (reporter.hasErrors) {
      Left(reporter.infos.collect {
        case info if info.severity == reporter.ERROR => info.msg
      }.toSeq)
    } else {
      Right(tempDir)
    }
  }

  def loadClass(classDir: File, className: String): Class[_] = {
    val loader = new java.net.URLClassLoader(
      Array(classDir.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )
    loader.loadClass(className)
  }
}
