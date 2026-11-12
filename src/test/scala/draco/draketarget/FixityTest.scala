package draco.draketarget

import draco._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

/** Drake's operator fixity table is a definition (Dev, 2026-11-12; GitHub #11):
 *  `draketarget.Fixity` is the record (a precedence and how a run of one precedence groups) and
 *  carries the table as globals — `operators`, `table`, `flattened`. `Drake.parse` reassociates
 *  an operator run by it and `Drake.emit` decides the minimal parentheses by it; before this the
 *  table was a private value of the hand-written engine, which a definition in main could not
 *  read. The rows are pinned here as drake.dlt EXPRESSIONS states them (Haskell's Prelude, the
 *  arrow below every other row).
 */
class FixityTest extends AnyFunSuite {

  private val specified: Seq[(String, Int, String)] = Seq(
    ("*", 7, "left"), ("/", 7, "left"), ("%", 7, "left"),
    ("+", 6, "left"), ("-", 6, "left"),
    ("++", 5, "right"),
    ("==", 4, "none"), ("!=", 4, "none"), ("<", 4, "none"), ("<=", 4, "none"), (">", 4, "none"), (">=", 4, "none"),
    ("&&", 3, "right"),
    ("||", 2, "right"),
    ("->", 0, "right"))

  private def leaf(s: String): Json = Json.fromString(s)
  private def node(op: String, members: Json*): Json = Json.obj(op -> Json.arr(members: _*))

  private def surface(expression: String): String =
    s"type Probe\n  elements\n    fix v Boolean $expression\ndomain draco Draco\n"
  private def read(expression: String): Json =
    Drake.parse(surface(expression)).dracoAspect.elements.head.value.json
  private def written(expression: String): String =
    Drake.emit(Drake.parse(surface(expression))).linesIterator.find(_.trim.startsWith("fix v Boolean ")).get.trim.stripPrefix("fix v Boolean ")

  test("the table is the fifteen rows drake.dlt states, each operator once, three of them flat") {
    val rows = Fixity.operators.map { case (op, f) => (op, f.precedence, f.associativity) }
    assert(rows == specified)
    assert(Fixity.operators.map(_._1).distinct.size == 15)
    assert(Fixity.table.keySet == specified.map(_._1).toSet)
    specified.foreach { case (op, precedence, associativity) =>
      assert(Fixity.table(op).precedence == precedence && Fixity.table(op).associativity == associativity, op)
    }
    assert(Fixity.flattened == Set("++", "&&", "||"))
    assert(Fixity.flattened.forall(op => Fixity.table(op).associativity == "right"))
  }

  test("the parser reads every row: each operator is a node, and a run groups as its row says") {
    Fixity.operators.foreach { case (op, f) =>
      assert(read(s"a $op b") == node(op, leaf("a"), leaf("b")), op)
      f.associativity match {
        case "left"  => assert(read(s"a $op b $op c") == node(op, node(op, leaf("a"), leaf("b")), leaf("c")), op)
        case "right" if Fixity.flattened(op) => assert(read(s"a $op b $op c") == node(op, leaf("a"), leaf("b"), leaf("c")), op)
        case "right" => assert(read(s"a $op b $op c") == node(op, leaf("a"), node(op, leaf("b"), leaf("c"))), op)
        case _       => assertThrows[RuntimeException](read(s"a $op b $op c"))
      }
    }
    // a tighter row binds first, whichever side it stands on
    assert(read("a + b * c") == node("+", leaf("a"), node("*", leaf("b"), leaf("c"))))
    assert(read("a * b + c") == node("+", node("*", leaf("a"), leaf("b")), leaf("c")))
    assert(read("a == b && c < d || e") ==
      node("||", node("&&", node("==", leaf("a"), leaf("b")), node("<", leaf("c"), leaf("d"))), leaf("e")))
  }

  test("the emitter reads the same rows: a pair is written only where the table would regroup") {
    Seq("a + b * c", "(a + b) * c", "a - (b - c)", "a - b - c", "a ++ b ++ c", "(a == b) == c",
        "a && (b || c)", "a || b && c", "(a -> b) -> c", "a -> b -> c").foreach { e =>
      assert(written(e) == e, e)
    }
    // a redundant pair dissolves at parse and is not written back
    assert(written("(a * b) + c") == "a * b + c")
    assert(written("a -> (b -> c)") == "a -> b -> c")
    assert(written("a || (b && c)") == "a || b && c")
  }

  test("a fixity is a record: its factory arguments are read lazily and its codec round-trips") {
    var reads = 0
    val f = Fixity({ reads += 1; 6 }, "left")
    assert(reads == 0)
    assert(f.precedence == 6 && f.precedence == 6 && reads == 1)
    val json = Fixity.encoder(f)
    assert(json == Json.obj("precedence" -> Json.fromInt(6), "associativity" -> leaf("left")))
    val back = Fixity.decoder.decodeJson(json).toOption.get
    assert(back.precedence == 6 && back.associativity == "left")
  }
}
