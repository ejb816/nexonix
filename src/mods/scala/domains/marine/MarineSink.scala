package domains.marine

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

/** Observability boundary for the Marine consumer in the isolated (pre-World)
  * slice — peer of `domains.aerial.AerialSink`. Hand-written on purpose: the test's
  * observation point, not generated domain structure, so it lives in scala/ with no
  * JSON twin. */
object MarineSink {
  private val entries = new ConcurrentLinkedQueue[String]()

  def record(entry: String): Unit = entries.add(entry)
  def recorded: Seq[String] = entries.asScala.toSeq
  def clear(): Unit = entries.clear()
}
