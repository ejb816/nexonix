package domains.terrestrial

import draco._
import java.nio.file.{Files, Paths}
import org.apache.pekko.actor.testkit.typed.scaladsl.BehaviorTestKit
import org.apache.pekko.actor.typed.PreRestart
import org.scalatest.funsuite.AnyFunSuite

/** Output's conversion of a World `Location` into a `LocationReport` lives in its definition:
  * a `case location Location` branch in the actor's message body. These tests pin the trio and
  * exercise the regenerated actor through BehaviorTestKit.
  */
class OutputProjectionTest extends AnyFunSuite {
  private type World = domains.world.World
  private type Dracon = draco.format.dracon.Dracon

  private def received(): (scala.collection.mutable.ArrayBuffer[Dracon], Dracon => Unit) = {
    val buffer = scala.collection.mutable.ArrayBuffer[Dracon]()
    (buffer, report => buffer += report)
  }

  private def kitFor(consumer: => Dracon => Unit): BehaviorTestKit[World] = {
    val actor = Output.actorType(consumer).asInstanceOf[Actor[World]]
    assert(actor.typeDefinition eq Output.typeDefinition)
    BehaviorTestKit(actor)
  }

  test("Output's Drake, JSON and generated Scala agree exactly and declare the World actor role") {
    def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")
    val stem = "src/mods/resources/domains/terrestrial/Output"
    val source = read(stem + ".drake")
    val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
    assert(Drake.emit(td) == source)
    assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td))
    assert(DracoGenerator.generate(td) == read("src/mods/scala/domains/terrestrial/Output.scala"))
    assert(td.actorAspect.fold(false, _.messageType == domains.world.World.typeDefinition.typeName))
  }

  test("each Location produces one LocationReport carrying type, latitude, longitude and elevationMetres") {
    val (reports, consumer) = received()
    val kit = kitFor(consumer)
    val cases = Seq((51.5, -0.12, 10668), (0.0, 0.0, 0), (-33.9, 151.2, -3))
    for (((lat, lon, metres), index) <- cases.zipWithIndex) {
      kit.run(Location(lat, lon, metres))
      assert(reports.size == index + 1)
      val report = reports.last
      assert(report.isInstanceOf[LocationReport])
      assert(report.typeDefinition eq LocationReport.typeDefinition)
      val cursor = report.json.hcursor
      assert(cursor.get[String]("type").contains("LOCATION"))
      assert(cursor.get[Double]("latitude").contains(lat))
      assert(cursor.get[Double]("longitude").contains(lon))
      assert(cursor.get[Int]("elevationMetres").contains(metres))
      assert(cursor.keys.map(_.toSeq).contains(Seq("type", "latitude", "longitude", "elevationMetres")))
      assert(kit.isAlive && kit.retrieveAllEffects().isEmpty)
    }
  }

  test("non-Location World messages are ignored before and after an accepted Location") {
    val (reports, consumer) = received()
    val kit = kitFor(consumer)
    kit.run(domains.world.Observable())
    kit.run(domains.aerial.Position())
    assert(reports.isEmpty && kit.isAlive)
    kit.run(Location())
    assert(reports.size == 1)
    kit.run(domains.world.Observable())
    kit.run(domains.aerial.Position())
    assert(reports.size == 1 && kit.isAlive && kit.retrieveAllEffects().isEmpty)
  }

  test("consumer capture remains lazy until a Location and occurs once per actor") {
    var captures = 0
    var deliveries = 0
    def consumer(): Dracon => Unit = { captures += 1; _ => deliveries += 1 }
    val kit = kitFor(consumer())
    kit.run(domains.world.Observable())
    kit.signal(PreRestart)
    assert(captures == 0 && deliveries == 0)
    kit.run(Location())
    kit.run(Location())
    assert(captures == 1 && deliveries == 2)
    val second = kitFor(consumer())
    assert(captures == 1)
    second.run(Location())
    assert(captures == 2 && deliveries == 3)
  }
}
