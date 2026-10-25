package domains.world

import draco._
import domains._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Observable extends World {
  val geocentric: Cartesian
  val heliocentric: Cartesian
}

object Observable extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Observable", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Observable] = Type[Observable] (typeDefinition)
  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)

  implicit lazy val encoder: Encoder[Observable] = Encoder.instance { x =>
    val fields = Seq(
      Some("geocentric" -> x.geocentric.asJson),
      Some("heliocentric" -> x.heliocentric.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Observable] = Decoder.instance { cursor =>
    for {
      _geocentric <- cursor.downField("geocentric").as[Option[Cartesian]].map(_.getOrElse(Cartesian.Null))
      _heliocentric <- cursor.downField("heliocentric").as[Option[Cartesian]].map(_.getOrElse(Cartesian.Null))
    } yield Observable (_geocentric, _heliocentric)
  }

  def apply (
    _geocentric: => Cartesian = Cartesian.Null,
    _heliocentric: => Cartesian = Cartesian.Null
  ) : Observable = new Observable {
    override lazy val geocentric: Cartesian = _geocentric
    override lazy val heliocentric: Cartesian = _heliocentric
    override lazy val typeDefinition: TypeDefinition = Observable.typeDefinition
  }

  lazy val Null: Observable = apply()

  def fromGeodetic(latitude: Double, longitude: Double, heightMetres: Double): Observable = Observable(_geocentric = Cartesian(Geodesy.geodeticToEcef(latitude, longitude, heightMetres)))
  def toGeodetic(observable: Observable): (Double, Double, Double) = Geodesy.ecefToGeodetic(observable.geocentric.x, observable.geocentric.y, observable.geocentric.z)
}
