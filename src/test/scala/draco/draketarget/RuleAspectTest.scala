package draco.draketarget

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** The rule section of the drake surface is definition-backed (Dev, 2026-11-10; GitHub #11):
 *  `draketarget.RuleAspectText` owns its layout, `gendrake.RuleAspectOf` maps a definition
 *  onto it, and `Drake.emit` delegates — the third section to cross after the domain section
 *  (2026-09-22) and the type section (2026-09-28). Element rendering stays the Drake
 *  target's, supplied as an explicit dependency, so the generated types never call the engine.
 */
class RuleAspectTest extends AnyFunSuite with PersistentTestLog {

  private def render(td: TypeDefinition): String = Drake.ruleAspectText(td)

  /** The `rule` line and everything indented under it. */
  private def section(source: String): String = {
    val lines = source.linesIterator.dropWhile(_ != "rule").toList
    (lines.headOption.toSeq ++ lines.drop(1).takeWhile(_.startsWith(" "))).mkString("\n")
  }

  private val watch =
    """type Watch
      |domain example Domain
      |rule
      |  pattern
      |    variables
      |      var t Thing
      |      var o Other
      |    conditions
      |      con t.ready
      |  action
      |    fix seen Thing t
      |    mon ctx.insert(seen)
      |""".stripMargin

  test("the target renders the complete rule section in canonical order, eliding what is empty") {
    val text = RuleAspectText(Seq("      var t Thing"), Seq("      con t.ready"), Seq("    mon ctx.insert(t)")).value
    assert(text == "rule\n  pattern\n    variables\n      var t Thing\n    conditions\n      con t.ready\n  action\n    mon ctx.insert(t)")
    assert(RuleAspectText(Seq("      var t Thing")).value == "rule\n  pattern\n    variables\n      var t Thing")
    assert(RuleAspectText(_conditions = Seq("      con ready")).value == "rule\n  pattern\n    conditions\n      con ready")
    assert(RuleAspectText(_action = Seq("    mon fire()")).value == "rule\n  action\n    mon fire()")
  }

  test("role is presence: a present rule with nothing in it is the one line, an absent one renders nothing and evaluates nothing") {
    assert(RuleAspectText().value == "rule")
    assert(RuleAspectText(throw new AssertionError("an absent rule's lines were evaluated"), _present = false).value == "")
    val plain = TypeDefinition(TypeName("Plain", Seq("example")))
    assert(render(plain).isEmpty && section(Drake.emit(plain)).isEmpty)
    val empty = TypeDefinition(TypeName("Empty", Seq("example")), _ruleAspect = draco.drake.Present(RuleAspect.Null))
    assert(render(empty) == "rule")
    assert(section(Drake.emit(empty)) == "rule")
    assert(Drake.parse(Drake.emit(empty)).ruleAspect.fold(false, _ => true), "a present-empty rule survives the surface")
  }

  test("a definition's rule maps onto the section through the live emitter and reads back") {
    val td = Drake.parse(watch)
    assert(render(td) == section(watch))
    assert(Drake.emit(td) == watch)
    val back = Drake.parse(Drake.emit(td)).ruleAspect.fold(RuleAspect.Null, identity)
    assert(back.pattern.variables.map(_.name) == Seq("t", "o"))
    assert(back.pattern.conditions.size == 1 && back.action.body.size == 2)
  }

  test("every rule section in the main and mods corpus is reproduced, and no other definition gains one") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val paths = roots.flatMap { root =>
      Using.resource(Files.walk(Paths.get(root))) { stream =>
        stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json") && p.getFileName.toString != ".json").toList.sorted
      }
    }
    assert(paths.size >= 186, s"the corpus walk found only ${paths.size} definitions")
    var rules = 0
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      val expected = section(new String(Files.readAllBytes(authored), UTF_8))
      if (td.ruleAspect.fold(false, _ => true)) rules += 1
      assert(render(td) == expected, s"rule section differs: $path")
    }
    assert(rules >= 24, s"only $rules rule sections found")
    console.info(s"RuleAspect over the corpus: ${paths.size} definitions, $rules rule sections reproduced, 0 skipped")
  }

  test("the running generator rule emits the definition-backed rule section") {
    val td = Drake.parse(watch)
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("ruleAspect")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == watch)
  }
}
