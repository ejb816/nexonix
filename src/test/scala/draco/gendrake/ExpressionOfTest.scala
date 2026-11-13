package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The drake surface's EXPRESSION renderer is definition-backed (Dev, 2026-11-12; GitHub #11):
 *  `gendrake.ExpressionOf` maps a value — a leaf or a single-key `{op: [operands]}` node — onto
 *  drake's own spelling, writing a pair of parentheses only where the fixity table
 *  (`draketarget.Fixity`) says the surface would otherwise regroup, and `Drake.expression`
 *  delegates to it after refusing a value outside the tree language. Over the corpus the
 *  reference is the AUTHORED SURFACE: every value's spelling closes a line of its definition's
 *  `.drake` (the hand-written renderer that was the reference until 2026-11-13 is removed).
 */
class ExpressionOfTest extends AnyFunSuite with PersistentTestLog {

  private def leaf(name: String): Json = Json.fromString(name)
  private def node(op: String, members: Json*): Json = Json.obj(op -> Json.arr(members: _*))
  private def of(value: Json): String = ExpressionOf(Dracon(value)).value

  private val (a, b, c) = (leaf("a"), leaf("b"), leaf("c"))

  test("each node takes drake's own spelling, and a leaf is written as authored") {
    assert(of(leaf("ctx.getRuntime()")) == "ctx.getRuntime()" && of(leaf("\"quoted, text\"")) == "\"quoted, text\"")
    assert(of(node(".", a, b, c)) == "a.b.c")
    assert(of(node("()", leaf("f"), a, b)) == "f(a, b)" && of(node("()", leaf("f"))) == "f()")
    assert(of(node("()", leaf("f"), a, node("=", leaf("name"), b))) == "f(a, name:b)")
    assert(of(node("\\", leaf("x"), leaf("y"), node("+", leaf("x"), leaf("y")))) == "\\x y -> x + y")
    assert(of(node("(,)", a, b)) == "(a, b)" && of(node("(,)")) == "()")
    assert(of(node("[]", a, b)) == "[a, b]" && of(node("[]")) == "[]")
    assert(of(node("{}", a, b)) == "{a, b}")
    draco.draketarget.Fixity.operators.foreach { case (op, _) => assert(of(node(op, a, b)) == s"a $op b", op) }
    assert(of(node("++", a, b, c)) == "a ++ b ++ c")
  }

  test("a pair is written only where the fixity table would regroup the surface") {
    val sum = node("+", a, b)
    assert(of(node("*", sum, c)) == "(a + b) * c" && of(node("+", node("*", a, b), c)) == "a * b + c")
    assert(of(node("-", node("-", a, b), c)) == "a - b - c" && of(node("-", a, node("-", b, c))) == "a - (b - c)")
    assert(of(node("->", a, node("->", b, c))) == "a -> b -> c" && of(node("->", node("->", a, b), c)) == "(a -> b) -> c")
    assert(of(node("==", node("==", a, b), c)) == "(a == b) == c" && of(node("==", a, node("==", b, c))) == "a == (b == c)")
    assert(of(node("||", a, node("&&", b, c))) == "a || b && c" && of(node("&&", a, node("||", b, c))) == "a && (b || c)")
    // a lambda runs to the end, so it is paired only when something follows it
    val lambda = node("\\", leaf("x"), leaf("x"))
    assert(of(node("++", a, lambda)) == "a ++ \\x -> x" && of(node("++", lambda, a)) == "(\\x -> x) ++ a")
    // a receiver or an applied head that is an operator run or a lambda takes its pair; arguments never do
    assert(of(node(".", sum, leaf("size"))) == "(a + b).size" && of(node("()", lambda, a)) == "(\\x -> x)(a)")
    assert(of(node("()", leaf("f"), sum, lambda)) == "f(a + b, \\x -> x)")
    assert(of(node("(,)", sum, node("[]", sum))) == "(a + b, [a + b])")
  }

  test("the value is read once, and only when the text is read; no value and no node of the language is the empty text") {
    var reads = 0
    val rendered = ExpressionOf({ reads += 1; Dracon(node("+", a, b)) })
    assert(reads == 0)
    assert(rendered.value == "a + b" && rendered.value == "a + b")
    assert(reads == 1)
    assert(ExpressionOf.Null.value == "" && of(Json.Null) == "")
    assert(of(node("<:", a, b)) == "", "a type-form node is not an expression")
  }

  test("the live emitter spells a value through the definition, and still refuses what is outside the language") {
    val value = node("()", node(".", node("()", leaf("guard"), node("==", a, b)), leaf("ifThenElse")), node("+", a, b), c)
    assert(Drake.expression(value) == "guard(a == b).ifThenElse(a + b, c)")
    assert(Drake.expression(value) == of(value))
    assert(Drake.expression(Json.Null) == "")
    assertThrows[RuntimeException](Drake.expression(node("<:", a, b)))
    assertThrows[RuntimeException](Drake.expression(node("+", a, node("?", b, c))))
    assertThrows[RuntimeException](Drake.expression(Json.fromInt(1)))
    assertThrows[RuntimeException](Drake.expression(Json.obj("+" -> Json.arr(a, b), "-" -> Json.arr(a, b))))
  }

  /** Every `value` a definition carries, at any depth: an element's, a parameter's, a body binding's. */
  private def values(json: Json): Seq[Json] = json.fold[Seq[Json]](
    Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
    members => members.flatMap(values),
    fields => fields.toList.flatMap { case (k, v) => if (k == "value") Seq(v) else values(v) }
  )

  test("every value in the main and mods corpus is reproduced") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 197, s"the corpus walk found only ${definitions.size} definitions")

    var trees  = 0
    var leaves = 0
    val differing: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      val surface = Files.readAllLines(Paths.get(path.stripSuffix(".json") + ".drake")).asScala.map(_.trim).toSet
      values(json).filterNot(_.isNull).flatMap { value =>
        if (value.isObject) trees += 1 else leaves += 1
        val actual = of(value)
        if (actual.isEmpty || surface.exists(line => line == actual || line.endsWith(" " + actual))) None
        else Some(s"$path: ${value.noSpaces}: '$actual' closes no line of the authored surface")
      }
    }
    differing.foreach(line => log.info(line))
    assert(trees >= 400 && leaves >= 150, s"the walk found only $trees trees and $leaves leaves")
    console.info(s"Expression over the corpus: ${trees + leaves} values reproduced in ${definitions.size} definitions ($trees trees, $leaves leaves), ${differing.size} differing")
    assert(differing.isEmpty, "the definition spells a value differently from its authored surface:\n  " + differing.mkString("\n  "))
  }
}
