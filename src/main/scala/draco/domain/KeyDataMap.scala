package draco.domain

trait KeyDataMap[K,T] extends Map[K, T] {
  protected val internalMap: Map[K, T]
  def removed(key: K): Map[K, T] = internalMap.removed(key)

  def updated[V1 >: T](key: K, value: V1): Map[K, V1] = internalMap.updated(key, value)

  def get(key: K): Option[T] = internalMap.get(key)

  def iterator: Iterator[(K, T)] = internalMap.iterator
}
