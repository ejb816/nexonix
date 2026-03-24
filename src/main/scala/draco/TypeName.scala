package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeName extends TypeInstance {
  val name: String
  val namePackage: Seq[String]
  val typeParameters: Seq[String]
  val namePath: String
  val resourcePath: String
}

object TypeName extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition.load(TypeName ("TypeName", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeName] = Type[TypeName] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeName] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      if (x.namePackage.nonEmpty) Some("namePackage" -> x.namePackage.asJson) else None,
      if (x.typeParameters.nonEmpty) Some("typeParameters" -> x.typeParameters.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[String]
      _namePackage <- cursor.downField("namePackage").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _typeParameters <- cursor.downField("typeParameters").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield TypeName (_name, _namePackage, _typeParameters)
  }

  def apply (
              _name: String,
              _namePackage: Seq[String] = Seq(),
              _typeParameters: Seq[String] = Seq()
            ) : TypeName = new TypeName {
    override val name: String = _name
    override val namePackage: Seq[String] = _namePackage
    override val typeParameters: Seq[String] = _typeParameters
    override val namePath: String = fullNamePath(namePackage, name)
    override val resourcePath: String = fullResourcePath(namePackage, name)
    override lazy val typeInstance: DracoType = TypeName.typeInstance
    override lazy val typeDefinition: TypeDefinition = TypeName.typeDefinition
  }

  lazy val Null: TypeName = new TypeName {
    override val name: String = ""
    override val namePackage: Seq[String] = Seq.empty
    override val typeParameters: Seq[String] = Seq.empty
    override val namePath: String = ""
    override val resourcePath: String = ""
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.Null
    override lazy val typeInstance: DracoType = TypeName.Null
  }

  def fullNamePath(np: Seq[String], n: String): String = if (np.isEmpty) n else s"${np.mkString(".")}.$n"
  def fullResourcePath(np: Seq[String], n: String): String = s"/${np.mkString("/")}/$n.json"
}
