package draco.service.zeromq

import draco.service.TextOutput
import org.zeromq.{SocketType, ZContext}

import java.nio.ByteBuffer
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.util.control.NonFatal

/** Target-side, single-peer TCP fixture. PAIR and single-frame UTF-8 are provisional.
  * One thread creates, uses and closes the socket/context. Queues and frames are bounded.
  * No JSON or Draco domain knowledge belongs here. A successful send means queued only.
  */
final class PairTextTransport(config: PairTextTransport.Config) extends AutoCloseable {
  import PairTextTransport._
  private val incoming = new ArrayBlockingQueue[String](config.capacity)
  private val outgoing = new ArrayBlockingQueue[String](config.capacity)
  private val errors = new ArrayBlockingQueue[String](config.capacity)
  private val losses = new AtomicLong()
  private val stopping = new AtomicBoolean(false)
  private val ready = Promise[String]()
  private val completed = Promise[Unit]()

  private def failure(reason: String): Unit = {
    losses.incrementAndGet()
    errors.offer(reason)
  }
  def failureCount: Long = losses.get()
  def pollFailure(): Option[String] = Option(errors.poll())
  def pollText(): Option[String] = Option(incoming.poll())
  def endpoint: String = Await.result(ready.future, config.shutdownMillis.millis)
  def isClosed: Boolean = completed.isCompleted

  val output: TextOutput = TextOutput(text => synchronized {
    if (stopping.get()) false
    else if (text == null || text.length > config.maxBytes ||
      text.getBytes(StandardCharsets.UTF_8).length > config.maxBytes) {
      failure("outgoing frame too large or null"); false
    } else if (!outgoing.offer(text)) { failure("outgoing queue full"); false }
    else true
  })

  private val owner = new Thread(() => run(), "draco-zeromq-owner")
  owner.setDaemon(true)
  owner.start()

  private def run(): Unit = {
    try {
      val context = new ZContext()
      try {
        val socket = context.createSocket(SocketType.PAIR)
        socket.setLinger(0)
        socket.setSndHWM(config.capacity)
        socket.setRcvHWM(config.capacity)
        socket.setMaxMsgSize(config.maxBytes.toLong)
        socket.setReceiveTimeOut(10)
        socket.setSendTimeOut(10)
        socket.setImmediate(true)
        val port = socket.bindToRandomPort("tcp://127.0.0.1")
        require(port > 0, "could not bind loopback socket")
        ready.success(s"tcp://127.0.0.1:$port")
        var multipart = false
        while (!stopping.get()) {
          // One send per iteration prevents output pressure starving receipt/stop.
          val text = outgoing.poll()
          if (text != null && !socket.send(text.getBytes(StandardCharsets.UTF_8)))
            failure("send timed out; frame discarded")
          val bytes = socket.recv()
          if (bytes != null) {
            val more = socket.hasReceiveMore
            if (multipart || more) {
              if (!multipart) failure("multipart frames unsupported")
              multipart = more // discard every part, including the last, without concatenation
            } else if (bytes.length > config.maxBytes) failure("incoming frame too large")
            else {
              try {
                val decoder = StandardCharsets.UTF_8.newDecoder()
                  .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                val decoded = decoder.decode(ByteBuffer.wrap(bytes)).toString
                if (!incoming.offer(decoded)) failure("incoming queue full")
              } catch { case _: java.nio.charset.CharacterCodingException => failure("invalid UTF-8") }
            }
          }
        }
      } finally context.close() // socket/context ownership stays on this thread
      completed.trySuccess(())
    } catch {
      case NonFatal(e) =>
        failure(s"I/O failed: ${e.getClass.getSimpleName}: ${e.getMessage}")
        ready.tryFailure(e)
        completed.tryFailure(e)
    } finally synchronized {
      stopping.set(true)
      var dropped = outgoing.poll()
      while (dropped != null) { failure("shutdown discarded queued frame"); dropped = outgoing.poll() }
    }
  }

  def requestStop(): Unit = synchronized { stopping.set(true) }

  override def close(): Unit = {
    requestStop()
    // Never interrupt a JeroMQ thread or close its socket from this caller.
    owner.join(config.shutdownMillis)
    if (owner.isAlive) throw new IllegalStateException("ZeroMQ owner did not stop within deadline")
    Await.result(completed.future, Duration.Zero)
  }
}

object PairTextTransport {
  final case class Config(capacity: Int = 64, maxBytes: Int = 65536, shutdownMillis: Long = 3000) {
    require(capacity > 0 && maxBytes > 0 && shutdownMillis > 0)
  }
}
