package draco.base

trait Rectangular extends Coordinates[(Meters,Meters)] {
  val value: (Meters,Meters)
  val x: Meters = value._1
  val y: Meters = value._2
}

object Rectangular {
  def apply (_x: Meters, _y: Meters) : Rectangular = new Rectangular {
    override val value: (Meters, Meters) = (_x, _y)
  }
}