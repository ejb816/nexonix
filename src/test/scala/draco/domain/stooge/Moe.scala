package draco.domain.stooge

trait Moe extends StoogeActor

object Moe {
  def apply() : Moe = new Moe {}
}
