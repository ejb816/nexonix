
package generated.draco

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeName  {
  val name: String
  val namePackage: Seq[String]
  val aspects: Seq[String]
  val typeParameters: Seq[String]
  val qualifiedName: String
  val namePath: String
  val resourcePath: String
}

object TypeName extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("TypeName", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[TypeName] = Type[TypeName] (typeDefinition)

  implicit lazy val encoder: Encoder[TypeName] = Encoder.instance { x =>
    val fields = Seq(
      Some("name" -> x.name.asJson),
      if (x.namePackage.nonEmpty) Some("namePackage" -> x.namePackage.asJson) else None,
      if (x.aspects.nonEmpty) Some("aspects" -> x.aspects.asJson) else None,
      if (x.typeParameters.nonEmpty) Some("typeParameters" -> x.typeParameters.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[TypeName] = Decoder.instance { cursor =>
    for {
      _name <- cursor.downField("name").as[String]
      _namePackage <- cursor.downField("namePackage").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _aspects <- cursor.downField("aspects").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _typeParameters <- cursor.downField("typeParameters").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
    } yield TypeName (_name, _namePackage, _aspects, _typeParameters)
  }

  def apply (
    _name: String,
    _namePackage: Seq[String] = Seq(),
    _aspects: Seq[String] = Seq(),
    _typeParameters: Seq[String] = Seq()
  ) : TypeName = new TypeName {
    override val name: String = _name
    override val namePackage: Seq[String] = _namePackage
    override val aspects: Seq[String] = canonicalOrder(_aspects)
    override val typeParameters: Seq[String] = _typeParameters
    override val qualifiedName: String = fullQualifiedName(name, aspects)
    override val namePath: String = fullNamePath(namePackage, qualifiedName)
    override val resourcePath: String = fullResourcePath(namePackage, name, aspects)
    override lazy val typeInstance: DracoType = TypeName.typeInstance
    override lazy val typeDefinition: TypeDefinition = TypeName.typeDefinition
  }

  lazy val Null: TypeName = new TypeName {
    override val name: String = ""
    override val namePackage: Seq[String] = Seq.empty
    override val aspects: Seq[String] = Seq.empty
    override val typeParameters: Seq[String] = Seq.empty
    override val qualifiedName: String = ""
    override val namePath: String = ""
    override val resourcePath: String = ""
    override lazy val typeDefinition: TypeDefinition = TypeDefinition.Null
    override lazy val typeInstance: DracoType = TypeName.Null
  }

  lazy val validAspects: Seq[String] = Seq("domain", "rule", "actor")
  def canonicalOrder(aspects: Seq[String]): Seq[String] = validAspects.filter(aspects.contains)
  def aspectSuffix(aspects: Seq[String]): String = canonicalOrder(aspects).map(_.capitalize).mkString
  def aspectExtension(aspects: Seq[String]): String = {
    val ordered: Seq[String] = canonicalOrder(aspects)
    if (ordered.isEmpty) "" else "." + ordered.mkString(".")
  }
  def fullQualifiedName(n: String, a: Seq[String]): String = s"$n${aspectSuffix(a)}"
  def fullNamePath(np: Seq[String], qn: String): String = if (np.isEmpty) qn else s"${np.mkString(".")}.${qn}"
  def fullResourcePath(np: Seq[String], n: String, a: Seq[String]): String = s"/${np.mkString("/")}/$n${aspectExtension(a)}.json"
}
