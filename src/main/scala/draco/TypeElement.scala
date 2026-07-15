package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

sealed trait TypeElement extends Primal[Json] {
  val name: String
  val valueType: String
  lazy val parameters: Seq[Parameter] = Seq.empty
  lazy val body: Seq[BodyElement] = Seq.empty
  lazy val value: Json = Json.Null
}

object TypeElement extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TypeElement", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeElement] = Type[TypeElement] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeElement] = Encoder.instance { x =>
    val kind = x match {
      case _: Fixed => "Fixed"
      case _: Mutable => "Mutable"
      case _: Dynamic => "Dynamic"
      case _: Parameter => "Parameter"
      case _: Monadic => "Monadic"
      case _: Pattern => "Pattern"
      case _: Action => "Action"
      case _: Condition => "Condition"
      case _: Variable => "Variable"
      case _: Factory => "Factory"
      case _: Local => "Local"
    }
    val fields = Seq(
      Some("kind" -> Json.fromString(kind)),
      if (x.name.nonEmpty) Some("name" -> x.name.asJson) else None,
      if (x.valueType.nonEmpty) Some("valueType" -> x.valueType.asJson) else None,
      if (x.parameters.nonEmpty) Some("parameters" -> x.parameters.asJson) else None,
      if (x.body.nonEmpty) Some("body" -> x.body.asJson) else None,
      if (!x.value.isNull && x.value.asString.forall(_.nonEmpty)) Some("value" -> x.value.asJson) else None
    ).flatten ++ (x match {
      case x: Pattern => Seq(
        if (x.variables.nonEmpty) Some("variables" -> x.variables.asJson) else None,
        if (x.conditions.nonEmpty) Some("conditions" -> x.conditions.asJson) else None
      ).flatten
      case x: Action => Seq(
        if (x.variables.nonEmpty) Some("variables" -> x.variables.asJson) else None
      ).flatten
      case _ => Seq.empty
    })
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeElement] = Decoder.instance { cursor =>
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Fixed (_name, _valueType, _value)

      case "Mutable" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Mutable (_name, _valueType, _value)

      case "Dynamic" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Dynamic (_name, _valueType, _parameters, _body, _value)

      case "Parameter" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Parameter (_name, _valueType, _value)

      case "Monadic" =>
        for {
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Monadic (_value)

      case "Pattern" =>
        for {
          _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
          _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
        } yield Pattern (_variables, _conditions)

      case "Action" =>
        for {
          _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Action (_variables, _body)

      case "Condition" =>
        for {
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Condition (_parameters, _value)

      case "Variable" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
        } yield Variable (_name, _valueType)

      case "Factory" =>
        for {
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Factory (_valueType, _parameters, _body)

      case "Local" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[Json]].map(_.getOrElse(Json.Null))
        } yield Local (_name, _valueType, _value)

      case other =>
        Left(io.circe.DecodingFailure(s"Unknown TypeElement kind: $other", cursor.history))
    }
  }
}

sealed trait BodyElement extends TypeElement

object BodyElement extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("BodyElement", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[BodyElement] = Type[BodyElement] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, BodyElement](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[BodyElement] = codec.encoder
  implicit def decoder: Decoder[BodyElement] = codec.decoder
}

trait Fixed extends BodyElement

object Fixed extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Fixed", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Fixed] = Type[Fixed] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Fixed](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Fixed] = codec.encoder
  implicit def decoder: Decoder[Fixed] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: Json = Json.Null
  ) : Fixed = new Fixed {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Fixed.typeDefinition
  }

  lazy val Null: Fixed = apply(
    _name = "",
    _valueType = "",
    _value = Json.Null
  )

}

trait Mutable extends BodyElement

object Mutable extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Mutable", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Mutable] = Type[Mutable] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Mutable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Mutable] = codec.encoder
  implicit def decoder: Decoder[Mutable] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: Json = Json.Null
  ) : Mutable = new Mutable {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Mutable.typeDefinition
  }

  lazy val Null: Mutable = apply(
    _name = "",
    _valueType = "",
    _value = Json.Null
  )

}

trait Dynamic extends BodyElement

object Dynamic extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Dynamic", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Dynamic] = Type[Dynamic] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Dynamic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Dynamic] = codec.encoder
  implicit def decoder: Decoder[Dynamic] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty,
    _value: Json = Json.Null
  ) : Dynamic = new Dynamic {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val parameters: Seq[Parameter] = _parameters
    override lazy val body: Seq[BodyElement] = _body
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Dynamic.typeDefinition
  }

  lazy val Null: Dynamic = apply(
    _name = "",
    _valueType = "",
    _parameters = Seq.empty,
    _body = Seq.empty,
    _value = Json.Null
  )

}

trait Parameter extends BodyElement

object Parameter extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Parameter", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Parameter] = Type[Parameter] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Parameter](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Parameter] = codec.encoder
  implicit def decoder: Decoder[Parameter] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: Json
  ) : Parameter = new Parameter {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Parameter.typeDefinition
  }

  lazy val Null: Parameter = apply(
    _name = "",
    _valueType = "",
    _value = Json.Null
  )

}

trait Monadic extends BodyElement

object Monadic extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Monadic", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Monadic] = Type[Monadic] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Monadic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Monadic] = codec.encoder
  implicit def decoder: Decoder[Monadic] = codec.decoder

  def apply (
    _value: Json
  ) : Monadic = new Monadic {
    override lazy val name: String = ""
    override lazy val valueType: String = "Unit"
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Monadic.typeDefinition
  }

  lazy val Null: Monadic = apply(
    _value = Json.Null
  )

}

trait Pattern extends BodyElement {
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
}

object Pattern extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Pattern", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Pattern] = Type[Pattern] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Pattern](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Pattern] = codec.encoder
  implicit def decoder: Decoder[Pattern] = codec.decoder

  def apply (
    _variables: Seq[Variable] = Seq.empty,
    _conditions: Seq[Condition] = Seq.empty
  ) : Pattern = new Pattern {
    override lazy val variables: Seq[Variable] = _variables
    override lazy val conditions: Seq[Condition] = _conditions
    override lazy val name: String = ""
    override lazy val valueType: String = "org.evrete.api.Knowledge => Unit"
    override lazy val typeDefinition: TypeDefinition = Pattern.typeDefinition
  }

  lazy val Null: Pattern = apply()

}

trait Action extends BodyElement {
  val variables: Seq[Variable]
}

object Action extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Action", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Action] = Type[Action] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Action](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Action] = codec.encoder
  implicit def decoder: Decoder[Action] = codec.decoder

  def apply (
    _variables: Seq[Variable] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ) : Action = new Action {
    override lazy val variables: Seq[Variable] = _variables
    override lazy val body: Seq[BodyElement] = _body
    override lazy val name: String = "ctx"
    override lazy val valueType: String = "org.evrete.api.RhsContext => Unit"
    override lazy val typeDefinition: TypeDefinition = Action.typeDefinition
  }

  lazy val Null: Action = apply()

}

trait Condition extends BodyElement

object Condition extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Condition", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Condition] = Type[Condition] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Condition](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Condition] = codec.encoder
  implicit def decoder: Decoder[Condition] = codec.decoder

  def apply (
    _parameters: Seq[Parameter] = Seq.empty,
    _value: Json
  ) : Condition = new Condition {
    override lazy val parameters: Seq[Parameter] = _parameters
    override lazy val value: Json = _value
    override lazy val name: String = ""
    override lazy val valueType: String = "Boolean"
    override lazy val typeDefinition: TypeDefinition = Condition.typeDefinition
  }

  lazy val Null: Condition = apply(
    _parameters = Seq.empty,
    _value = Json.Null
  )

}

trait Variable extends BodyElement

object Variable extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Variable", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Variable] = Type[Variable] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Variable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Variable] = codec.encoder
  implicit def decoder: Decoder[Variable] = codec.decoder

  def apply (
    _name: String,
    _valueType: String
  ) : Variable = new Variable {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val typeDefinition: TypeDefinition = Variable.typeDefinition
  }

  lazy val Null: Variable = apply(
    _name = "",
    _valueType = ""
  )

}

trait Factory extends BodyElement

object Factory extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Factory", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Factory] = Type[Factory] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Factory](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Factory] = codec.encoder
  implicit def decoder: Decoder[Factory] = codec.decoder

  def apply (
    _valueType: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ) : Factory = new Factory {
    override lazy val valueType: String = _valueType
    override lazy val parameters: Seq[Parameter] = _parameters
    override lazy val body: Seq[BodyElement] = _body
    override lazy val name: String = ""
    override lazy val typeDefinition: TypeDefinition = Factory.typeDefinition
  }

  lazy val Null: Factory = apply(
    _valueType = "",
    _parameters = Seq.empty,
    _body = Seq.empty
  )

}

trait Local extends BodyElement

object Local extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Local", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Local] = Type[Local] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Local](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Local] = codec.encoder
  implicit def decoder: Decoder[Local] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: Json
  ) : Local = new Local {
    override lazy val name: String = _name
    override lazy val valueType: String = _valueType
    override lazy val value: Json = _value
    override lazy val typeDefinition: TypeDefinition = Local.typeDefinition
  }

  lazy val Null: Local = apply(
    _name = "",
    _valueType = "",
    _value = Json.Null
  )

}
