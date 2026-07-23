package draco

import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

/** Falsifies the expression-rendering contract prototype ([[SourceContract]]) against
  * the two production renderers it generalizes: `ExpressionRenderer.render` with
  * `ScalaTemplates` must equal `Generator.expression`, and with `DrakeTemplates` must
  * equal `Generator.drakeExpression`, over a corpus exercising every operator. If the
  * engine+slots reproduce both, the slot boundary is proven — the two hand-written
  * renderers collapse to one engine plus three language slots. */
class SourceContractTest extends AnyFunSuite {

  private def op (name: String, args: Json*) : Json = Json.obj(name -> Json.arr(args: _*))
  private def s (v: String) : Json = Json.fromString(v)

  /** Trees valid on BOTH surfaces (no `=`, which only `Generator.expression` renders). */
  private val sharedTrees: Seq[Json] = Seq(
    op(".", s("a"), s("b"), s("c")),
    op("->", s("A"), s("B")),
    op("->", s("A"), s("B"), s("C")),
    op("()", s("f"), s("x"), s("y")),
    op("\\", s("x"), op("()", s("f"), s("x"))),                         // single param
    op("\\", s("x"), s("y"), op("()", s("g"), s("x"), s("y"))),         // multi param
    op("if", s("cond"), s("thenV"), s("elseV")),
    op("(,)", s("k"), s("v")),
    op("*", s("a"), s("b")),
    op("==", s("x"), s("y")),
    op("!=", s("x"), s("y")),
    op("()", s("f"), op("\\", s("j"), op("if", s("cond"), s("j"), s("k")))),  // nested
    s("\"Primes\""),                                                    // string literal leaf
    s("plain")                                                          // host-opaque leaf
  )

  test("ScalaTemplates reproduces Generator.expression over every operator") {
    sharedTrees.foreach { t =>
      assert(ExpressionRenderer.render(t, ScalaTemplates) == Generator.expression(t), s"mismatch on ${t.noSpaces}")
    }
  }

  test("DrakeTemplates reproduces Generator.drakeExpression over every operator") {
    sharedTrees.foreach { t =>
      assert(ExpressionRenderer.render(t, DrakeTemplates) == Generator.drakeExpression(t), s"mismatch on ${t.noSpaces}")
    }
  }

  test("the = (assignment) node is a Scala-only expression form") {
    val assign = op("=", s("name"), s("value"))
    assert(ExpressionRenderer.render(assign, ScalaTemplates) == Generator.expression(assign))
  }
}
