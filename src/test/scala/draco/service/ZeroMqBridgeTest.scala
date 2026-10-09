package draco.service

import draco._
import draco.service.zeromq.{BridgeRules, PairTextTransport, ZeroMqBridge}
import io.circe.Json
import io.circe.syntax._
import org.apache.pekko.actor.typed.{ActorSystem, Behavior, PostStop, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.funsuite.AnyFunSuite
import org.zeromq.{SocketType, ZContext}

import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Await
import scala.concurrent.duration._

class ZeroMqBridgeTest extends AnyFunSuite {
  private val domain = TypeName("Draco", Seq("draco"))
  private def ontology = DomainBuilder.ontology(DomainBuilder.define("Draco", Seq("draco")))
  private def request(payload: Json, name: TypeName = domain): String =
    Json.obj("destinationDomain" -> name.asJson, "payload" -> payload).noSpaces
  private def eventually(check: => Boolean): Unit = {
    val deadline = 5.seconds.fromNow
    while (!check && deadline.hasTimeLeft()) Thread.sleep(10)
    assert(check)
  }
  private def peer[A](endpoint: String)(f: org.zeromq.ZMQ.Socket => A): A = {
    val context = new ZContext()
    try {
      val socket = context.createSocket(SocketType.PAIR)
      socket.setLinger(0); socket.setSendTimeOut(2000); socket.setReceiveTimeOut(3000)
      assert(socket.connect(endpoint))
      f(socket)
    } finally context.close()
  }

  // Domain actors use the existing Draco Actor/Rule primitives. The transport rule
  // never selects or decodes this TypeName payload: the domain's input rule does.
  private def ruleActor[T](clazz: Class[T], name: String)(consume: T => Unit): ActorSystem[T] = {
    val behavior = Behaviors.setup[T] { _ =>
      val k = Rule.knowledgeService.newKnowledge()
      val rule = Rule[T](_ => (), ctx => {
        val value = ctx.get[T]("$value"); ctx.deleteFact("$value"); consume(value)
      })
      k.builder().newRule(name).forEach("$value", clazz).execute(rule.action(_)).build()
      val session = k.newStatefulSession()
      new Actor[T] {
        override lazy val typeDefinition = TypeDefinition(TypeName(name, Seq("draco", "service", "fixture")))
        override def receive(ctx: TypedActorContext[T], msg: T): Behavior[T] = {
          session.insert(Seq(msg): _*); session.fire(); Behaviors.same
        }
        override def receiveSignal(ctx: TypedActorContext[T], signal: Signal): Behavior[T] = {
          if (signal == PostStop) session.close()
          Behaviors.same
        }
      }
    }
    ActorSystem(behavior, name)
  }
  private def stop[T](actor: ActorSystem[T]): Unit = {
    actor.terminate(); Await.result(actor.whenTerminated, 5.seconds)
  }

  test("external TCP peer crosses transport rules, ontology, typed domain input and output rules") {
    val returned = new ArrayBlockingQueue[Json => Boolean](1)
    val badPayloads = new AtomicInteger()
    val typedCount = new AtomicInteger()
    val output = ruleActor(classOf[TypeName], "BridgeDomainOutput") { value =>
      typedCount.incrementAndGet()
      assert(returned.peek()(value.asJson))
    }
    val input = ruleActor(classOf[Json], "BridgeDomainInput") { value =>
      value.as[TypeName].fold(_ => badPayloads.incrementAndGet(), typed => { output ! typed; 0 })
    }
    val bridge = new ZeroMqBridge(ontology, Seq(BridgeRules.Endpoint(domain, input ! _)))
    try {
      returned.add(bridge.outputFor(domain))
      peer(bridge.endpoint) { socket =>
        val value = TypeName("Example", Seq("example"), Seq(Json.fromString("Text"))).asJson
        for (_ <- 1 to 3) {
          assert(socket.send(request(value)))
          val bytes = socket.recv(); assert(bytes != null)
          val response = io.circe.parser.parse(new String(bytes, UTF_8)).toOption.get
          assert(response.hcursor.downField("sourceDomain").as[TypeName] == Right(domain))
          assert(response.hcursor.downField("payload").focus.contains(value))
        }
        assert(typedCount.get() == 3)
        assert(socket.send(request(Json.fromInt(3))))
        eventually(badPayloads.get() == 1)
        socket.setReceiveTimeOut(100)
        assert(socket.recv() == null)
        assert(typedCount.get() == 3)
      }
      assert(bridge.failureCount == 0)
    } finally { stop(input); stop(output); bridge.close() }
  }

  test("transport rules reject malformed and unknown input without dispatch and retract every fact") {
    val received = scala.collection.mutable.ArrayBuffer.empty[Json]
    val errors = scala.collection.mutable.ArrayBuffer.empty[String]
    val k = Rule.knowledgeService.newKnowledge()
    BridgeRules.install(k, new BridgeRules.Routing(ontology, Seq(BridgeRules.Endpoint(domain, received += _))),
      TextOutput(_ => true), errors += _)
    val session = k.newStatefulSession()
    try {
      Seq("{", "[]", "{}", Json.obj("destinationDomain" -> domain.asJson).noSpaces,
        request(Json.Null, TypeName("Missing", Seq("draco")))).foreach { text =>
        session.insert(Seq(BridgeRules.Incoming(text)): _*); session.fire()
      }
      assert(received.isEmpty && errors.size == 5)
      session.insert(Seq(BridgeRules.Incoming(request(Json.fromString("ok")))): _*)
      session.fire(); session.fire()
      assert(received.toSeq == Seq(Json.fromString("ok")))
      val facts = new AtomicInteger()
      session.forEachFact((_: Object) => facts.incrementAndGet())
      assert(facts.get() == 0)
    } finally session.close()
  }

  test("routing requires complete identity and detects duplicate domains and endpoints") {
    def named(name: TypeName): DomainType = new Domain[Any] {
      override lazy val typeDefinition = TypeDefinition(name)
      override lazy val typeDictionary = TypeDictionary.Null
    }
    val first = TypeName("Generic", Seq("example"), Seq(Json.fromString("Text")))
    val second = TypeName("Generic", Seq("example"), Seq(Json.fromString("Int")))
    val route = new BridgeRules.Routing(DomainOntology(Seq(named(first), named(second))),
      Seq(BridgeRules.Endpoint(first, _ => ()), BridgeRules.Endpoint(second, _ => ())))
    assert(route.resolve(first).isRight && route.resolve(second).isRight)
    assert(route.resolve(TypeName("Generic", Seq("example"))).left.toOption.contains("unknown domain"))
    val duplicate = new BridgeRules.Routing(DomainOntology(Seq(named(first), named(first))), Nil)
    assert(duplicate.resolve(first).left.toOption.contains("ambiguous domain"))
    val missing = new BridgeRules.Routing(ontology, Nil)
    assert(missing.resolve(domain).left.toOption.contains("missing input endpoint"))
    val ambiguous = new BridgeRules.Routing(ontology,
      Seq(BridgeRules.Endpoint(domain, _ => ()), BridgeRules.Endpoint(domain, _ => ())))
    assert(ambiguous.resolve(domain).left.toOption.contains("ambiguous input endpoint"))
  }

  test("output callbacks reject unconfigured sources and shutdown rejects further results") {
    val bridge = new ZeroMqBridge(ontology, Seq(BridgeRules.Endpoint(domain, _ => ())))
    val output = bridge.outputFor(domain)
    try {
      intercept[IllegalArgumentException](bridge.outputFor(TypeName("Ghost")))
      bridge.endpoint
    } finally bridge.close()
    assert(!output(Json.Null))
    bridge.close()
  }

  test("transport rejects multipart and invalid UTF-8 without leaking trailing frames") {
    val transport = new PairTextTransport(PairTextTransport.Config())
    try peer(transport.endpoint) { socket =>
      assert(socket.sendMore("part1")); assert(socket.send("part2"))
      assert(socket.send(Array(0xc3.toByte, 0x28.toByte)))
      assert(socket.send("valid"))
      eventually(transport.failureCount == 2)
      var received: Option[String] = None
      eventually { if (received.isEmpty) received = transport.pollText(); received.contains("valid") }
      assert(transport.pollText().isEmpty)
    } finally transport.close()
  }

  test("incoming queue overflow is bounded and observable") {
    val transport = new PairTextTransport(PairTextTransport.Config(capacity = 1))
    try peer(transport.endpoint) { socket =>
      for (i <- 1 to 10) assert(socket.send(i.toString))
      eventually(transport.failureCount > 0)
      assert(transport.pollText().nonEmpty)
      assert(transport.pollFailure().contains("incoming queue full"))
    } finally transport.close()
  }

  test("outgoing queue rejection, oversize and unavailable peer are observable; close is bounded") {
    val transport = new PairTextTransport(PairTextTransport.Config(capacity = 1, maxBytes = 16))
    transport.endpoint
    assert(!transport.output.send("x" * 17))
    val attempts = (1 to 1000).map(_ => transport.output.send("hi"))
    assert(attempts.contains(false))
    eventually(transport.failureCount > 1)
    transport.close()
    assert(transport.isClosed)
    assert(!transport.output.send("late"))
    transport.close()
  }

  test("fresh bridge instances can start and stop repeatedly") {
    for (_ <- 1 to 3) {
      val bridge = new ZeroMqBridge(ontology, Seq(BridgeRules.Endpoint(domain, _ => ())))
      try assert(bridge.endpoint.startsWith("tcp://127.0.0.1:")) finally bridge.close()
    }
  }

  test("a send to an unavailable peer reports a timeout rather than delivery") {
    val transport = new PairTextTransport(PairTextTransport.Config())
    try {
      transport.endpoint
      assert(transport.output.send("queued"))
      var failure: Option[String] = None
      eventually { if (failure.isEmpty) failure = transport.pollFailure(); failure.nonEmpty }
      assert(failure.contains("send timed out; frame discarded"))
    } finally transport.close()
  }
}
