package domains.terrestrial

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

/** Observability boundary for the Terrestrial consumer in the isolated (pre-World)
  * slice — peer of `domains.aerial.AerialSink`. Where a `Consumer`'s rule deposits
  * what it has consumed; stands in for a real external sink. Hand-written on
  * purpose: the test's observation point, not generated domain structure, so it
  * lives in scala/ with no JSON twin. */
object TerrestrialSink {
  private val entries = new ConcurrentLinkedQueue[String]()

  def record(entry: String): Unit = entries.add(entry)
  def recorded: Seq[String] = entries.asScala.toSeq
  def clear(): Unit = entries.clear()
}
