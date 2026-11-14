package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The drake surface's ELEMENT renderer is definition-backed (Dev, 2026-11-12; GitHub #11):
 *  `gendrake.ElementOf` maps one element at a level onto its member lines — a leaf line, a
 *  dyn block bracketing its `parameters` and `body`, a case-branch bracketing its statements —
 *  reading the keyword from `draketarget.Keyword`, the type from `TypeFormOf` and the value
 *  from `ExpressionOf`. The name is the one explicit dependency: a method's formals live on
 *  Dynamic alone. `Drake.emit` refuses what the renderer refused and then delegates. Over the
 *  corpus the reference is the AUTHORED SURFACE: every member's lines stand as one block in its
 *  definition's `.drake` (the hand-written renderer that was the reference until 2026-11-13 is removed).
 */
class ElementOfTest extends AnyFunSuite with PersistentTestLog {

  private def of(element: TypeElement, level: Int): Seq[String] = ElementOf(element, level).lines

  private val probe: String =
    """type Probe
      |  elements
      |    fix a Int 1
      |    mut b Int
      |    now fix c (Int -> Int) \x -> x + 1
      |    dyn d Int [
      |      parameters
      |        par x Int
      |        now par y [Int] []
      |      body
      |        loc z Int x
      |        mon println(z)
      |        fix value Int z
      |    ]
      |    dyn e Int a + 1
      |domain draco Draco
      |rule
      |  pattern
      |    variables
      |      var v Integer
      |    conditions
      |      con v > 0
      |  action
      |    mon println(v)
      |actor
      |  messageType String
      |  message
      |    case s String [
      |      mon println(s)
      |    ]
      |    case [
      |      mon println(msg)
      |    ]
      |""".stripMargin

  private def decoded(json: Json): TypeDefinition = json.as[TypeDefinition].fold(error => fail(error.toString), identity)

  /** Every element a definition's sections hand to the renderer, with the level they give it. */
  private def members(td: TypeDefinition): Seq[(TypeElement, Int)] = {
    val aspect = td.dracoAspect
    val rule   = td.ruleAspect.fold(Seq.empty[(TypeElement, Int)], r =>
      r.pattern.variables.map(e => (e: TypeElement, 3)) ++ r.pattern.conditions.map(e => (e: TypeElement, 3)) ++ r.action.body.map(e => (e: TypeElement, 2)))
    val actor  = td.actorAspect.fold(Seq.empty[(TypeElement, Int)], a =>
      (a.start.body ++ a.message.body ++ a.signal.body).map(e => (e: TypeElement, 2)))
    aspect.elements.map(e => (e: TypeElement, 2)) ++ aspect.factory.parameters.map(e => (e: TypeElement, 3)) ++
      aspect.factory.body.map(e => (e: TypeElement, 3)) ++ aspect.globalElements.map(e => (e: TypeElement, 2)) ++ rule ++ actor
  }

  private def size(element: TypeElement): Int = 1 + (element.parameters ++ element.body).map(size).sum

  test("a leaf is one line: keyword, name, type slot and value, each only when it has one") {
    val elements = Drake.parse(probe).dracoAspect.elements
    assert(of(elements(0), 2) == Seq("    fix a Int 1"))
    assert(of(elements(1), 2) == Seq("    mut b Int"))
    assert(of(elements(2), 2) == Seq("    now fix c (Int -> Int) \\x -> x + 1"), "a strict element, an arrow type in its pair")
    assert(of(elements(4), 0) == Seq("dyn e Int a + 1"), "a dyn with neither parameters nor body is a leaf")
    val rule = Drake.parse(probe).ruleAspect.fold(fail("no rule"), identity)
    assert(of(rule.pattern.variables.head, 3) == Seq("      var v Integer"))
    assert(of(rule.pattern.conditions.head, 3) == Seq("      con v > 0"), "a condition carries a value only")
    assert(of(rule.action.body.head, 2) == Seq("    mon println(v)"))
  }

  test("a dyn block and a case-branch bracket themselves and indent what they hold") {
    val read = Drake.parse(probe)
    assert(of(read.dracoAspect.elements(3), 1) == Seq(
      "  dyn d Int [",
      "    parameters",
      "      par x Int",
      "      now par y [Int] []",
      "    body",
      "      loc z Int x",
      "      mon println(z)",
      "      fix value Int z",
      "  ]"))
    val message = read.actorAspect.fold(fail("no actor"), identity).message.body
    assert(of(message(0), 2) == Seq("    case s String [", "      mon println(s)", "    ]"))
    assert(of(message(1), 2) == Seq("    case [", "      mon println(msg)", "    ]"), "no binder and no type leave no marker")
  }

  test("the lines are read on demand, and a method's formals are read from the element") {
    var reads = 0
    val element = Drake.parse(probe).dracoAspect.elements.head
    val rendered = ElementOf({ reads += 1; element }, 0)
    assert(reads == 0)
    assert(rendered.lines == Seq("fix a Int 1") && rendered.lines.size == 1 && reads == 1, "no formals leave the name as authored")
    assert(element.typeParameters.isEmpty, "every kind but a method carries the empty default")
    val method = Dynamic("a", Dracon.string("Int"), _value = Dracon.string("1"),
      _typeParameters = Seq(TypeParameter("T"), TypeParameter("S", Dracon.string("Product"))))
    assert(ElementOf(method, 0).lines == Seq("dyn a(T, S <: Product) Int 1"))
  }

  test("the live emitter spells elements through the definition and still refuses what it refused") {
    val read = Drake.parse(probe)
    assert(Drake.emit(read) == probe)
    val json = TypeDefinition.encoder(read)
    assert(Drake.emit(decoded(json)) == probe)
    def withElement(index: Int, change: Json => Json): TypeDefinition = decoded(
      json.hcursor.downField("dracoAspect").downField("elements").downN(index).withFocus(change).top.get)
    // a block dyn carrying a value outside its body
    assertThrows[RuntimeException](Drake.emit(withElement(3, _.mapObject(_.add("value", Json.fromString("1"))))))
    // a value outside the tree language, at the top and inside a block
    val unknown = Json.obj("??" -> Json.arr(Json.fromString("a")))
    assertThrows[RuntimeException](Drake.emit(withElement(0, _.mapObject(_.add("value", unknown)))))
    assertThrows[RuntimeException](Drake.emit(withElement(3, _.hcursor.downField("body").downN(0).withFocus(_.mapObject(_.add("value", unknown))).top.get)))
    // a kind with no member line
    assertThrows[RuntimeException](Drake.emit(withElement(0, _.mapObject(_.add("kind", Json.fromString("Action"))))))
  }

  test("every element in the main and mods corpus is reproduced") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 199, s"the corpus walk found only ${definitions.size} definitions")

    var elements = 0
    var lines    = 0
    val differing: Seq[String] = definitions.flatMap { path =>
      val td = decoded(parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity))
      val surface = Files.readAllLines(Paths.get(path.stripSuffix(".json") + ".drake")).asScala.toSeq
      members(td).flatMap { case (element, level) =>
        elements += size(element)
        val actual = of(element, level)
        lines += actual.size
        if (actual.nonEmpty && surface.containsSlice(actual)) None
        else Some(s"$path: '${element.name}' is no block of the authored surface:\n    ${actual.mkString("\n    ")}")
      }
    }
    differing.foreach(line => log.info(line))
    assert(elements >= 1000 && lines >= 1000, s"the walk found only $elements elements and $lines lines")
    console.info(s"Element over the corpus: $elements elements reproduced in ${definitions.size} definitions ($lines lines), ${differing.size} differing")
    assert(differing.isEmpty, "the definition spells an element differently from its authored surface:\n  " + differing.mkString("\n  "))
  }
}
