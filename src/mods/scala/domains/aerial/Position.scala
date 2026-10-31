package domains.aerial

import draco._
import domains._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Position extends Aerial {
  val latitude: Double
  val longitude: Double
  val altitudeFeet: Int
}

object Position extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Position", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Position] = Type[Position] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  implicit lazy val encoder: Encoder[Position] = Encoder.instance { x =>
    val fields = Seq(
      Some("latitude" -> x.latitude.asJson),
      Some("longitude" -> x.longitude.asJson),
      Some("altitudeFeet" -> x.altitudeFeet.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Position] = Decoder.instance { cursor =>
    for {
      _latitude <- cursor.downField("latitude").as[Option[Double]].map(_.getOrElse(0.0))
      _longitude <- cursor.downField("longitude").as[Option[Double]].map(_.getOrElse(0.0))
      _altitudeFeet <- cursor.downField("altitudeFeet").as[Option[Int]].map(_.getOrElse(0))
    } yield Position (_latitude, _longitude, _altitudeFeet)
  }

  def apply (
    _latitude: => Double = 0.0,
    _longitude: => Double = 0.0,
    _altitudeFeet: => Int = 0
  ) : Position = new Position {
    override lazy val latitude: Double = _latitude
    override lazy val longitude: Double = _longitude
    override lazy val altitudeFeet: Int = _altitudeFeet
    override lazy val typeDefinition: TypeDefinition = Position.typeDefinition
  }

  lazy val Null: Position = apply()


}
