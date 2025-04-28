package draco.domain

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

import java.util.regex.Matcher

sealed trait TypeDefinition {
  val typeName: TypeName
  val typeParameters: Seq[String]
  val dependsOn: Seq[TypeName]
  val derivesFrom: Seq[TypeName]
  val members: Seq[Member]
  val parameters: Seq[Parameter]
  val rules: Seq[TypeName]
}

object TypeDefinition {
  val Null: TypeDefinition = TypeDefinition (
    _typeName = TypeName.Null
  )
  def apply (
              _typeName: TypeName,
              _typeParameters: Seq[String] = Seq[String](),
              _dependsOn: Seq[TypeName] = Seq[TypeName](),
              _derivesFrom: Seq[TypeName] = Seq[TypeName](),
              _members: Seq[Member] = Seq[Member](),
              _parameters: Seq[Parameter] = Seq[Parameter](),
              _rules: Seq[TypeName] = Seq[TypeName]()
            ) : TypeDefinition = {
    new TypeDefinition {
      override val typeName: TypeName = _typeName
      override val typeParameters: Seq[String] = _typeParameters
      override val dependsOn: Seq[TypeName] = _dependsOn
      override val derivesFrom: Seq[TypeName] = _derivesFrom
      override val members: Seq[Member] = _members
      override val parameters: Seq[Parameter] = _parameters
      override val rules: Seq[TypeName] = _rules
    }
  }
  // Encode a TypeDefinition
  implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    Json.obj(
      "typeName"       -> td.typeName.asJson,       // TypeName
      "typeParameters" -> td.typeParameters.asJson, // Seq[String]
      "dependsOn"      -> td.dependsOn.asJson,      // Seq[TypeName]
      "derivesFrom"    -> td.derivesFrom.asJson,    // Seq[TypeName]
      "members"        -> td.members.asJson,        // Seq[Member]
      "parameters"     -> td.parameters.asJson,     // Seq[Parameter]
      "rules"          -> td.rules.asJson           // Seq[Rule]
    )
  }

  implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName        <- cursor.downField("typeName").as[TypeName]
      _typeParameters  <- cursor.downField("typeParameters").as[Seq[String]]
      _dependsOn       <- cursor.downField("dependsOn").as[Seq[TypeName]]
      _derivesFrom     <- cursor.downField("derivesFrom").as[Seq[TypeName]]
      _members         <- cursor.downField("members").as[Seq[Member]]
      _parameters      <- cursor.downField("parameters").as[Seq[Parameter]]
      _rules           <- cursor.downField("rules").as[Seq[TypeName]]
    } yield TypeDefinition (
      _typeName,
      _typeParameters,
      _dependsOn,
      _derivesFrom,
      _members,
      _parameters,
      _rules
    )
  }

  def generateRule (
                     ruleDef: Rule,
                     namePackage: Seq[String],
                     dependsOn: Seq[TypeName]
                   ): String = {
    val variables = ruleDef.variables.map { case (key, value) =>
      ("$" + key) -> value
    }

    val depends: Seq[TypeName] => String = d => {
      if (d.isEmpty) "" else s"import ${d.map(f => f.fullName).mkString("\nimport ")}"
    }

    // Create a forEach call with all variables
    val variableString = if (ruleDef.variables.nonEmpty) {
      val varParams = ruleDef.variables.flatMap { case (key, value) =>
        Seq(s"\"$$${key}\"", s"classOf[$value]")
      }.mkString(", ")
      s".forEach($varParams)"
    } else {
      ""
    }

    val replacedConditions = ruleDef.conditions.map { condition =>
      variables.foldLeft(condition) { (updatedCondition, variable) =>
        // Updated regular expression with negative lookbehind
        updatedCondition.replaceAll(s"(?<!\\.)\\b${variable._1.drop(1)}\\b", Matcher.quoteReplacement(variable._1))
      }
    }

    val conditionsString = replacedConditions.map { condition =>
      s"\t.where(\"${condition.replaceAll("\"", "\\\\\"")}\")"
    }.mkString("\n")

    // Find JSON variables that could be used as sources for Value objects
    val jsonVariables = ruleDef.variables.filter { case (_, varType) =>
      varType == "Json" || varType.endsWith(".Json") || varType == "io.circe.Json"
    }

    val valuePathDeclarations = ruleDef.values.map { case (valueName, pathElements) =>
      if (jsonVariables(pathElements.head).isEmpty) {
        val quotedPaths = "Seq(" + pathElements.map(item => "\"" + item + "\"").mkString(", ") + ")"
        s"val $valueName: Value = Value(\"$valueName\", Json.Null, $quotedPaths)"
      } else {
        val quotedPaths = "Seq(" + pathElements.tail.map(item => "\"" + item + "\"").mkString(", ") + ")"
        s"val $valueName: Value = Value(\"$valueName\", ${pathElements.head}, $quotedPaths)"
      }
    }

    val bodyPrepend = variables.map { variable =>
      s"val ${variable._1.drop(1)}: ${variable._2} = context.get[${variable._2}](${"\"" + variable._1 + "\""})"
    }

    val body = (bodyPrepend ++ valuePathDeclarations ++ ruleDef.action).mkString("\n\t\t")
    val functionBody = s"(context: RhsContext) => {\n\t\t$body\n}"

    s"""
       |package ${namePackage.mkString(".")}
       |
       |import draco.domain.DomainRule
       |import org.evrete.api.Knowledge
       |import org.evrete.api.RhsContext
       |${depends(dependsOn)}
       |
       |trait ${ruleDef.name} extends DomainRule
       |
       |object ${ruleDef.name} {
       |
       |\tval rule: Knowledge => Unit = knowledge => {
       |\t\tknowledge
       |\t\t.builder()
       |\t\t.newRule ("${ruleDef.name}")
       |\t\t$variableString
       |\t\t$conditionsString
       |
       |\t\t.execute($functionBody)
       |\t\t.build()
       |\t}
       |}
       |""".stripMargin
  }

  def generate (td: TypeDefinition, closed: Boolean = false) : String = {
    val namePackage = td.typeName.namePackage
    val sealedType = if (closed) "sealed " else ""
    val derived: Seq[TypeName] => String = d => {
      if (d.isEmpty) "" else "extends " + d.map(f => f.name).mkString(" with ")
    }
    val depends: Seq[TypeName] => String = d => {
      if (d.isEmpty) "" else s"import ${d.map(f => f.fullName).mkString("\nimport ")}"
    }
    def kind: ((Member, String)) => String = {
      case (m: Fixed, s) =>
        s"${s}val ${m.aName}: ${m.aType}${if (m.aValue.isEmpty) "" else s" = ${m.aValue}"}"
      case (m: Mutable, s) =>
        s"${s}var ${m.aName}: ${m.aType}${if (m.aValue.isEmpty) "" else s" = ${m.aValue}"}"
      case (m: Dynamic, s) =>
        s"${s}def ${m.aName}: ${m.aType}${if (m.aValue.isEmpty) "" else s" = ${m.aValue}"}"
      case (m: Parameter, s) =>
        s"${s}_${m.aName}: ${m.aType}${if (m.aValue.isEmpty) "" else s" = ${m.aValue}"}"
      case (m: Member, s) =>
        s"${s}val ${m.aName}: ${m.aType}${if (m.aValue.isEmpty) "" else s" = ${m.aValue}"}"
    }

    def prepend(prefix: String): Member => (Member, String) = m => (m, prefix)

    val prependMembers: Seq[(Member, String)] = td.members.map(prepend("  "))
    val typeMembers: Seq[String] = prependMembers.map(kind)
    val typeBody = if (td.members.isEmpty) "" else
      s"""{
         |\t${typeMembers.mkString("\n")}
         |}""".stripMargin

    val typeParameters = if (td.typeParameters.isEmpty) "" else s"[${td.typeParameters.mkString(",")}]"

    val prependParameters: Seq[(Member, String)] = td.parameters.map(prepend("    "))
    val parameterMembers: Seq[String] = prependParameters.map[String](kind)
    val parameterList = if (parameterMembers.isEmpty) "" else
      s"""
         |\t${parameterMembers.mkString(",\n")}
         |""".stripMargin

    val prependApply: Seq[(Member, String)] = td.members.map(prepend("  override "))
    val applyMembers: Seq[String] = prependApply.map(kind)
    val applyBody = if (td.members.isEmpty) "" else
      s"""
         |{
         |\t${applyMembers.mkString("\n")}
         |}
         |""".stripMargin
    val ruleSet =
      s"""
         |val ruleSet: DomainRuleSet = Seq[DomainRule]()
         ||""".stripMargin
    s"""
       |package ${namePackage.mkString(".")}
       |
       |${depends(td.dependsOn)}
       |
       |${sealedType}trait ${td.typeName.name}$typeParameters ${derived(td.derivesFrom)} $typeBody
       |
       |object ${td.typeName.name} {
       |def apply$typeParameters ($parameterList) : ${td.typeName.name} = new ${td.typeName.name} $applyBody
       |
       |$ruleSet
       |}
       |""".stripMargin
  }
}