package draco.domain

trait KeyValueMap[K,V] extends Map[K, V] {
  val kvMap: Map[K, V]
  def removed(key: K): Map[K, V] = kvMap.removed(key)
  def updated[V1 >: V](key: K, value: V1): Map[K, V1] = kvMap.updated(key, value)
  def get(key: K): Option[V] = kvMap.get(key)
  def iterator: Iterator[(K, V)] = kvMap.iterator
}

object KeyValueMap {
  def apply[K,V] (_kvMap: KeyValueMap[K,V]) : KeyValueMap[K,V] = new KeyValueMap[K,V] {
    override val kvMap: Map[K, V] = _kvMap
  }
}
