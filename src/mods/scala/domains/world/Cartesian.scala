package domains.world

import draco._
import domains._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Cartesian extends DracoType {
  val x: Double
  val y: Double
  val z: Double
}

object Cartesian extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Cartesian", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Cartesian] = Type[Cartesian] (typeDefinition)
  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)

  implicit lazy val encoder: Encoder[Cartesian] = Encoder.instance { x =>
    val fields = Seq(
      Some("x" -> x.x.asJson),
      Some("y" -> x.y.asJson),
      Some("z" -> x.z.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Cartesian] = Decoder.instance { cursor =>
    for {
      _x <- cursor.downField("x").as[Option[Double]].map(_.getOrElse(0.0))
      _y <- cursor.downField("y").as[Option[Double]].map(_.getOrElse(0.0))
      _z <- cursor.downField("z").as[Option[Double]].map(_.getOrElse(0.0))
    } yield Cartesian (_x, _y, _z)
  }

  def apply (
    _x: => Double = 0.0,
    _y: => Double = 0.0,
    _z: => Double = 0.0
  ) : Cartesian = new Cartesian {
    override lazy val x: Double = _x
    override lazy val y: Double = _y
    override lazy val z: Double = _z
    override lazy val typeDefinition: TypeDefinition = Cartesian.typeDefinition
  }

  lazy val Null: Cartesian = apply()

  def apply(value: (Double, Double, Double)): Cartesian = Cartesian(value._1, value._2, value._3)
}
