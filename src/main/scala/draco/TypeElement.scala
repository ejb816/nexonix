package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import org.nexonix.domains.Primal
import org.nexonix.format.json.Codec

sealed trait TypeElement extends Primal[String] {
  val name: String
  val valueType: String
  val parameters: Seq[Parameter] = Seq ()
  val body: Seq[BodyElement] = Seq ()
}

sealed trait BodyElement extends TypeElement

object BodyElement {
  private lazy val codec = Codec.sub[TypeElement, BodyElement](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[BodyElement] = codec.encoder
  implicit def decoder: Decoder[BodyElement] = codec.decoder
}

object TypeElement {
  def apply (
              _name: String,
              _valueType: String,
              _value: String
            ) : TypeElement = new TypeElement {
    override val name: String = _name
    override val valueType: String = _valueType
    override val value: String = _value
  }
  lazy val Null: TypeElement = TypeElement (_name = "", _valueType = "", _value = "")
  implicit lazy val encoder: Encoder[TypeElement] = Encoder.instance { te =>
    val kind = te match {
      case _: Fixed => "Fixed"
      case _: Mutable => "Mutable"
      case _: Dynamic => "Dynamic"
      case _: Parameter => "Parameter"
      case _: Action => "Action"
      case _: Condition => "Condition"
      case _: Variable => "Variable"
      case _: Factory => "Factory"
      case _: Monadic => "Monadic"
      case _: TypeElement => "TypeElement"
    }

    val fields = Seq(
      Some("kind" -> Json.fromString(kind)),
      if (te.name.nonEmpty) Some("name" -> te.name.asJson) else None,
      if (te.valueType.nonEmpty) Some("valueType" -> te.valueType.asJson) else None,
      if (te.value.nonEmpty) Some("value" -> te.value.asJson) else None,
      if (te.parameters.nonEmpty) Some("parameters" -> te.parameters.asJson) else None,
      if (te.body.nonEmpty) Some("body" -> te.body.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  implicit lazy val decoder: Decoder[TypeElement] = Decoder.instance { cursor =>
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Fixed (_name, _valueType, _value)

      case "Mutable" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Mutable (_name, _valueType, _value)

      case "Dynamic" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Dynamic(_name, _valueType, _parameters, _body)

      case "Parameter" =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Parameter (_name, _valueType, _value)

      case "Action" =>
        for {
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Action (_parameters, _body)

      case "Condition" =>
        for {
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
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

      case "Monadic" =>
        for {
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Monadic(_value)

      case _ =>
        for {
          _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
          _valueType <- cursor.downField("valueType").as[Option[String]].map(_.getOrElse(""))
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield TypeElement (_name, _valueType, _value)

    }
  }
}


sealed trait Fixed extends BodyElement
object Fixed {
  def apply (
      _name: String,
      _valueType: String,
      _value: String = ""
    ) : Fixed = {
    new Fixed {
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
    }
  }
}

sealed trait Mutable extends BodyElement
object Mutable {
  def apply (
              _name: String,
              _valueType: String,
              _value: String = ""
            ) : Mutable = {
    new Mutable {
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
    }
  }
}

sealed trait Dynamic extends BodyElement
object Dynamic {
  def apply (
              _name: String,
              _valueType: String,
              _parameters: Seq[Parameter],
              _body: Seq[BodyElement]
            ) : Dynamic = {
    new Dynamic {
      override val name: String = _name
      override val valueType: String = _valueType
      override val parameters: Seq[Parameter] = _parameters
      override val body: Seq[BodyElement] = _body
      override val value: String = ""
    }
  }
}

sealed trait Parameter extends BodyElement
object Parameter {
  def apply (
              _name: String,
              _valueType: String,
              _value: String
            ) : Parameter = {
    new Parameter {
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
    }
  }

  private lazy val codec = Codec.sub[TypeElement, Parameter](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Parameter] = codec.encoder
  implicit def decoder: Decoder[Parameter] = codec.decoder
}

sealed trait Monadic extends BodyElement
object Monadic {
  def apply(_value: String): Monadic = new Monadic {
    override val name: String = ""
    override val valueType: String = "Unit"
    override val value: String = _value
  }

  lazy val Null: Monadic = Monadic("")
  private lazy val codec = Codec.sub[TypeElement, Monadic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Monadic] = codec.encoder
  implicit def decoder: Decoder[Monadic] = codec.decoder
}

sealed trait Action extends BodyElement
object Action {
  def apply (
            _parameters: Seq[Parameter],
            _body: Seq[BodyElement]
            ) : Action = new Action {
    override val name: String = ""
    override val valueType: String = "Unit"
    override val value: String = ""
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = _body
  }
  lazy val Null: Action = Action (_parameters = Seq.empty, _body = Seq.empty)
  private lazy val codec = Codec.sub[TypeElement, Action](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Action] = codec.encoder
  implicit def decoder: Decoder[Action] = codec.decoder
}
sealed trait Condition extends BodyElement

object Condition {
  def apply (
              _parameters: Seq[Parameter],
              _value: String
            ) : Condition = new Condition {
    override val name: String = ""
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = Seq ()
    override val valueType: String = "Boolean"
    override val value: String = _value
  }

  lazy val Null: Condition = Condition (_parameters = Seq.empty, _value = "")
  private lazy val codec = Codec.sub[TypeElement, Condition](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Condition] = codec.encoder
  implicit def decoder: Decoder[Condition] = codec.decoder
}


sealed trait Variable extends BodyElement

object Variable {
  def apply (
              _variableName: String,
              _variableType: String
            ) : Variable = new Variable {
    override val name: String = _variableName
    override val valueType: String = _variableType
    override val value: String = ""
    override val parameters: Seq[Parameter] = Seq ()
    override val body: Seq[BodyElement] = Seq ()
  }
  lazy val Null: Variable = Variable (_variableName = "", _variableType = "")
  private lazy val codec = Codec.sub[TypeElement, Variable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Variable] = codec.encoder
  implicit def decoder: Decoder[Variable] = codec.decoder
}

sealed trait Factory extends BodyElement

object Factory {
  def apply(
    _fullName: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ): Factory = new Factory {
    override val name: String = ""
    override val valueType: String = _fullName
    override val value: String = ""
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = _body
  }

  lazy val Null: Factory = Factory("")

  private lazy val codec = Codec.sub[TypeElement, Factory](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Factory] = codec.encoder
  implicit def decoder: Decoder[Factory] = codec.decoder
}
