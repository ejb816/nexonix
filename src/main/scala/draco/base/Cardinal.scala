package draco.base

import org.nexonix.domains.Primal

trait Cardinal extends Unit with Primal[Double] {
  override val name: String = "Cardinal"
  override val description: String = "Numeric scalar value element of Natural, Integer, Rational or Real numbers"
}

