package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeName {
  val name: String
  val namePackage: Seq[String]
  val parent: String
  val fullName: String
  val resourcePath: String
}

object TypeName {
  private def fullTypeName: (Seq[String],String) => String = (np, n) => s"${np.mkString(".")}.$n"
  private def fullResourcePath: (Seq[String],String) => String = (np, n) => s"/${np.mkString("/")}/$n.json"

  lazy val Null: TypeName = new TypeName {
    override val name: String = ""
    override val namePackage: Seq[String] = Seq()
    override val parent: String = ""
    override val fullName: String = ""
    override val resourcePath: String = ""
  }
  def apply (
              _name: String,
              _parent: String = "",
              _namePackage: Seq[String] = Seq ()
            ) : TypeName = {
    new TypeName {
      val nameElements: Seq[String] = _parent.split('.')
      override val name: String = _name
      override val namePackage: Seq[String] = if (_parent.nonEmpty) nameElements.dropRight(1) ++ Seq (_name.toLowerCase)
      else if (_namePackage.nonEmpty) _namePackage
      else Seq (_name.toLowerCase)
      override val parent: String = _parent
      override val fullName: String = fullTypeName (namePackage, _name)
      override val resourcePath: String = fullResourcePath (namePackage, _name)
    }
  }

  lazy implicit val encoder: Encoder[TypeName] = Encoder.instance { t =>
    // Only local identity; DO NOT embed parent object (breaks the cycle).
    Json.obj(
      "name"        -> Json.fromString(t.name),
      "namePackage" -> t.namePackage.asJson,
      "parent"  -> t.parent.asJson
    )
  }

  lazy implicit val decoder: Decoder[TypeName] = Decoder.instance { c =>
    for {
      _name        <- c.downField("name").as[String]
      _namePackage <- c.downField("namePackage").as[Seq[String]]
      _parent      <- c.downField("parent").as[String]
    } yield {
      TypeName(_name, _parent, _namePackage)
    }
  }}
