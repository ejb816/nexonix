package draco.domain.stooge

trait Larry extends StoogeActor

object Larry {
  def apply() : Larry = new Larry {}
}