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

  private def typeDefinitionFromJson (td: TypeDefinition) : String = {
    val json = td.asJson.spaces2
    s"""parser.parse(\"\"\"$json\"\"\").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)"""
  }

  private def ruleDefinitionFromJson (rd: RuleDefinition) : String = {
    val json = rd.asJson.spaces2
    s"""parser.parse(\"\"\"$json\"\"\").flatMap(_.as[RuleDefinition]).getOrElse(RuleDefinition.Null)"""
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
      s"""    .where("${typeName.fullName}.w$idx($params)")"""
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
    if (derivation.isEmpty) "extends TypeInstance"
    else "extends " + derivation.map(_.name).mkString(" with ")
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
    if (factory.body.nonEmpty) {
      val overrides = factory.body.map {
        case f: Fixed   => s"    override val ${f.name}: ${f.valueType} = ${f.value}"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = ${m.value}"
        case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = ${d.value}"
        case be: BodyElement => s"    override val ${be.name}: ${be.valueType} = ${be.value}"
      }
      s"{\n${overrides.mkString("\n")}\n  }"
    } else {
      val overrides = factory.parameters.map { p =>
        s"    override val ${p.name}: ${p.valueType} = _${p.name}"
      }
      val tdOverride = "    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition"
      val allOverrides = overrides :+ tdOverride
      s"{\n${allOverrides.mkString("\n")}\n  }"
    }
  }

  private def globalElementsDeclaration (
    globalElements: Seq[BodyElement]
  ) : String = {
    if (globalElements.isEmpty) ""
    else {
      val globals = globalElements.map {
        case f: Fixed =>
          val init = if (f.value.isEmpty) s"null.asInstanceOf[${f.valueType}]" else f.value
          s"  val ${f.name}: ${f.valueType} = $init"
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

  private def nullInstance (
    typeName: TypeName,
    elements: Seq[TypeElement],
    factory: Factory
  ) : String = {
    val name = baseName(typeName.name)
    if (factory.valueType.nonEmpty) {
      val nullArgs = factory.parameters.map { p =>
        s"    _${p.name} = ${nullValueFor(p.valueType, p.value)}"
      }
      if (nullArgs.isEmpty) s"lazy val Null: $name = apply()"
      else s"lazy val Null: $name = apply(\n${nullArgs.mkString(",\n")}\n  )"
    } else if (elements.isEmpty) {
      s"lazy val Null: $name = new $name {}"
    } else {
      val nullMembers = elements.map {
        case f: Fixed => s"    override val ${f.name}: ${f.valueType} = null.asInstanceOf[${f.valueType}]"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = null.asInstanceOf[${m.valueType}]"
        case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = null.asInstanceOf[${d.valueType}]"
        case p: Parameter => s"    override val ${p.name}: ${p.valueType} = null.asInstanceOf[${p.valueType}]"
        case te: TypeElement => s"    override val ${te.name}: ${te.valueType} = null.asInstanceOf[${te.valueType}]"
      }
      s"lazy val Null: $name = new $name {\n${nullMembers.mkString("\n")}\n  }"
    }
  }

  private def defaultInstance (
    typeName: TypeName,
    factory: Factory
  ) : String = {
    val name = baseName(typeName.name)
    val specifiedParams = factory.parameters.filter(_.value.nonEmpty)
    if (specifiedParams.isEmpty) s"lazy val Default: $name = apply()"
    else {
      val paramAssignments = specifiedParams.map { p =>
        s"    _${p.name} = ${p.value}"
      }
      s"lazy val Default: $name = apply(\n${paramAssignments.mkString(",\n")}\n  )"
    }
  }

  // --- Companion object generation ---

  private def typeGlobal (td: TypeDefinition) : String = {
    val factory = td.factory
    val hasFactory = factory.valueType.nonEmpty
    val hasGlobalElements = td.globalElements.nonEmpty
    val objName = baseName(td.typeName.name)
    val wName = wildcardName(td.typeName.name)

    val header = s"object $objName extends App with TypeInstance"
    val tdLiteral = s"  lazy val typeDefinition: TypeDefinition = ${typeDefinitionFromJson(td)}"
    val tiLiteral = s"  lazy val typeInstance: Type[$wName] = Type[$wName] (typeDefinition)"

    if (!hasFactory && !hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |}""".stripMargin
    } else if (!hasFactory && hasGlobalElements) {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |
         |${globalElementsDeclaration(td.globalElements)}
         |}""".stripMargin
    } else {
      s"""$header {
         |$tdLiteral
         |$tiLiteral
         |
         |  def apply (${factoryParameters(factory.parameters)}) : ${factory.valueType} = new ${factory.valueType} ${factoryBody(factory)}
         |
         |  ${nullInstance(td.typeName, td.elements, td.factory)}
         |
         |  ${defaultInstance(td.typeName, td.factory)}
         |
         |${globalElementsDeclaration(td.globalElements)}
         |}""".stripMargin
    }
  }

  // --- Trait declaration helper ---

  private def traitDeclaration (td: TypeDefinition) : String =
    s"${typeModifier(td.modules)}trait ${td.typeName.name} ${typeExtends(td.derivation)} ${typeBody(td.elements)}"

  // --- DomainInstance companion generation ---

  private def domainInstanceLiteral (objName: String, dn: DomainName) : String = {
    val elementNames = if (dn.elementTypeNames.isEmpty) "Seq.empty"
    else {
      val names = dn.elementTypeNames.map(n => s"      \"$n\"")
      s"Seq (\n${names.mkString(",\n")}\n    )"
    }
    s"""  lazy val domainInstance: Domain[$objName] = Domain[$objName] (
       |    _domainName = DomainName (
       |      _typeName = typeDefinition.typeName,
       |      _elementTypeNames = $elementNames
       |    )
       |  )""".stripMargin
  }

  private def domainGlobal (td: TypeDefinition, dn: DomainName) : String = {
    val objName = baseName(td.typeName.name)
    val wName = wildcardName(td.typeName.name)
    val hasGlobalElements = td.globalElements.nonEmpty

    val header = s"object $objName extends App with DomainInstance"
    val tdLiteral = s"  lazy val typeDefinition: TypeDefinition = ${typeDefinitionFromJson(td)}"
    val tiLiteral = s"  lazy val typeInstance: Type[$wName] = Type[$wName] (typeDefinition)"
    val diLiteral = domainInstanceLiteral(objName, dn)
    val globals = if (hasGlobalElements) s"\n${globalElementsDeclaration(td.globalElements)}" else ""

    s"""$header {
       |$tdLiteral
       |$tiLiteral
       |
       |$diLiteral$globals
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

  // --- Main generate methods ---

  lazy val main: Main = Main.roots
  lazy val test: Test = Test.roots

  def generate (td: TypeDefinition) : String = {
    s"""
       |package ${td.typeName.namePackage.mkString(".")}
       |
       |${traitDeclaration(td)}
       |
       |${typeGlobal(td)}
       |""".stripMargin
  }

  def generate (td: TypeDefinition, dn: DomainName) : String = {
    s"""
       |package ${td.typeName.namePackage.mkString(".")}
       |
       |${traitDeclaration(td)}
       |
       |${domainGlobal(td, dn)}
       |""".stripMargin
  }

  def generate (typeDefinitions: Seq[TypeDefinition]) : String = {
    if (typeDefinitions.isEmpty) return ""
    if (typeDefinitions.size == 1) return generate(typeDefinitions.head)
    val ordered = moduleOrder(typeDefinitions)
    val pkg = ordered.head.typeName.namePackage.mkString(".")
    val typeBlocks = ordered.map { td =>
      s"${traitDeclaration(td)}\n\n${typeGlobal(td)}"
    }
    s"\npackage $pkg\n\n${typeBlocks.mkString("\n\n")}\n"
  }

  private def ruleImports (namePackage: Seq[String]) : String = {
    val packageImports = namePackage.inits.toSeq.tail.init.map(p => s"import ${p.mkString(".")}._")
    val allPackageImports = if (packageImports.contains("import draco._")) packageImports
      else "import draco._" +: packageImports
    val frameworkImports = Seq (
      "import io.circe.{Json, parser}",
      "import org.evrete.api.{Knowledge, RhsContext}",
      "import java.util.function.Consumer"
    )
    (allPackageImports ++ frameworkImports).mkString("\n")
  }

  def generate (rd: RuleDefinition) : String = {
    val name = rd.typeName.name
    s"""
       |package ${rd.typeName.namePackage.mkString(".")}
       |
       |${ruleImports(rd.typeName.namePackage)}
       |
       |trait $name extends RuleInstance
       |
       |object $name extends App with RuleInstance {
       |  private lazy val ruleDefinition: RuleDefinition = ${ruleDefinitionFromJson(rd)}
       |${conditionFunctions(rd.conditions)}
       |  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
       |${actionBody(rd.action, rd.variables, rd.values)}
       |  }
       |
       |  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
       |    knowledge
       |    .builder()
       |    .newRule ("${rd.typeName.fullName}")
       |    .forEach (
       |${factVariables(rd.variables)}
       |    )
       |${whereConditions(rd.conditions, rd.typeName)}
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
       |}
       |""".stripMargin
  }

  def apply (typeDictionary: TypeDictionary) : Generator = {
    new Generator {}
  }
}
