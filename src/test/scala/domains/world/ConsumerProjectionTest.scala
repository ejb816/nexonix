package domains.world

import draco._
import java.nio.file.{Files, Paths}
import org.apache.pekko.actor.testkit.typed.scaladsl.BehaviorTestKit
import org.apache.pekko.actor.typed.PreRestart
import org.scalatest.funsuite.AnyFunSuite

class ConsumerProjectionTest extends AnyFunSuite {
  test("Consumer's Drake, JSON and generated Scala agree exactly and declare the World actor role") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/world/Consumer"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/world/Consumer.scala"))
    assert(td.actorAspect.fold(false, _.messageType == World.typeDefinition.typeName))
  }

  test("positions produce one Location each, preserving horizontal coordinates and rounding metres") {
    val received = scala.collection.mutable.ArrayBuffer[World]()
    val target: World => Unit = message => received += message
    val actor = Consumer.actorType(target).asInstanceOf[Actor[World]]
    assert(actor.typeDefinition eq Consumer.typeDefinition)
    val kit = BehaviorTestKit(actor)
    val cases = Seq((51.5, -0.12, 35000, 10668), (0.0, 0.0, 2, 1),
      (0.0, 0.0, -2, -1), (0.0, 0.0, 1, 0), (0.0, 0.0, -1, 0))
    for (((lat, lon, feet, metres), index) <- cases.zipWithIndex) {
      kit.run(domains.aerial.Position(lat, lon, feet))
      assert(received.size == index + 1)
      assert(received.last.isInstanceOf[domains.terrestrial.Location])
      val location = received.last.asInstanceOf[domains.terrestrial.Location]
      assert(math.abs(location.latitude - lat) < 1e-6)
      assert(math.abs(location.longitude - lon) < 1e-6)
      assert(location.elevationMetres == metres)
      assert(kit.isAlive && kit.retrieveAllEffects().isEmpty)
    }
  }

  test("non-Position messages are ignored before and after an accepted message") {
    val received = scala.collection.mutable.ArrayBuffer[World]()
    val target: World => Unit = message => received += message
    val kit = BehaviorTestKit(Consumer.actorType(target).asInstanceOf[Actor[World]])
    kit.run(Observable())
    kit.run(domains.terrestrial.Location())
    assert(received.isEmpty && kit.isAlive)
    kit.run(domains.aerial.Position())
    assert(received.size == 1)
    kit.run(Observable())
    kit.run(domains.terrestrial.Location())
    assert(received.size == 1 && kit.isAlive && kit.retrieveAllEffects().isEmpty)
  }

  test("provider capture remains lazy until a Position and occurs once per actor") {
    var captures = 0
    var deliveries = 0
    def provider(): World => Unit = { captures += 1; _ => deliveries += 1 }
    val kit = BehaviorTestKit(Consumer.actorType(provider()).asInstanceOf[Actor[World]])
    kit.run(Observable())
    kit.signal(PreRestart)
    assert(captures == 0 && deliveries == 0)
    kit.run(domains.aerial.Position())
    kit.run(domains.aerial.Position())
    assert(captures == 1 && deliveries == 2)
    val second = BehaviorTestKit(Consumer.actorType(provider()).asInstanceOf[Actor[World]])
    assert(captures == 1)
    second.run(domains.aerial.Position())
    assert(captures == 2 && deliveries == 3)
  }

  test("signals are inert and do not prevent a subsequent conversion") {
    var deliveries = 0
    val target: World => Unit = _ => deliveries += 1
    val kit = BehaviorTestKit(Consumer.actorType(target).asInstanceOf[Actor[World]])
    kit.signal(PreRestart)
    assert(deliveries == 0 && kit.isAlive && kit.retrieveAllEffects().isEmpty)
    kit.run(domains.aerial.Position())
    kit.signal(PreRestart)
    assert(deliveries == 1 && kit.isAlive && kit.retrieveAllEffects().isEmpty)
    kit.run(domains.aerial.Position())
    assert(deliveries == 2)
  }
}
