package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeElement extends Primal[String] {
  val name: String
  val valueType: String
  val parameters: Seq[Parameter] = Seq ()
  val body: Seq[BodyElement] = Seq ()
}

sealed trait BodyElement extends TypeElement

object BodyElement extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "BodyElement",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("TypeElement", _namePackage = Seq ("draco"))
    ),
    _modules = Seq (
      TypeName ("Fixed", _namePackage = Seq ("draco")),
      TypeName ("Mutable", _namePackage = Seq ("draco")),
      TypeName ("Dynamic", _namePackage = Seq ("draco")),
      TypeName ("Parameter", _namePackage = Seq ("draco")),
      TypeName ("Monadic", _namePackage = Seq ("draco")),
      TypeName ("Pattern", _namePackage = Seq ("draco")),
      TypeName ("Action", _namePackage = Seq ("draco")),
      TypeName ("Condition", _namePackage = Seq ("draco")),
      TypeName ("Variable", _namePackage = Seq ("draco")),
      TypeName ("Factory", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[BodyElement] = Type[BodyElement] (typeDefinition)
  private lazy val codec = Codec.sub[TypeElement, BodyElement](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[BodyElement] = codec.encoder
  implicit def decoder: Decoder[BodyElement] = codec.decoder
}

object TypeElement extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeElement",
      _namePackage = Seq ("draco")
    ),
    _modules = Seq (
      TypeName ("BodyElement", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      Fixed ("name", "String"),
      Fixed ("valueType", "String"),
      Fixed ("parameters", "Seq[Parameter]"),
      Fixed ("body", "Seq[BodyElement]")
    )
  )
  lazy val typeInstance: Type[TypeElement] = Type[TypeElement] (typeDefinition)

  def apply (
              _name: String,
              _valueType: String,
              _value: String
            ) : TypeElement = new TypeElement {
    override lazy val typeInstance: DracoType = TypeElement.typeInstance
    override val name: String = _name
    override val valueType: String = _valueType
    override val value: String = _value
    override val typeDefinition: TypeDefinition = TypeElement.typeDefinition
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
      case _: Pattern => "Pattern"
      case _: TypeElement => "TypeElement"
    }

    val fields = Seq (
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

      case "Pattern" =>
        for {
          _variables  <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
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
object Fixed extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Fixed", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Fixed", _parameters = Seq (
      Parameter ("name", "String", ""),
      Parameter ("valueType", "String", ""),
      Parameter ("value", "String", "\"\"")
    ))
  )
  lazy val typeInstance: Type[Fixed] = Type[Fixed] (typeDefinition)
  def apply (
      _name: String,
      _valueType: String,
      _value: String = ""
    ) : Fixed = {
    new Fixed {
      override lazy val typeInstance: DracoType = Fixed.typeInstance
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
      override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
    }
  }
}

sealed trait Mutable extends BodyElement
object Mutable extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Mutable", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Mutable", _parameters = Seq (
      Parameter ("name", "String", ""),
      Parameter ("valueType", "String", ""),
      Parameter ("value", "String", "\"\"")
    ))
  )
  lazy val typeInstance: Type[Mutable] = Type[Mutable] (typeDefinition)
  def apply (
              _name: String,
              _valueType: String,
              _value: String = ""
            ) : Mutable = {
    new Mutable {
      override lazy val typeInstance: DracoType = Mutable.typeInstance
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
      override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
    }
  }
}

sealed trait Dynamic extends BodyElement
object Dynamic extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Dynamic", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Dynamic", _parameters = Seq (
      Parameter ("name", "String", ""),
      Parameter ("valueType", "String", ""),
      Parameter ("parameters", "Seq[Parameter]", "Seq.empty"),
      Parameter ("body", "Seq[BodyElement]", "Seq.empty")
    ))
  )
  lazy val typeInstance: Type[Dynamic] = Type[Dynamic] (typeDefinition)
  def apply (
              _name: String,
              _valueType: String,
              _parameters: Seq[Parameter],
              _body: Seq[BodyElement]
            ) : Dynamic = {
    new Dynamic {
      override lazy val typeInstance: DracoType = Dynamic.typeInstance
      override val name: String = _name
      override val valueType: String = _valueType
      override val parameters: Seq[Parameter] = _parameters
      override val body: Seq[BodyElement] = _body
      override val value: String = ""
      override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
    }
  }
}

sealed trait Parameter extends BodyElement
object Parameter extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Parameter", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Parameter", _parameters = Seq (
      Parameter ("name", "String", ""),
      Parameter ("valueType", "String", ""),
      Parameter ("value", "String", "")
    ))
  )
  lazy val typeInstance: Type[Parameter] = Type[Parameter] (typeDefinition)
  def apply (
              _name: String,
              _valueType: String,
              _value: String
            ) : Parameter = {
    new Parameter {
      override lazy val typeInstance: DracoType = Parameter.typeInstance
      override val name: String = _name
      override val valueType: String = _valueType
      override val value: String = _value
      override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
    }
  }

  private lazy val codec = Codec.sub[TypeElement, Parameter](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Parameter] = codec.encoder
  implicit def decoder: Decoder[Parameter] = codec.decoder
}

sealed trait Monadic extends BodyElement
object Monadic extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Monadic", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Monadic", _parameters = Seq (
      Parameter ("value", "String", "")
    ))
  )
  lazy val typeInstance: Type[Monadic] = Type[Monadic] (typeDefinition)
  def apply(_value: String): Monadic = new Monadic {
    override lazy val typeInstance: DracoType = Monadic.typeInstance
    override val name: String = ""
    override val valueType: String = "Unit"
    override val value: String = _value
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  lazy val Null: Monadic = Monadic("")
  private lazy val codec = Codec.sub[TypeElement, Monadic](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Monadic] = codec.encoder
  implicit def decoder: Decoder[Monadic] = codec.decoder
}

sealed trait Pattern extends BodyElement {
  val variables: Seq[Variable]
  val conditions: Seq[Condition]
}

object Pattern extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Pattern", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _elements = Seq (
      Fixed ("variables", "Seq[Variable]"),
      Fixed ("conditions", "Seq[Condition]")
    ),
    _factory = Factory ("Pattern", _parameters = Seq (
      Parameter ("variables", "Seq[Variable]", "Seq.empty"),
      Parameter ("conditions", "Seq[Condition]", "Seq.empty")
    ))
  )
  lazy val typeInstance: Type[Pattern] = Type[Pattern] (typeDefinition)
  def apply (
      _variables: Seq[Variable] = Seq.empty,
      _conditions: Seq[Condition] = Seq.empty
    ) : Pattern = new Pattern {
    override lazy val typeInstance: DracoType = Pattern.typeInstance
    override val variables: Seq[Variable] = _variables
    override val conditions: Seq[Condition] = _conditions
    override val name: String = ""
    override val valueType: String = "org.evrete.api.Knowledge => Unit"
    override val value: String = ""
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
  lazy val Null: Pattern = Pattern (_variables = Seq.empty, _conditions = Seq.empty)
  private lazy val codec = Codec.sub[TypeElement, Pattern](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Pattern] = codec.encoder
  implicit def decoder: Decoder[Pattern] = codec.decoder
}
sealed trait Action extends BodyElement {
  val variables: Seq[Variable]
  val values: Seq[Value]
}
object Action extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Action", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _elements = Seq (
      Fixed ("variables", "Seq[Variable]"),
      Fixed ("values", "Seq[Value]")
    ),
    _factory = Factory ("Action", _parameters = Seq (
      Parameter ("variables", "Seq[Variable]", "Seq.empty"),
      Parameter ("values", "Seq[Value]", "Seq.empty"),
      Parameter ("body", "Seq[BodyElement]", "Seq.empty")
    ))
  )
  lazy val typeInstance: Type[Action] = Type[Action] (typeDefinition)
  def apply (
              _variables: Seq[Variable] = Seq.empty,
              _values: Seq[Value] = Seq.empty,
              _body: Seq[BodyElement]
            ) : Action = new Action {
    override lazy val typeInstance: DracoType = Action.typeInstance
    override val name: String = "ctx"
    override val valueType: String = "org.evrete.api.RHSContext => Unit"
    override val value: String = ""
    override val body: Seq[BodyElement] = _body
    override val variables: Seq[Variable] = _variables
    override val values: Seq[Value] = _values
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
  lazy val Null: Action = Action (_variables = Seq.empty, _values = Seq.empty, _body = Seq.empty)
  private lazy val codec = Codec.sub[TypeElement, Action](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Action] = codec.encoder
  implicit def decoder: Decoder[Action] = codec.decoder
}
sealed trait Condition extends BodyElement

object Condition extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Condition", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Condition", _parameters = Seq (
      Parameter ("parameters", "Seq[Parameter]", "Seq.empty"),
      Parameter ("value", "String", "")
    ))
  )
  lazy val typeInstance: Type[Condition] = Type[Condition] (typeDefinition)
  def apply (
              _parameters: Seq[Parameter],
              _value: String
            ) : Condition = new Condition {
    override lazy val typeInstance: DracoType = Condition.typeInstance
    override val name: String = ""
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = Seq ()
    override val valueType: String = "Boolean"
    override val value: String = _value
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  lazy val Null: Condition = Condition (_parameters = Seq.empty, _value = "")
  private lazy val codec = Codec.sub[TypeElement, Condition](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Condition] = codec.encoder
  implicit def decoder: Decoder[Condition] = codec.decoder
}


sealed trait Variable extends BodyElement

object Variable extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Variable", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Variable", _parameters = Seq (
      Parameter ("name", "String", ""),
      Parameter ("valueType", "String", "")
    ))
  )
  lazy val typeInstance: Type[Variable] = Type[Variable] (typeDefinition)
  def apply (
              _variableName: String,
              _variableType: String
            ) : Variable = new Variable {
    override lazy val typeInstance: DracoType = Variable.typeInstance
    override val name: String = _variableName
    override val valueType: String = _variableType
    override val value: String = ""
    override val parameters: Seq[Parameter] = Seq ()
    override val body: Seq[BodyElement] = Seq ()
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
  lazy val Null: Variable = Variable (_variableName = "", _variableType = "")
  private lazy val codec = Codec.sub[TypeElement, Variable](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Variable] = codec.encoder
  implicit def decoder: Decoder[Variable] = codec.decoder
}

sealed trait Factory extends BodyElement

object Factory extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName ("Factory", _namePackage = Seq ("draco")),
    _derivation = Seq (TypeName ("BodyElement", _namePackage = Seq ("draco"))),
    _factory = Factory ("Factory", _parameters = Seq (
      Parameter ("valueType", "String", ""),
      Parameter ("parameters", "Seq[Parameter]", "Seq.empty"),
      Parameter ("body", "Seq[BodyElement]", "Seq.empty")
    ))
  )
  lazy val typeInstance: Type[Factory] = Type[Factory] (typeDefinition)
  def apply(
    _fullName: String,
    _parameters: Seq[Parameter] = Seq.empty,
    _body: Seq[BodyElement] = Seq.empty
  ): Factory = new Factory {
    override lazy val typeInstance: DracoType = Factory.typeInstance
    override val name: String = ""
    override val valueType: String = _fullName
    override val value: String = ""
    override val parameters: Seq[Parameter] = _parameters
    override val body: Seq[BodyElement] = _body
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }

  lazy val Null: Factory = Factory("")

  private lazy val codec = Codec.sub[TypeElement, Factory](TypeElement.encoder, TypeElement.decoder)
  implicit def encoder: Encoder[Factory] = codec.encoder
  implicit def decoder: Decoder[Factory] = codec.decoder
}
