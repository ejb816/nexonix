package draco.service.zeromq

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorSystem, Behavior, PostStop, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

/** Staging actor membrane. Bounded queues bridge socket/domain threads to working memory.
  * Domain outputs obtain a callback bound to a configured domain, never a wire source claim.
  * This fixture owns its actor system; a later service assembly can supply the lifecycle.
  */
final class ZeroMqBridge(ontology: DomainOntology, endpoints: Seq[BridgeRules.Endpoint],
                         config: PairTextTransport.Config = PairTextTransport.Config()) extends AutoCloseable {
  private val transport = new PairTextTransport(config)
  // Fail construction on bind failure rather than returning a running actor with dead I/O.
  private val boundEndpoint = try transport.endpoint catch {
    case NonFatal(e) =>
      try transport.close() catch { case NonFatal(closeError) => e.addSuppressed(closeError) }
      throw e
  }
  private val routing = new BridgeRules.Routing(ontology, endpoints)
  private val results = new ArrayBlockingQueue[BridgeRules.Result](config.capacity)
  private val failures = new ArrayBlockingQueue[String](config.capacity)
  private val count = new AtomicLong()
  @volatile private var closed = false
  private def failed(reason: String): Unit = { count.incrementAndGet(); failures.offer(reason) }
  private case object Tick
  private val actor = Behaviors.withTimers[Tick.type] { timers =>
    val knowledge = Rule.knowledgeService.newKnowledge()
    BridgeRules.install(knowledge, routing, transport.output, failed)
    val session = knowledge.newStatefulSession()
    timers.startTimerWithFixedDelay(Tick, 10.millis)
    new Actor[Tick.type] {
      override lazy val typeDefinition: TypeDefinition =
        TypeDefinition(TypeName("ZeroMqBridge", Seq("draco", "service", "zeromq")))
      override def receive(ctx: TypedActorContext[Tick.type], msg: Tick.type): Behavior[Tick.type] = {
        var n = 0
        while (n < config.capacity) {
          transport.pollText().foreach { text =>
            session.insert(Seq(BridgeRules.Incoming(text)): _*); session.fire()
          }
          val result = results.poll()
          if (result != null) { session.insert(Seq(result): _*); session.fire() }
          n += 1
        }
        Behaviors.same
      }
      override def receiveSignal(ctx: TypedActorContext[Tick.type], signal: Signal): Behavior[Tick.type] = {
        if (signal == PostStop) {
          closed = true
          try session.close() finally transport.requestStop()
        }
        Behaviors.same
      }
    }
  }
  private val system = try ActorSystem(actor, "dracoZeroMqBridge") catch {
    case NonFatal(e) => transport.close(); throw e
  }
  def endpoint: String = boundEndpoint
  def failureCount: Long = count.get() + transport.failureCount
  def pollFailure(): Option[String] = Option(failures.poll()).orElse(transport.pollFailure())

  def outputFor(domain: TypeName): Json => Boolean = {
    routing.resolve(domain).fold(error => throw new IllegalArgumentException(error), identity)
    payload => synchronized {
      if (closed) false
      else if (!results.offer(BridgeRules.Result(domain, payload))) { failed("domain output queue full"); false }
      else true
    }
  }
  override def close(): Unit = {
    synchronized { closed = true }
    system.terminate()
    Await.result(system.whenTerminated, config.shutdownMillis.millis + 1.second)
    transport.close() // idempotent; also covers failure before actor setup
    var result = results.poll()
    while (result != null) { failed("shutdown discarded domain result"); result = results.poll() }
  }
}
