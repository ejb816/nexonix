package draco.base

import draco.Primal

trait Cardinal extends Unit with Primal[Double] {
  override val name: String = "Cardinal"
  override val description: String = "Numeric scalar value element of Natural, Integer, Rational or Real numbers"
}

