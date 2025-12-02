package draco.base

import draco.Primal

trait Radians extends Rotation with Primal[Double] {
  override val name: String = "Radians"
  override val description: String = "Arc length divided by radius"
}

object Radians {
  def apply (_value: Double) : Radians = new Radians {
    override val value: Double = _value
  }
}
