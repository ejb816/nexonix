package domains.aerial

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

/** Observability boundary for the Aerial consumer in the isolated (pre-World)
  * slice — where a `Consumer`'s rule deposits what it has consumed. It stands in
  * for a real external sink (a downstream service, a log, or another medium once
  * transforms exist). Hand-written on purpose: it is the test's observation point,
  * not generated domain structure, so it lives in scala/ with no JSON twin. */
object AerialSink {
  private val entries = new ConcurrentLinkedQueue[String]()

  def record(entry: String): Unit = entries.add(entry)
  def recorded: Seq[String] = entries.asScala.toSeq
  def clear(): Unit = entries.clear()
}
