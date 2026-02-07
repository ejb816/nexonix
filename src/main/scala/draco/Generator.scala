package draco

trait Generator {

}

object Generator extends App {
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

  lazy val main: Main = Main.roots
  lazy val test: Test = Test.roots
  def generate (
                 rd: RuleDefinition
               ): String = {
    s"""
       |package ${rd.typeName.namePackage.mkString(".")}
       |
       |trait ${rd.typeName.name} extends draco.Rule
       |
       |object ${rd.typeName.name} {
       |${conditionFunctions(rd.conditions)}
       |  val action: java.util.function.Consumer[org.evrete.api.RhsContext] = (ctx: org.evrete.api.RhsContext) => {
       |${actionBody(rd.action, rd.variables, rd.values)}
       |  }
       |
       |  val pattern: org.evrete.api.Knowledge => Unit = knowledge => {
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
       |}
       |""".stripMargin
  }
  private def typeModifier (
    modules: Seq[TypeName]
  ) : String = {
    if (modules.isEmpty) "" else "sealed "
  }

  private def typeExtends (
    derivation: Seq[TypeName]
  ) : String = {
    if (derivation.isEmpty) "" else "extends " + derivation.map(_.name).mkString(" with ")
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
    elements: Seq[BodyElement]
  ) : String = {
    if (elements.isEmpty) ""
    else {
      val overrides = elements.map {
        case f: Fixed =>
          val init = if (f.value.isEmpty) "" else s" = ${f.value}"
          s"    override val ${f.name}: ${f.valueType}$init"
        case m: Mutable =>
          val init = if (m.value.isEmpty) "" else s" = ${m.value}"
          s"    override var ${m.name}: ${m.valueType}$init"
        case d: Dynamic =>
          val init = if (d.value.isEmpty) "" else s" = ${d.value}"
          s"    override def ${d.name}: ${d.valueType}$init"
        case p: Parameter =>
          val init = if (p.value.isEmpty) "" else s" = ${p.value}"
          s"    override val ${p.name}: ${p.valueType}$init"
        case be: BodyElement =>
          val init = if (be.value.isEmpty) "" else s" = ${be.value}"
          s"    override val ${be.name}: ${be.valueType}$init"
      }
      s"{\n${overrides.mkString("\n")}\n  }"
    }
  }

  private def ruleSetDeclaration (
    rules: Seq[TypeName]
  ) : String = {
    if (rules.isEmpty) "val ruleSet: Seq[Knowledge => Unit] = Seq()"
    else {
      val ruleRefs = rules.map(_.fullName + ".rule")
      s"val ruleSet: Seq[Knowledge => Unit] = Seq(\n    ${ruleRefs.mkString(",\n    ")}\n  )"
    }
  }

  private def typeGlobalsDeclaration (
    typeGlobals: Seq[BodyElement]
  ) : String = {
    if (typeGlobals.isEmpty) ""
    else {
      val globals = typeGlobals.map {
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

  private def nullInstance (
    typeName: TypeName,
    elements: Seq[TypeElement]
  ) : String = {
    if (elements.isEmpty) s"lazy val Null: ${typeName.name} = new ${typeName.name} {}"
    else {
      val nullMembers = elements.map {
        case f: Fixed => s"    override val ${f.name}: ${f.valueType} = null.asInstanceOf[${f.valueType}]"
        case m: Mutable => s"    override var ${m.name}: ${m.valueType} = null.asInstanceOf[${m.valueType}]"
        case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = null.asInstanceOf[${d.valueType}]"
        case p: Parameter => s"    override val ${p.name}: ${p.valueType} = null.asInstanceOf[${p.valueType}]"
        case te: TypeElement => s"    override val ${te.name}: ${te.valueType} = null.asInstanceOf[${te.valueType}]"
      }
      s"lazy val Null: ${typeName.name} = new ${typeName.name} {\n${nullMembers.mkString("\n")}\n  }"
    }
  }

  private def defaultInstance (
    typeName: TypeName,
    factory: Factory
  ) : String = {
    val specifiedParams = factory.parameters.filter(_.value.nonEmpty)
    if (specifiedParams.isEmpty) s"lazy val Default: ${typeName.name} = apply()"
    else {
      val paramAssignments = specifiedParams.map { p =>
        s"    _${p.name} = ${p.value}"
      }
      s"lazy val Default: ${typeName.name} = apply(\n${paramAssignments.mkString(",\n")}\n  )"
    }
  }

  private def companionObject (td: TypeDefinition) : String = {
    val factory = td.factory
    val hasFactory = factory.valueType.nonEmpty
    val hasGlobals = td.typeGlobals.nonEmpty

    if (!hasFactory && !hasGlobals) {
      s"object ${td.typeName.name}"
    } else if (!hasFactory && hasGlobals) {
      s"""object ${td.typeName.name} {
         |${typeGlobalsDeclaration(td.typeGlobals)}
         |}""".stripMargin
    } else {
      s"""object ${td.typeName.name} {
         |  def apply (${factoryParameters(factory.parameters)}) : ${factory.valueType} = new ${factory.valueType} ${factoryBody(factory.body)}
         |
         |  ${nullInstance(td.typeName, td.elements)}
         |
         |  ${defaultInstance(td.typeName, td.factory)}
         |
         |${typeGlobalsDeclaration(td.typeGlobals)}
         |
         |  ${ruleSetDeclaration(td.rules)}
         |}""".stripMargin
    }
  }

  def generate (td: TypeDefinition) : String = {
    s"""
       |package ${td.typeName.namePackage.mkString(".")}
       |
       |import org.evrete.api.Knowledge
       |
       |${typeModifier(td.modules)}trait ${td.typeName.name} ${typeExtends(td.derivation)} ${typeBody(td.elements)}
       |
       |${companionObject(td)}
       |""".stripMargin
  }
  def apply (typeDictionary: TypeDictionary) : Generator = {
    new Generator {}
  }
}