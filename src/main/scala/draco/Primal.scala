package draco

trait Primal [T] {
  val value : T
}

object Primal {
  def apply[T] (_value: T) : Primal[T] = new Primal[T] {
    override val value: T = _value
  }
}