package draco.base

import draco.Primal

trait Meters extends Distance with Primal[Double] {
  override val name: String = "Meters"
  override val description: String = "Distance or length measure"
}

object Meters {
  def apply (_value: Double) : Meters = new Meters {
    override val value: Double = _value
  }
}
