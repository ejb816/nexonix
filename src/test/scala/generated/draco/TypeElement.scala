
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

sealed trait TypeElement extends Primal[String] {
  val name: String
  val valueType: String
  val parameters: Seq[Parameter]
  val body: Seq[BodyElement]
}

object TypeElement extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("TypeElement", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeElement] = Type[TypeElement] (typeDefinition)

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
      case _: TypeElement => "TypeElement"
    }
    val fields = Seq(
      Some("kind" -> Json.fromString(kind)),
      if (x.name.nonEmpty) Some("name" -> x.name.asJson) else None,
      if (x.valueType.nonEmpty) Some("valueType" -> x.valueType.asJson) else None,
      if (x.parameters.nonEmpty) Some("parameters" -> x.parameters.asJson) else None,
      if (x.body.nonEmpty) Some("body" -> x.body.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeElement] = Decoder.instance { cursor =>
    cursor.downField("kind").as[String].flatMap {
      case "Fixed" =>
        for {
          _name <- cursor.downField("name").as[String]
          _valueType <- cursor.downField("valueType").as[String]
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Fixed (_name, _valueType, _value)

      case "Mutable" =>
        for {
          _name <- cursor.downField("name").as[String]
          _valueType <- cursor.downField("valueType").as[String]
          _value <- cursor.downField("value").as[Option[String]].map(_.getOrElse(""))
        } yield Mutable (_name, _valueType, _value)

      case "Dynamic" =>
        for {
          _name <- cursor.downField("name").as[String]
          _valueType <- cursor.downField("valueType").as[String]
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Dynamic (_name, _valueType, _parameters, _body)

      case "Parameter" =>
        for {
          _name <- cursor.downField("name").as[String]
          _valueType <- cursor.downField("valueType").as[String]
          _value <- cursor.downField("value").as[String]
        } yield Parameter (_name, _valueType, _value)

      case "Monadic" =>
        for {
          _value <- cursor.downField("value").as[String]
        } yield Monadic (_value)

      case "Pattern" =>
        for {
          _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
          _conditions <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
        } yield Pattern (_variables, _conditions)

      case "Action" =>
        for {
          _variables <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
          _values <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Action (_variables, _values, _body)

      case "Condition" =>
        for {
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _value <- cursor.downField("value").as[String]
        } yield Condition (_parameters, _value)

      case "Variable" =>
        for {
          _name <- cursor.downField("name").as[String]
          _valueType <- cursor.downField("valueType").as[String]
        } yield Variable (_name, _valueType)

      case "Factory" =>
        for {
          _valueType <- cursor.downField("valueType").as[String]
          _parameters <- cursor.downField("parameters").as[Option[Seq[Parameter]]].map(_.getOrElse(Seq.empty))
          _body <- cursor.downField("body").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
        } yield Factory (_valueType, _parameters, _body)

      case _ =>
        Right(TypeElement.Null)
    }
  }
}

sealed trait BodyElement extends TypeElement 

object BodyElement extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("BodyElement", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[BodyElement] = Type[BodyElement] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, BodyElement](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[BodyElement] = codec.encoder
  implicit def decoder: Decoder[BodyElement] = codec.decoder
}

trait Fixed extends BodyElement 

object Fixed extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Fixed", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Fixed] = Type[Fixed] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Fixed](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Fixed] = codec.encoder
  implicit def decoder: Decoder[Fixed] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: String = ""
  ) : Fixed = new Fixed {
    override val name: String = _name
    override val valueType: String = _valueType
    override val value: String = _value
    override lazy val typeInstance: DracoType = Fixed.typeInstance
    override lazy val typeDefinition: TypeDefinition = Fixed.typeDefinition
  }

  lazy val Null: Fixed = apply(
    _name = "",
    _valueType = "",
    _value = ""
  )


}

trait Mutable extends BodyElement 

object Mutable extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Mutable", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Mutable] = Type[Mutable] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Mutable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Mutable] = codec.encoder
  implicit def decoder: Decoder[Mutable] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: String = ""
  ) : Mutable = new Mutable {
    override val name: String = _name
    override val valueType: String = _valueType
    override val value: String = _value
    override lazy val typeInstance: DracoType = Mutable.typeInstance
    override lazy val typeDefinition: TypeDefinition = Mutable.typeDefinition
  }

  lazy val Null: Mutable = apply(
    _name = "",
    _valueType = "",
    _value = ""
  )


}

trait Dynamic extends BodyElement 

object Dynamic extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Dynamic", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Dynamic] = Type[Dynamic] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Dynamic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Dynamic] = codec.encoder
  implicit def decoder: Decoder[Dynamic] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ) : Dynamic = new Dynamic {
    override val name: String = _name
    override val valueType: String = _valueType
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = _body
    override lazy val typeInstance: DracoType = Dynamic.typeInstance
    override lazy val typeDefinition: TypeDefinition = Dynamic.typeDefinition
  }

  lazy val Null: Dynamic = apply(
    _name = "",
    _valueType = "",
    _parameters = Seq.empty,
    _body = Seq.empty
  )


}

trait Parameter extends BodyElement 

object Parameter extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Parameter", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Parameter] = Type[Parameter] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Parameter](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Parameter] = codec.encoder
  implicit def decoder: Decoder[Parameter] = codec.decoder

  def apply (
    _name: String,
    _valueType: String,
    _value: String
  ) : Parameter = new Parameter {
    override val name: String = _name
    override val valueType: String = _valueType
    override val value: String = _value
    override lazy val typeInstance: DracoType = Parameter.typeInstance
    override lazy val typeDefinition: TypeDefinition = Parameter.typeDefinition
  }

  lazy val Null: Parameter = apply(
    _name = "",
    _valueType = "",
    _value = ""
  )


}

trait Monadic extends BodyElement 

object Monadic extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Monadic", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Monadic] = Type[Monadic] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Monadic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Monadic] = codec.encoder
  implicit def decoder: Decoder[Monadic] = codec.decoder

  def apply (
    _value: String
  ) : Monadic = new Monadic {
    override val value: String = _value
    override lazy val typeInstance: DracoType = Monadic.typeInstance
    override lazy val typeDefinition: TypeDefinition = Monadic.typeDefinition
  }

  lazy val Null: Monadic = apply(
    _value = ""
  )


}

trait Pattern extends BodyElement {
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
}

object Pattern extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Pattern", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Pattern] = Type[Pattern] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Pattern](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Pattern] = codec.encoder
  implicit def decoder: Decoder[Pattern] = codec.decoder

  def apply (
    _variables: Seq[Variable] = Seq.empty,
    _conditions: Seq[Condition] = Seq.empty
  ) : Pattern = new Pattern {
    override val variables: Seq[Variable] = _variables
    override val conditions: Seq[Condition] = _conditions
    override lazy val typeInstance: DracoType = Pattern.typeInstance
    override lazy val typeDefinition: TypeDefinition = Pattern.typeDefinition
  }

  lazy val Null: Pattern = apply(
    _variables = Seq.empty,
    _conditions = Seq.empty
  )


}

trait Action extends BodyElement {
  val variables: Seq[Variable]
  val values: Seq[Value]
}

object Action extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Action", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Action] = Type[Action] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Action](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Action] = codec.encoder
  implicit def decoder: Decoder[Action] = codec.decoder

  def apply (
    _variables: Seq[Variable] = Seq.empty,
    _values: Seq[Value] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ) : Action = new Action {
    override val variables: Seq[Variable] = _variables
    override val values: Seq[Value] = _values
    override val body: Seq[BodyElement] = _body
    override lazy val typeInstance: DracoType = Action.typeInstance
    override lazy val typeDefinition: TypeDefinition = Action.typeDefinition
  }

  lazy val Null: Action = apply(
    _variables = Seq.empty,
    _values = Seq.empty,
    _body = Seq.empty
  )


}

trait Condition extends BodyElement 

object Condition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Condition", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Condition] = Type[Condition] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Condition](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Condition] = codec.encoder
  implicit def decoder: Decoder[Condition] = codec.decoder

  def apply (
    _parameters: Seq[Parameter] = Seq.empty,
    _value: String
  ) : Condition = new Condition {
    override val parameters: Seq[Parameter] = _parameters
    override val value: String = _value
    override lazy val typeInstance: DracoType = Condition.typeInstance
    override lazy val typeDefinition: TypeDefinition = Condition.typeDefinition
  }

  lazy val Null: Condition = apply(
    _parameters = Seq.empty,
    _value = ""
  )


}

trait Variable extends BodyElement 

object Variable extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Variable", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Variable] = Type[Variable] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Variable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Variable] = codec.encoder
  implicit def decoder: Decoder[Variable] = codec.decoder

  def apply (
    _name: String,
    _valueType: String
  ) : Variable = new Variable {
    override val name: String = _name
    override val valueType: String = _valueType
    override lazy val typeInstance: DracoType = Variable.typeInstance
    override lazy val typeDefinition: TypeDefinition = Variable.typeDefinition
  }

  lazy val Null: Variable = apply(
    _name = "",
    _valueType = ""
  )


}

trait Factory extends BodyElement 

object Factory extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Factory", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Factory] = Type[Factory] (typeDefinition)

  private lazy val codec = Codec.sub[TypeElement, Factory](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Factory] = codec.encoder
  implicit def decoder: Decoder[Factory] = codec.decoder

  def apply (
    _valueType: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ) : Factory = new Factory {
    override val valueType: String = _valueType
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = _body
    override lazy val typeInstance: DracoType = Factory.typeInstance
    override lazy val typeDefinition: TypeDefinition = Factory.typeDefinition
  }

  lazy val Null: Factory = apply(
    _valueType = "",
    _parameters = Seq.empty,
    _body = Seq.empty
  )


}
