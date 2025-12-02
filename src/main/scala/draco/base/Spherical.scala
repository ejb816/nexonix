package draco.base

trait Spherical extends Coordinates[(Radians,Radians,Meters)] {
  val value: (Radians,Radians,Meters)
  val azimuth: Radians
  val elevation: Radians
  val range: Meters
}

object Spherical {
  def apply (_azimuth: Radians, _elevation: Radians, _range: Meters): Spherical = new Spherical {
    override val value: (Radians, Radians, Meters) = (_azimuth, _elevation, _range)
    override val azimuth: Radians = value._1
    override val elevation: Radians = value._2
    override val range: Meters = value._3
  }
}