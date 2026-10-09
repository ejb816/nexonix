package draco.service

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait ServiceConfiguration extends DracoType {
  val assembly: draco.Assembly
}

object ServiceConfiguration extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ServiceConfiguration", _namePackage = Seq ("draco", "service")))
  lazy val dracoType: Type[ServiceConfiguration] = Type[ServiceConfiguration] (typeDefinition)
  lazy val domainType: Domain[Service] = Domain[Service] (typeDefinition)

  implicit lazy val encoder: Encoder[ServiceConfiguration] = Encoder.instance { x =>
    val fields = Seq(
      Some("assembly" -> x.assembly.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[ServiceConfiguration] = Decoder.instance { cursor =>
    for {
      _assembly <- cursor.downField("assembly").as[Option[draco.Assembly]].map(_.getOrElse(draco.Assembly.Null))
    } yield ServiceConfiguration (_assembly)
  }

  def apply (
    _assembly: => draco.Assembly = draco.Assembly.Null
  ) : ServiceConfiguration = new ServiceConfiguration {
    override lazy val assembly: draco.Assembly = _assembly
    override lazy val typeDefinition: TypeDefinition = ServiceConfiguration.typeDefinition
  }

  lazy val Null: ServiceConfiguration = apply()


}
