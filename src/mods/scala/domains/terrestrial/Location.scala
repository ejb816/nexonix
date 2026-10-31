package domains.terrestrial

import draco._
import domains._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Location extends Terrestrial {
  val latitude: Double
  val longitude: Double
  val elevationMetres: Int
}

object Location extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Location", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Location] = Type[Location] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  implicit lazy val encoder: Encoder[Location] = Encoder.instance { x =>
    val fields = Seq(
      Some("latitude" -> x.latitude.asJson),
      Some("longitude" -> x.longitude.asJson),
      Some("elevationMetres" -> x.elevationMetres.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Location] = Decoder.instance { cursor =>
    for {
      _latitude <- cursor.downField("latitude").as[Option[Double]].map(_.getOrElse(0.0))
      _longitude <- cursor.downField("longitude").as[Option[Double]].map(_.getOrElse(0.0))
      _elevationMetres <- cursor.downField("elevationMetres").as[Option[Int]].map(_.getOrElse(0))
    } yield Location (_latitude, _longitude, _elevationMetres)
  }

  def apply (
    _latitude: => Double = 0.0,
    _longitude: => Double = 0.0,
    _elevationMetres: => Int = 0
  ) : Location = new Location {
    override lazy val latitude: Double = _latitude
    override lazy val longitude: Double = _longitude
    override lazy val elevationMetres: Int = _elevationMetres
    override lazy val typeDefinition: TypeDefinition = Location.typeDefinition
  }

  lazy val Null: Location = apply()


}
