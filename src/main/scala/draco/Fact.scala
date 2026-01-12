package draco

trait Fact[P <: Product]

object Fact {
  def  apply[P <: Product] (_t: P): Fact[P] = new Fact[P] { _t }
}
