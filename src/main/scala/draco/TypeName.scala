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
    val fields = Seq(
      Some("name" -> Json.fromString(t.name)),
      if (t.namePackage.nonEmpty) Some("namePackage" -> t.namePackage.asJson) else None,
      if (t.parent.nonEmpty) Some("parent" -> t.parent.asJson) else None
    ).flatten

    Json.obj(fields: _*)
  }

  lazy implicit val decoder: Decoder[TypeName] = Decoder.instance { c =>
    for {
      _name        <- c.downField("name").as[String]
      _namePackage <- c.downField("namePackage").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _parent      <- c.downField("parent").as[Option[String]].map(_.getOrElse(""))
    } yield {
      TypeName(_name, _parent, _namePackage)
    }
  }}
