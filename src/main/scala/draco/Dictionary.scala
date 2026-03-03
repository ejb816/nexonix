package draco

trait Dictionary[K,V] extends Map[K, V] {
  val kvMap: Map[K, V]
  def removed(key: K): Map[K, V] = kvMap.removed(key)
  def updated[V1 >: V](key: K, value: V1): Map[K, V1] = kvMap.updated(key, value)
  def get(key: K): Option[V] = kvMap.get(key)
  def iterator: Iterator[(K, V)] = kvMap.iterator
}

object Dictionary {
  def apply[K,V] (_kvMap: Dictionary[K,V]) : Dictionary[K,V] = new Dictionary[K,V] {
    override val kvMap: Map[K, V] = _kvMap
  }
}
