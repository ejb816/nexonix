package draco.draketarget

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** The actor section of the drake surface is definition-backed (Dev, 2026-11-10; GitHub #11):
 *  `draketarget.ActorAspectText` owns its layout, `gendrake.ActorAspectOf` maps a definition
 *  onto it, and `Drake.emit` delegates — the fourth section to cross, after the domain, type
 *  and rule sections. The message type an actor's ASPECT carries is spelled as the type
 *  section spells a reference: bare in the actor's own package, with its package words
 *  elsewhere. Type-parameter spelling and element rendering stay the Drake target's,
 *  supplied as explicit dependencies.
 */
class ActorAspectTest extends AnyFunSuite with PersistentTestLog {

  private def render(td: TypeDefinition): String = Drake.actorAspectText(td)

  /** The `actor` line and everything indented under it. */
  private def section(source: String): String = {
    val lines = source.linesIterator.dropWhile(_ != "actor").toList
    (lines.headOption.toSeq ++ lines.drop(1).takeWhile(_.startsWith(" "))).mkString("\n")
  }

  private def relay(messageType: String): String =
    s"""type Relay
       |domain example Domain
       |actor
       |  messageType $messageType
       |  start
       |    fix seen [String]+ []
       |  message
       |    mon seen.append(msg.toString)
       |  signal
       |    mon seen.clear()
       |""".stripMargin

  test("the target renders the complete actor section in canonical order, eliding what is empty") {
    val text = ActorAspectText("example other Signal", Seq("    fix seen [String]+ []"), Seq("    mon seen.append(msg)"), Seq("    mon seen.clear()")).value
    assert(text == "actor\n  messageType example other Signal\n  start\n    fix seen [String]+ []\n  message\n    mon seen.append(msg)\n  signal\n    mon seen.clear()")
    assert(ActorAspectText("Signal").value == "actor\n  messageType Signal")
    assert(ActorAspectText(_message = Seq("    mon fire()")).value == "actor\n  message\n    mon fire()")
    assert(ActorAspectText(_start = Seq("    mon open()"), _signal = Seq("    mon close()")).value == "actor\n  start\n    mon open()\n  signal\n    mon close()")
  }

  test("role is presence: a present actor with nothing in it is the one line, an absent one renders nothing and evaluates nothing") {
    assert(ActorAspectText().value == "actor")
    assert(ActorAspectText(throw new AssertionError("an absent actor's message type was evaluated"), _present = false).value == "")
    val plain = TypeDefinition(TypeName("Plain", Seq("example")))
    assert(render(plain).isEmpty && section(Drake.emit(plain)).isEmpty)
    val empty = TypeDefinition(TypeName("Empty", Seq("example")), _actorAspect = draco.drake.Present(ActorAspect.Null))
    assert(render(empty) == "actor")
    assert(section(Drake.emit(empty)) == "actor")
    assert(Drake.parse(Drake.emit(empty)).actorAspect.fold(false, _ => true), "a present-empty actor survives the surface")
  }

  test("the message type is bare in the actor's own package and carries its package words elsewhere") {
    val own = Drake.parse(relay("Signal"))
    assert(own.actorAspect.fold(ActorAspect.Null, identity).messageType == TypeName("Signal", Seq("example")))
    assert(render(own) == section(relay("Signal")))
    assert(Drake.emit(own) == relay("Signal"))

    val other = Drake.parse(relay("example other Signal"))
    assert(other.actorAspect.fold(ActorAspect.Null, identity).messageType == TypeName("Signal", Seq("example", "other")))
    assert(render(other) == section(relay("example other Signal")))
    assert(Drake.emit(other) == relay("example other Signal"))
    val back = Drake.parse(Drake.emit(other)).actorAspect.fold(ActorAspect.Null, identity)
    assert(back.start.body.size == 1 && back.message.body.size == 1 && back.signal.body.size == 1)
  }

  test("every actor section in the main and mods corpus is reproduced, and no other definition gains one") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val paths = roots.flatMap { root =>
      Using.resource(Files.walk(Paths.get(root))) { stream =>
        stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json") && p.getFileName.toString != ".json").toList.sorted
      }
    }
    assert(paths.size >= 188, s"the corpus walk found only ${paths.size} definitions")
    var actors = 0
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      val expected = section(new String(Files.readAllBytes(authored), UTF_8))
      if (td.actorAspect.fold(false, _ => true)) actors += 1
      assert(render(td) == expected, s"actor section differs: $path")
    }
    assert(actors >= 15, s"only $actors actor sections found")
    console.info(s"ActorAspect over the corpus: ${paths.size} definitions, $actors actor sections reproduced, 0 skipped")
  }

  test("the running generator rule emits the definition-backed actor section") {
    val td = Drake.parse(relay("example other Signal"))
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("actorAspect")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == relay("example other Signal"))
  }
}
