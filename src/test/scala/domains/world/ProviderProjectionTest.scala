package domains.world

import draco._
import java.nio.file.{Files, Paths}
import org.apache.pekko.actor.testkit.typed.scaladsl.BehaviorTestKit
import org.apache.pekko.actor.typed.PreRestart
import org.scalatest.funsuite.AnyFunSuite

class ProviderProjectionTest extends AnyFunSuite {
  test("Provider's Drake, JSON and generated Scala agree exactly and declare the World actor role") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/world/Provider"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/world/Provider.scala"))
    assert(td.actorAspect.fold(false, _.messageType == World.typeDefinition.typeName))
  }

  test("target capture is lazy and evaluated once per actor when first used") {
    var captures = 0
    val received = scala.collection.mutable.ArrayBuffer[World]()
    def target(): World => Unit = { captures += 1; message => received += message }
    val actor = Provider.actorType(target()).asInstanceOf[Actor[World]]
    val kit = BehaviorTestKit(actor)
    assert(captures == 0)
    kit.signal(PreRestart)
    assert(captures == 0 && received.isEmpty)
    val message = Observable()
    kit.run(message)
    kit.run(message)
    assert(captures == 1 && received.size == 2)
    val second = BehaviorTestKit(Provider.actorType(target()).asInstanceOf[Actor[World]])
    assert(captures == 1)
    second.run(message)
    assert(captures == 2 && received.size == 3)
  }

  test("Provider forwards each World message unchanged exactly once and stays alive") {
    val received = scala.collection.mutable.ArrayBuffer[World]()
    val target: World => Unit = message => received += message
    val actor = Provider.actorType(target).asInstanceOf[Actor[World]]
    assert(actor.typeDefinition eq Provider.typeDefinition)
    val kit = BehaviorTestKit(actor)
    val messages: Seq[World] = Seq(Observable(), domains.aerial.Position(), domains.terrestrial.Location())
    for ((message, index) <- messages.zipWithIndex) {
      kit.run(message)
      assert(received.size == index + 1)
      assert(received.last eq message)
      assert(kit.isAlive)
      assert(kit.retrieveAllEffects().isEmpty)
    }
  }

  test("signals neither forward nor prevent a subsequent message") {
    val received = scala.collection.mutable.ArrayBuffer[World]()
    val target: World => Unit = message => received += message
    val kit = BehaviorTestKit(Provider.actorType(target).asInstanceOf[Actor[World]])
    kit.signal(PreRestart)
    assert(received.isEmpty && kit.isAlive)
    assert(kit.retrieveAllEffects().isEmpty)
    val message = Observable()
    kit.run(message)
    kit.signal(PreRestart)
    assert(received.size == 1 && (received.head eq message))
    assert(kit.isAlive && kit.retrieveAllEffects().isEmpty)
    kit.run(message)
    assert(received.size == 2 && (received.last eq message))
  }
}
