package draco

import io.circe.syntax.EncoderOps

trait Generator {

}

object Generator extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Generator",
      _namePackage = Seq ("draco")
    ),
    _factory = Factory (
      "Generator",
      _parameters = Seq (
        Parameter ("typeDictionary", "TypeDictionary", "")
      )
    )
  )
  lazy val typeInstance: Type[Generator] = Type[Generator] (typeDefinition)

  // --- Literal generation helpers ---

  private def wildcardName (name: String): String = {
    val idx = name.indexOf('[')
    if (idx < 0) name else name.substring(0, idx) + "[_]"
  }

  private def baseName (name: String): String = {
    val idx = name.indexOf('[')
    if (idx < 0) name else name.substring(0, idx)
  }

  /** Parameterized name from TypeName: "Primal[T]" or "Primal" if no typeParameters */
  private def parameterizedName (tn: TypeName) : String = {
    if (tn.typeParameters.isEmpty) tn.name
    else s"${tn.name}[${tn.typeParameters.mkString(", ")}]"
  }

  /** Wildcard name from TypeName: "Primal[_]" or "Primal" if no typeParameters */
  private def wildcardTypeName (tn: TypeName) : String = {
    if (tn.typeParameters.isEmpty) tn.name
    else s"${tn.name}[${tn.typeParameters.map(_ => "_").mkString(", ")}]"
  }

  private def typeNameLiteral (tn: TypeName) : String = {
    val args = Seq(
      Some(s""""${tn.name}""""),
      if (tn.namePackage.nonEmpty) Some(s"_namePackage = Seq(${tn.namePackage.map(s => s""""$s"""").mkString(", ")})") else None,
      if (tn.typeParameters.nonEmpty) Some(s"_typeParameters = Seq(${tn.typeParameters.map(s => s""""$s"""").mkString(", ")})") else None
    ).flatten
    s"TypeName (${args.mkString(", ")})"
  }

  private def typeDefinitionLoad (td: TypeDefinition) : String = {
    s"""draco.TypeDefinition.load(${typeNameLiteral(td.typeName)})"""
  }

  private def ruleDefinitionFromJson (td: TypeDefinition) : String = {
    val json = td.asJson.spaces2
    s"""parser.parse(\"\"\"$json\"\"\").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)"""
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
      s"  def w$idx($params): Boolean = ${c.value}"
    }.mkString("\n")
  }

  private def whereConditions (
    conditions: Seq[Condition],
    typeName: TypeName
  ) : String = {
    if (conditions.isEmpty) ""
    else conditions.zipWithIndex.map { case (c, idx) =>
      // Reference the condition function with fully qualified class name and $-prefixed parameters
      val params = c.parameters.map(p => s"$$${p.name}").mkString(", ")
      s"""    .where("${typeName.namePath}.w$idx($params)")"""
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
        if (f.name.nonEmpty) s"      val ${f.name}: ${f.valueType} = ${f.value}"
        else s"      ${f.value}"
      case m: Mutable =>
        if (m.name.nonEmpty) s"      var ${m.name}: ${m.valueType} = ${m.value}"
        else s"      ${m.value}"
      case d: Dynamic =>
        if (d.name.nonEmpty) s"      def ${d.name}: ${d.valueType} = ${d.value}"
        else s"      ${d.value}"
      case v: Variable =>
        s"      val ${v.name}: ${v.valueType} = ctx.get[${v.valueType}](\"$$${v.name}\")"
      case te: BodyElement =>
        s"      ${te.value}"
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
    derivation: Seq[TypeName]
  ) : String = {
    if (derivation.isEmpty) "extends Extensible"
    else if (derivation.head.name == "Extensible" && derivation.head.typeParameters.nonEmpty) {
      // Extensible as Generator directive: substitute its type parameter into extends position
      val extendsType = derivation.head.typeParameters.head
      val withTypes = derivation.tail.map(parameterizedName)
      if (withTypes.isEmpty) s"extends $extendsType"
      else s"extends $extendsType with ${withTypes.mkString(" with ")}"
    }
    else "extends Extensible with " + derivation.map(parameterizedName).mkString(" with ")
  }

  private def methodParameters (
    parameters: Seq[Parameter]
  ) : String = {
    if (parameters.isEmpty) ""
    else {
      val params = parameters.map { p =>
        val default = if (p.value.isEmpty) "" else s" = ${p.value}"
        s"${p.name}: ${p.valueType}$default"
      }
      s"(${params.mkString(", ")})"
    }
  }

  private def methodBody (
    body: Seq[BodyElement]
  ) : String = {
    if (body.isEmpty) "???"
    else if (body.size == 1) {
      // Single expression - just the value
      val be = body.head
      if (be.value.isEmpty) "???" else be.value
    } else {
      // Multiple statements - all but last are val/var declarations or monadic effects, last is return expression
      val init = body.init.map {
        case f: Fixed => s"    val ${f.name}: ${f.valueType} = ${f.value}"
        case m: Mutable => s"    var ${m.name}: ${m.valueType} = ${m.value}"
        case mo: Monadic => s"    ${mo.value}"
        case be: BodyElement => s"    val ${be.name}: ${be.valueType} = ${be.value}"
      }
      val last = body.last.value
      s"{\n${init.mkString("\n")}\n    $last\n  }"
    }
  }

  private def typeBody (
    elements: Seq[TypeElement]
  ) : String = {
    if (elements.isEmpty) ""
    else {
      val members = elements.map {
        case f: Fixed => s"  val ${f.name}: ${f.valueType}"
        case m: Mutable => s"  var ${m.name}: ${m.valueType}"
        case d: Dynamic => s"  def ${d.name}${methodParameters(d.parameters)}: ${d.valueType}"
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
        val default = if (p.value.isEmpty) "" else s" = ${p.value}"
        s"_${p.name}: ${p.valueType}$default"
      }
      s"\n    ${params.mkString(",\n    ")}\n  "
    }
  }

  private def factoryBody (
    factory: Factory
  ) : String = {
    val instanceOverrides = Seq(
      "    override lazy val typeInstance: DracoType = ${objName}.typeInstance",
      "    override lazy val typeDefinition: TypeDefinition = ${objName}.typeDefinition"
    ).map(s => s.replace("${objName}", baseName(factory.valueType)))
    if (factory.body.nonEmpty) {
      val overrides = factory.body.map {
        case f: Fixed   => s"    override val ${f.name}: ${f.valueType} = ${f.value}"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = ${m.value}"
        case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = ${d.value}"
        case be: BodyElement => s"    override val ${be.name}: ${be.valueType} = ${be.value}"
      }
      s"{\n${(overrides ++ instanceOverrides).mkString("\n")}\n  }"
    } else {
      val overrides = factory.parameters.map { p =>
        s"    override val ${p.name}: ${p.valueType} = _${p.name}"
      }
      s"{\n${(overrides ++ instanceOverrides).mkString("\n")}\n  }"
    }
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
          val init = if (f.value.isEmpty) s"null.asInstanceOf[${f.valueType}]" else f.value
          s"  lazy val ${f.name}: ${f.valueType} = $init"
        case m: Mutable =>
          val init = if (m.value.isEmpty) s"null.asInstanceOf[${m.valueType}]" else m.value
          s"  var ${m.name}: ${m.valueType} = $init"
        case d: Dynamic =>
          s"  def ${d.name}${methodParameters(d.parameters)}: ${d.valueType} = ${methodBody(d.body)}"
        case mo: Monadic =>
          s"  ${mo.value}"
        case be: BodyElement =>
          val init = if (be.value.isEmpty) s"null.asInstanceOf[${be.valueType}]" else be.value
          s"  val ${be.name}: ${be.valueType} = $init"
      }
      globals.mkString("\n")
    }
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
      case _                          => s"null.asInstanceOf[$valueType]"
    }
  }

  // --- Codec generation helpers ---

  private def elisionCheck (p: Parameter, instanceVar: String) : Option[String] = {
    if (p.value.isEmpty) None  // required field — always encode
    else p.valueType match {
      case "String"                    => Some(s"$instanceVar.${p.name}.nonEmpty")
      case s if s.startsWith("Seq[")   => Some(s"$instanceVar.${p.name}.nonEmpty")
      case s if s.startsWith("Map[")   => Some(s"$instanceVar.${p.name}.nonEmpty")
      case "Factory"                   => Some(s"$instanceVar.${p.name}.valueType.nonEmpty")
      case "Action"                    => Some(s"$instanceVar.${p.name}.body.nonEmpty")
      case "Pattern"                   => Some(s"$instanceVar.${p.name}.variables.nonEmpty")
      case "TypeName"                  => Some(s"$instanceVar.${p.name}.name.nonEmpty")
      case _                           => None  // no natural emptiness — always encode
    }
  }

  /** Find the root discriminated union parent by walking up the derivation chain.
    * Returns the topmost ancestor that has modules (e.g., TypeElement, not BodyElement). */
  private def findDiscriminatedParent (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition]
  ) : Option[String] = {
    td.derivation.map(tn => baseName(tn.name)).flatMap { name =>
      familyMap.get(name) match {
        case Some(parentTd) if parentTd.modules.nonEmpty =>
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
    td.modules.flatMap { moduleTn =>
      val name = baseName(moduleTn.name)
      familyMap.get(name) match {
        case Some(moduleTd) if moduleTd.modules.nonEmpty =>
          // Intermediate sealed trait — recurse
          collectLeafModules(moduleTd, familyMap)
        case _ =>
          // Leaf (concrete) or not in familyMap
          Seq(name)
      }
    }
  }

  private def fieldElisionEncoder (params: Seq[Parameter], instanceVar: String) : String = {
    val fields = params.map { p =>
      elisionCheck(p, instanceVar) match {
        case Some(check) =>
          s"""      if ($check) Some("${p.name}" -> $instanceVar.${p.name}.asJson) else None"""
        case None =>
          s"""      Some("${p.name}" -> $instanceVar.${p.name}.asJson)"""
      }
    }
    s"""Encoder.instance { $instanceVar =>
       |    val fields = Seq(
       |${fields.mkString(",\n")}
       |    ).flatten
       |    Json.obj(fields: _*)
       |  }""".stripMargin
  }

  private def fieldElisionDecoder (params: Seq[Parameter], typeName: String) : String = {
    val objName = baseName(typeName)
    val forLines = params.map { p =>
      if (p.value.nonEmpty) {
        val default = p.valueType match {
          case "String"                    => "\"\""
          case s if s.startsWith("Seq[")   => "Seq.empty"
          case s if s.startsWith("Map[")   => "Map.empty"
          case _                           => p.value
        }
        s"""      _${p.name} <- cursor.downField("${p.name}").as[Option[${p.valueType}]].map(_.getOrElse($default))"""
      } else {
        s"""      _${p.name} <- cursor.downField("${p.name}").as[${p.valueType}]"""
      }
    }
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
    val params = td.factory.parameters
    s"""  implicit lazy val encoder: Encoder[$wName] = ${fieldElisionEncoder(params, "x")}
       |  implicit lazy val decoder: Decoder[$wName] = ${fieldElisionDecoder(params, name)}""".stripMargin
  }

  private def discriminatedCodecDeclaration (
    td: TypeDefinition,
    familyMap: Map[String, TypeDefinition]
  ) : String = {
    val name = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    val leaves = collectLeafModules(td, familyMap)

    // Encoder: pattern match on each leaf subtype
    val matchArms = leaves.map { leaf =>
      s"""      case _: $leaf => "$leaf""""
    }.mkString("\n")
    val fallbackMatch = s"""      case _: $name => "$name""""

    // For each leaf, build encoder fields from its factory parameters
    val encoderBody =
      s"""  implicit lazy val encoder: Encoder[$wName] = Encoder.instance { x =>
         |    val kind = x match {
         |$matchArms
         |$fallbackMatch
         |    }
         |    val fields = Seq(
         |      Some("kind" -> Json.fromString(kind)),
         |${encoderFieldLines("x", td, leaves, familyMap)}
         |    ).flatten
         |    Json.obj(fields: _*)
         |  }""".stripMargin

    // Decoder: dispatch on kind field
    val decoderCases = leaves.map { leaf =>
      val leafTd = familyMap.get(leaf)
      val params = leafTd.map(_.factory.parameters).getOrElse(Seq.empty)
      val forLines = params.map { p =>
        if (p.value.nonEmpty) {
          val default = p.valueType match {
            case "String"                    => "\"\""
            case s if s.startsWith("Seq[")   => "Seq.empty"
            case s if s.startsWith("Map[")   => "Map.empty"
            case _                           => p.value
          }
          s"""          _${p.name} <- cursor.downField("${p.name}").as[Option[${p.valueType}]].map(_.getOrElse($default))"""
        } else {
          s"""          _${p.name} <- cursor.downField("${p.name}").as[${p.valueType}]"""
        }
      }
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
    val fallbackParams = td.factory.parameters
    val fallbackForLines = fallbackParams.map { p =>
      if (p.value.nonEmpty) {
        val default = p.valueType match {
          case "String"                    => "\"\""
          case s if s.startsWith("Seq[")   => "Seq.empty"
          case s if s.startsWith("Map[")   => "Map.empty"
          case _                           => p.value
        }
        s"""          _${p.name} <- cursor.downField("${p.name}").as[Option[${p.valueType}]].map(_.getOrElse($default))"""
      } else {
        s"""          _${p.name} <- cursor.downField("${p.name}").as[${p.valueType}]"""
      }
    }
    val fallbackYieldArgs = fallbackParams.map(p => s"_${p.name}").mkString(", ")
    val fallbackCase = if (fallbackParams.isEmpty) {
      s"""      case _ =>
         |        Right($name.Null)""".stripMargin
    } else {
      s"""      case _ =>
         |        for {
         |${fallbackForLines.mkString("\n")}
         |        } yield $name ($fallbackYieldArgs)""".stripMargin
    }

    val decoderBody =
      s"""  implicit lazy val decoder: Decoder[$wName] = Decoder.instance { cursor =>
         |    cursor.downField("kind").as[String].flatMap {
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
    instanceVar: String,
    parentTd: TypeDefinition,
    leaves: Seq[String],
    familyMap: Map[String, TypeDefinition]
  ) : String = {
    // Fields accessible on the parent: its elements and factory parameters
    val parentFieldNames = (parentTd.elements.map(_.name) ++ parentTd.factory.parameters.map(_.name)).distinct

    // Find representative Parameter for each parent field (from factory params or synthesize from elements)
    val parentParams: Seq[Parameter] = parentFieldNames.flatMap { name =>
      parentTd.factory.parameters.find(_.name == name).orElse {
        parentTd.elements.find(_.name == name).map(e => Parameter(e.name, e.valueType, ""))
      }
    }

    parentParams.map { p =>
      val hasNonEmpty = p.valueType == "String" ||
        p.valueType.startsWith("Seq[") ||
        p.valueType.startsWith("Map[")
      elisionCheck(p, instanceVar) match {
        case Some(check) =>
          s"""      if ($check) Some("${p.name}" -> $instanceVar.${p.name}.asJson) else None"""
        case None if hasNonEmpty =>
          // For discriminated union fields, use .nonEmpty as default elision for String/Seq/Map
          s"""      if ($instanceVar.${p.name}.nonEmpty) Some("${p.name}" -> $instanceVar.${p.name}.asJson) else None"""
        case None =>
          s"""      Some("${p.name}" -> $instanceVar.${p.name}.asJson)"""
      }
    }.mkString(",\n")
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
        if (td.modules.nonEmpty) {
          // Pattern 2: discriminated union (top-level sealed trait)
          discriminatedCodecDeclaration(td, familyMap)
        } else if (td.factory.valueType.nonEmpty && td.factory.parameters.nonEmpty) {
          // Pattern 1: simple field-based — only when factory params are accessible as trait elements
          val elementNames = td.elements.map(_.name).toSet
          val paramNames = td.factory.parameters.map(_.name).toSet
          if (paramNames.subsetOf(elementNames)) simpleCodecDeclaration(td)
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
    factory: Factory
  ) : String = {
    val name = typeName.name
    val wName = wildcardTypeName(typeName)
    val typeParams = if (typeName.typeParameters.isEmpty) "" else s"[${typeName.typeParameters.map(_ => "Nothing").mkString(", ")}]"
    if (factory.valueType.nonEmpty && factory.body.isEmpty) {
      val nullArgs = factory.parameters.map { p =>
        s"    _${p.name} = ${nullValueFor(p.valueType, p.value)}"
      }
      if (nullArgs.isEmpty) s"lazy val Null: $wName = apply$typeParams()"
      else s"lazy val Null: $wName = apply$typeParams(\n${nullArgs.mkString(",\n")}\n  )"
    } else if (factory.valueType.nonEmpty && factory.body.nonEmpty) {
      val nullMembers = elements.map { e =>
        s"    override val ${e.name}: ${e.valueType} = ${nullValueFor(e.valueType, "")}"
      }
      val nullInstanceOverrides = Seq(
        s"    override lazy val typeDefinition: TypeDefinition = TypeDefinition.Null",
        s"    override lazy val typeInstance: DracoType = $name.Null"
      )
      s"lazy val Null: $wName = new $wName {\n${(nullMembers ++ nullInstanceOverrides).mkString("\n")}\n  }"
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

  private def typeGlobal (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty) : String = {
    val factory = td.factory
    val hasFactory = factory.valueType.nonEmpty
    val hasGlobalElements = td.globalElements.nonEmpty
    val objName = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    val typeParams = if (td.typeName.typeParameters.isEmpty) "" else s"[${td.typeName.typeParameters.mkString(", ")}]"
    val appMixin = if (hasExplicitMain(td.globalElements)) "" else "App with "

    val header = s"object $objName extends ${appMixin}TypeInstance"
    val tdLiteral = s"  lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}"
    val tiLiteral = s"  lazy val typeInstance: Type[$wName] = Type[$wName] (typeDefinition)"
    val codec = codecDeclaration(td, familyContext)
    val codecBlock = if (codec.nonEmpty) s"\n$codec\n" else ""

    if (!hasFactory && !hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |$codecBlock}""".stripMargin
    } else if (!hasFactory && hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |$codecBlock
         |${globalElementsDeclaration(td.globalElements)}
         |}""".stripMargin
    } else {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |$codecBlock
         |  def apply$typeParams (${factoryParameters(factory.parameters)}) : ${factory.valueType} = new ${factory.valueType} ${factoryBody(factory)}
         |
         |  ${nullInstance(td.typeName, td.elements, td.factory)}
         |
         |${globalElementsDeclaration(td.globalElements)}
         |}""".stripMargin
    }
  }

  // --- Object-only companion generation (no trait, extends DracoType) ---

  private def objectGlobal (td: TypeDefinition) : String = {
    val objName = td.typeName.name
    s"""object $objName extends DracoType {
       |  lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}
       |  lazy val typeInstance: DracoType = this
       |
       |${globalElementsDeclaration(td.globalElements)}
       |}""".stripMargin
  }

  // --- Trait declaration helper ---

  private def traitDeclaration (td: TypeDefinition) : String =
    s"${typeModifier(td.modules)}trait ${parameterizedName(td.typeName)} ${typeExtends(td.derivation)} ${typeBody(td.elements)}"

  // --- DomainInstance companion generation ---

  private def domainInstanceLiteral (objName: String, td: TypeDefinition) : String = {
    val elementNames = if (td.elementTypeNames.isEmpty) "Seq.empty"
    else {
      val names = td.elementTypeNames.map(n => s"      \"$n\"")
      s"Seq (\n${names.mkString(",\n")}\n    )"
    }
    val optionalFields = Seq(
      if (td.superDomain.name.nonEmpty) Some(s"      _superDomain = ${typeNameLiteral(td.superDomain)}") else None,
      if (td.source.name.nonEmpty) Some(s"      _source = ${typeNameLiteral(td.source)}") else None,
      if (td.target.name.nonEmpty) Some(s"      _target = ${typeNameLiteral(td.target)}") else None
    ).flatten
    val optionalFieldsStr = if (optionalFields.isEmpty) ""
    else s",\n${optionalFields.mkString(",\n")}"
    s"""  lazy val domainInstance: Domain[$objName] = Domain[$objName] (
       |    _domainDefinition = TypeDefinition (
       |      typeDefinition.typeName,
       |      _elementTypeNames = $elementNames$optionalFieldsStr
       |    )
       |  )""".stripMargin
  }

  private def domainGlobal (td: TypeDefinition, familyContext: Seq[TypeDefinition] = Seq.empty) : String = {
    val objName = td.typeName.name
    val wName = wildcardTypeName(td.typeName)
    val hasGlobalElements = td.globalElements.nonEmpty
    val appMixin = if (hasExplicitMain(td.globalElements)) "" else "App with "

    val header = s"object $objName extends ${appMixin}DomainInstance"
    val tdLiteral = s"  lazy val typeDefinition: TypeDefinition = ${typeDefinitionLoad(td)}"
    val tiLiteral = s"  lazy val typeInstance: Type[$wName] = Type[$wName] (typeDefinition)"
    val codec = codecDeclaration(td, familyContext)
    val codecBlock = if (codec.nonEmpty) s"\n$codec\n" else ""
    val diLiteral = domainInstanceLiteral(objName, td)
    val globals = if (hasGlobalElements) s"\n${globalElementsDeclaration(td.globalElements)}" else ""

    s"""$header {
       |$tdLiteral
       |$tiLiteral
       |$codecBlock
       |$diLiteral$globals
       |}""".stripMargin
  }

  // --- RuleInstance companion generation ---

  private def ruleGlobal (td: TypeDefinition) : String = {
    val name = td.typeName.name
    val ruleTd = TypeDefinition (
      td.typeName,
      _variables = td.variables,
      _conditions = td.conditions,
      _values = td.values,
      _pattern = td.pattern,
      _action = td.action
    )
    val appMixin = if (hasExplicitMain(td.globalElements)) "" else "App with "
    s"""object $name extends ${appMixin}RuleInstance {
       |  private lazy val ruleDefinition: TypeDefinition = ${ruleDefinitionFromJson(ruleTd)}
       |${conditionFunctions(td.conditions)}
       |  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
       |${actionBody(td.action, td.variables, td.values)}
       |  }
       |
       |  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
       |    knowledge
       |    .builder()
       |    .newRule ("${td.typeName.namePath}")
       |    .forEach (
       |${factVariables(td.variables)}
       |    )
       |${whereConditions(td.conditions, td.typeName)}
       |    .execute (action)
       |    .build()
       |  }
       |
       |  lazy val ruleInstance: RuleType = Rule[$name] (
       |    ruleDefinition,
       |    _pattern = pattern,
       |    _action = action
       |  )
       |
       |  lazy val typeDefinition: TypeDefinition = TypeDefinition (
       |    _typeName = ruleDefinition.typeName,
       |    _derivation = Seq (
       |      RuleInstance.typeInstance.typeDefinition.typeName
       |    )
       |  )
       |
       |  lazy val typeInstance: DracoType = Type[$name] (typeDefinition)
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
        td.modules.flatMap(tn => byName.get(baseName(tn.name))).foreach(walk)
      }
    }
    val referencedAsModule = tds.flatMap(_.modules.map(tn => baseName(tn.name))).toSet
    tds.filterNot(td => referencedAsModule.contains(baseName(td.typeName.name))).foreach(walk)
    tds.foreach(walk) // safety net for unreachable types
    ordered.toSeq.map(byName)
  }

  // --- Detection helpers ---

  private def isDomain (td: TypeDefinition) : Boolean =
    td.elementTypeNames.nonEmpty

  private def isRule (td: TypeDefinition) : Boolean =
    td.variables.nonEmpty

  private def isActor (td: TypeDefinition) : Boolean =
    td.derivation.exists(tn => Set("ActorType", "ActorInstance", "ExtensibleBehavior").contains(tn.name))

  /** Object-only type: no trait, no factory, no derivation, but has globalElements.
    * Emits object extending DracoType with typeInstance = this. */
  private def isObjectOnly (td: TypeDefinition) : Boolean =
    td.elements.isEmpty && td.factory.valueType.isEmpty && td.derivation.isEmpty && td.globalElements.nonEmpty

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
    valueType.split("[\\[\\],\\s]+").filter(_.nonEmpty).filterNot(Set("Seq", "Map", "Option", "Set"))
  }

  /** Collect external type imports needed by a TypeDefinition */
  private def externalImports (td: TypeDefinition) : Seq[String] = {
    val allValueTypes = td.elements.map(_.valueType) ++
      td.factory.parameters.map(_.valueType) ++
      td.globalElements.map(_.valueType) ++
      td.derivation.map(_.name)
    val typeNames = allValueTypes.flatMap(extractTypeNames).distinct
    typeNames.flatMap(externalTypeImports.get).distinct.sorted
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
    "import io.circe.{Json, parser}",
    "import org.evrete.api.{Knowledge, RhsContext}",
    "import java.util.function.Consumer"
  )

  private def packageHierarchyImports (namePackage: Seq[String]) : Seq[String] = {
    val packageImports = namePackage.inits.toSeq.tail.init.map(p => s"import ${p.mkString(".")}._")
    if (packageImports.contains("import draco._")) packageImports
    else "import draco._" +: packageImports
  }

  private def typeImports (td: TypeDefinition, hasCodec: Boolean, instanceType: String = "") : String = {
    val pkg = packageHierarchyImports(td.typeName.namePackage)
    val codec = if (hasCodec) circeImports else Seq.empty
    val instance = instanceType match {
      case "actor" => pekkoImports
      case _ => Seq.empty
    }
    val external = externalImports(td)
    val allImports = pkg ++ codec ++ instance ++ external
    if (allImports.isEmpty) "" else s"\n${allImports.mkString("\n")}\n"
  }

  def generate (td: TypeDefinition) : String = {
    if (isRule(td)) {
      val imports = ruleImports(td.typeName.namePackage)
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |
         |$imports
         |
         |trait ${td.typeName.name} extends RuleInstance
         |
         |${ruleGlobal(td)}
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
    } else {
      val instanceType = if (isActor(td)) "actor" else ""
      val imports = typeImports(td, hasCodec(td), instanceType)
      s"""
         |package ${td.typeName.namePackage.mkString(".")}
         |$imports
         |${traitDeclaration(td)}
         |
         |${typeGlobal(td)}
         |""".stripMargin
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
      _elements = ordered.flatMap(_.elements),
      _factory = Factory("", _parameters = ordered.flatMap(_.factory.parameters)),
      _globalElements = ordered.flatMap(_.globalElements)
    )
    val imports = typeImports(mergedTd, anyCodec)
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
}
