package draco.domain.stooge

trait Curly extends StoogeActor

object Curly {
  def apply(): Curly = new Curly {}
}
