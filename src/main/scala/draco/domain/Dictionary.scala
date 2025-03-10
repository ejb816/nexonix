package draco.domain

trait Dictionary[T] extends KeyDataMap[String, T] {
  val name: TypeName = TypeName (
    _name = classOf[T].getSimpleName + "Dictionary",
    _namePackage = classOf[T].getPackage.getName.split("\\.").to[Seq[String]]
  )
}

object Dictionary {
  def apply[T](elements: (String, T)*) : Dictionary[T] = {
    new Dictionary[T] {
      override protected val internalMap: Map[String, T] = Map(elements: _*)
    }
  }
}