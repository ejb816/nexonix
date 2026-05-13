package draco

trait Dictionary[K, V] extends Map[K, V] with DracoType {
  val kvMap: Map[K, V]
  def removed(key: K): Map[K, V] = kvMap.removed(key)
  def updated[V1 >: V](key: K, value: V1): Map[K, V1] = kvMap.updated(key, value)
  def get(key: K): Option[V] = kvMap.get(key)
  def iterator: Iterator[(K, V)] = kvMap.iterator
}

object Dictionary extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Dictionary", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Dictionary[_, _]] = Type[Dictionary[_, _]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
