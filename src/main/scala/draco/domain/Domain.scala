package draco.domain

trait Domain[T] extends Tree[Domain[T]] {
  val superDomain: Domain[T]
  val subDomains: Dictionary[Domain[T]]
}

object Domain {
  def apply[T] (
                 _superDomain: Domain[T],
                 _subDomains: Dictionary[Domain[T]],
                 _base: Domain[T],
               ) : Domain[T] = {
    new Domain[T] {
      override val superDomain: Domain[T] = _superDomain
      override val subDomains: Dictionary[Domain[T]] = _subDomains
      override val base: Domain[T] = _base
    }
  }
}
