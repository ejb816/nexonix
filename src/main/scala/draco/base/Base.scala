package draco.base

import draco.Draco

trait Base extends Draco

object Base extends App {
  def apply () : Base = new Base {}
}
