package draco.domain

trait Dictionary[T] extends KeyDataMap[String, T]

object Dictionary {
  def apply[T](elements: (String, T)*) : Dictionary[T] = {
    new Dictionary[T] {
      override protected val internalMap: Map[String, T] = Map(elements: _*)
    }
  }
}