package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeName {
  val name: String
  val namePackage: Seq[String]
  val typeParameters: Seq[String]
  val namePath: String
  val resourcePath: String
}

object TypeName extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("TypeName", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeName] = Type[TypeName] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

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
      _name <- cursor.downField("name").as[Option[String]].map(_.getOrElse(""))
      _namePackage <- cursor.downField("namePackage").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _typeParameters <- cursor.downField("typeParameters").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield TypeName (_name, _namePackage, _typeParameters)
  }

  def apply (
    _name: String,
    _namePackage: Seq[String] = Seq(),
    _typeParameters: Seq[String] = Seq()
  ) : TypeName = new TypeName {
    override lazy val name: String = _name
    override lazy val namePackage: Seq[String] = _namePackage
    override lazy val typeParameters: Seq[String] = _typeParameters
    override lazy val namePath: String = fullNamePath(namePackage, name)
    override lazy val resourcePath: String = fullResourcePath(namePackage, name)
  }

  lazy val Null: TypeName = apply(
    _name = "",
    _namePackage = Seq(),
    _typeParameters = Seq()
  )

  def fullNamePath(np: Seq[String], n: String): String = if (np.isEmpty) n else s"${np.mkString(".")}.${n}"
  def fullResourcePath(np: Seq[String], n: String): String = s"/${np.mkString("/")}/$n.json"
}
