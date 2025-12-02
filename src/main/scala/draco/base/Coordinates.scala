package draco.base

import draco.{TypeDefinition, TypeDictionary}

trait Coordinates[T] {
  val value: T
}

object Coordinates {
  def apply[T] (t: T): Coordinates[T] = new Coordinates[T] {
    override val value: T = t
  }
}