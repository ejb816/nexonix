package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The refusal of a value outside drake's tree language is a definition (Dev, 2026-11-14;
 *  GitHub #11): `gendrake.ValueRefusalOf` maps a value onto the reasons it cannot be spelled —
 *  an operator that is neither structural nor in `draketarget.Fixity`, a value that is neither
 *  a text leaf nor a single-key node — at any depth, with the messages the emitter raised by
 *  hand. The reasons are texts, there being no definition to name at `Drake.expression`;
 *  `gendrake.ElementRefusalOf` wraps each into a Problem on its subject. It reads a node's own
 *  key and its compact text through `Dracon.keys` and `Dracon.compact`.
 */
class ValueRefusalOfTest extends AnyFunSuite {

  private def leaf(text: String): Json = Json.fromString(text)
  private def node(op: String, operands: Json*): Json = Json.obj(op -> Json.arr(operands: _*))
  private def of(value: Json): Seq[String] = ValueRefusalOf(Dracon(value)).reasons
  private val (a, b, c) = (leaf("a"), leaf("b"), leaf("c"))
  private def unknown(op: String, value: Json) = s"Drake.expression: unknown operator '$op' in ${value.noSpaces}"
  private def unrenderable(value: Json) = s"Drake.expression: unrenderable value ${value.noSpaces}"

  test("a value inside the tree language has no reasons") {
    assert(of(Json.Null).isEmpty && of(a).isEmpty && ValueRefusalOf(Dracon.Null).reasons.isEmpty)
    Seq(".", "()", "\\", "(,)", "[]", "{}", "=").foreach(op => assert(of(node(op, a, b)).isEmpty, s"structural $op"))
    draketarget.Fixity.table.keys.foreach(op => assert(of(node(op, a, b)).isEmpty, s"operator $op"))
    assert(of(node("()", node(".", node("()", leaf("guard"), node("==", a, b)), leaf("ifThenElse")), node("+", a, b), c)).isEmpty)
    assert(of(Json.obj("()" -> a)).isEmpty, "a single operand that is not a list is one operand")
  }

  test("an unknown operator is named, with the whole node") {
    val typed = node("<:", a, b)
    assert(of(typed) == Seq(unknown("<:", typed)))
    assert(of(node("?", a)) == Seq(unknown("?", node("?", a))))
  }

  test("a value that is neither a leaf nor a single-key node is unrenderable") {
    Seq(Json.fromInt(1), Json.True, Json.arr(a, b), Json.obj(), Json.obj("+" -> Json.arr(a, b), "-" -> Json.arr(a, b)))
      .foreach(value => assert(of(value) == Seq(unrenderable(value))))
  }

  test("the refusal reaches every depth, and reports every reason in order") {
    val inner = node("?", b, c)
    assert(of(node("+", a, inner)) == Seq(unknown("?", inner)))
    val outer = node("<:", inner, Json.fromInt(1))
    assert(of(outer) == Seq(unknown("<:", outer), unknown("?", inner), unrenderable(Json.fromInt(1))))
    assert(of(Json.obj("()" -> Json.fromInt(1))) == Seq(unrenderable(Json.fromInt(1))))
  }

  test("the value is read once, and only when the reasons are read") {
    var reads = 0
    val refusal = ValueRefusalOf({ reads += 1; Dracon(node("<:", a, b)) })
    assert(reads == 0)
    assert(refusal.reasons.size == 1 && refusal.reasons.size == 1)
    assert(reads == 1)
  }

  test("the live entry point raises the first reason, and no value of the corpus is refused") {
    val typed = node("<:", a, b)
    assert(intercept[IllegalArgumentException](Drake.expression(typed)).getMessage == unknown("<:", typed))
    assert(intercept[IllegalArgumentException](Drake.expression(Json.fromInt(1))).getMessage == unrenderable(Json.fromInt(1)))
    assert(Drake.expression(node("+", a, b)) == "a + b" && Drake.expression(Json.Null) == "")

    def values(json: Json): Seq[Json] = json.fold[Seq[Json]](
      Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
      members => members.flatMap(values),
      fields => fields.toList.flatMap { case (key, value) => if (key == "value") Seq(value) else values(value) })
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json").map(_.toString).toSeq
    }.sorted
    val all = definitions.flatMap(path => io.circe.parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), values).map(path -> _))
    assert(definitions.size >= 204 && all.size >= 690, s"the walk found ${definitions.size} definitions and ${all.size} values")
    val refused = all.flatMap { case (path, value) => of(value).map(reason => s"$path: $reason") }
    assert(refused.isEmpty, "a committed value is outside the tree language:\n  " + refused.mkString("\n  "))
  }
}
