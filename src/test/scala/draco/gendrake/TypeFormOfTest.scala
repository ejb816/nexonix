package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The drake surface's TYPE-FORM renderer is definition-backed (Dev, 2026-11-11; GitHub #11):
 *  `gendrake.TypeFormOf` maps a type form — a name leaf or one of the nine tree nodes — onto
 *  drake's own spelling, and `Drake.emit`'s `drakeType` delegates to it. It is the first of the
 *  emitter's renderers to cross, and the leaf of them: the element renderer calls it, it calls
 *  nothing but itself. The hand-written `TypeForm.neutral` stays as the text of a tree for every
 *  other reader, and is the reference this suite compares against over the corpus.
 */
class TypeFormOfTest extends AnyFunSuite with PersistentTestLog {

  private def leaf(name: String): Json = Json.fromString(name)
  private def node(op: String, members: Json*): Json = Json.obj(op -> Json.arr(members: _*))
  private def of(form: Json): String = TypeFormOf(Dracon(form)).value

  private val forms     = Set("->", "(,)", "[]", "{}", "[]+", "{}+", "()", "<:", ">:")
  private val typeSlots = Set("valueType", "typeParameters", "upperBound", "lowerBound", "derivation")

  test("each type form takes drake's own spelling, and a leaf is written as authored") {
    assert(of(leaf("Waypoint")) == "Waypoint" && of(leaf("draco.format.dracon.Dracon")) == "draco.format.dracon.Dracon")
    assert(of(node("[]", leaf("T"))) == "[T]" && of(node("[]+", leaf("T"))) == "[T]+")
    assert(of(node("{}", leaf("T"))) == "{T}" && of(node("{}", leaf("K"), leaf("V"))) == "{K, V}")
    assert(of(node("{}+", leaf("T"))) == "{T}+" && of(node("{}+", leaf("K"), leaf("V"))) == "{K, V}+")
    assert(of(node("(,)", leaf("A"), leaf("B"))) == "(A, B)" && of(node("(,)")) == "()")
    assert(of(node("()", leaf("Primal"), leaf("T"))) == "Primal(T)" && of(node("()", leaf("F"), leaf("A"), leaf("B"))) == "F(A, B)")
    assert(of(node("<:", leaf("T"), leaf("Product"))) == "T <: Product" && of(node(">:", leaf("V1"), leaf("V"))) == "V1 >: V")
    assert(of(node("()", leaf("Primal"), node("(,)", leaf("Gaze"), leaf("domains.sentient.Distance")))) == "Primal((Gaze, domains.sentient.Distance))")
  }

  test("an arrow groups to the right: only an arrow in its source takes parentheses") {
    val ab = node("->", leaf("A"), leaf("B"))
    assert(of(ab) == "A -> B")
    assert(of(node("->", leaf("A"), node("->", leaf("B"), leaf("C")))) == "A -> B -> C")
    assert(of(node("->", ab, leaf("C"))) == "(A -> B) -> C")
    assert(of(node("->", node("(,)", leaf("TypeElement"), leaf("Int")), node("[]", leaf("Text")))) == "(TypeElement, Int) -> [Text]")
    assert(of(node("->", leaf("M"), node("(,)"))) == "M -> ()")
  }

  test("the form is read once, and only when the text is read; no form and no node of the nine is the empty text") {
    var reads = 0
    val rendered = TypeFormOf({ reads += 1; Dracon(node("[]", leaf("T"))) })
    assert(reads == 0)
    assert(rendered.value == "[T]" && rendered.value == "[T]")
    assert(reads == 1)
    assert(TypeFormOf.Null.value == "" && of(Json.Null) == "")
    assert(of(node("+", leaf("a"), leaf("b"))) == "", "an expression node is not a type form")
  }

  test("the live emitter spells a type through the definition") {
    assert(Drake.typeSurface(node("[]", leaf("Waypoint"))) == "[Waypoint]")
    assert(Drake.typeSurface(node("->", leaf("A"), leaf("B"))) == "(A -> B)", "a value-type slot parenthesizes a top-level arrow")
    assert(of(node("(,)", leaf("S"), leaf("T"))) == "(S, T)", "a type parameter is a type form, with no spelling of its own since 2026-11-14")
    val td = TypeDefinition(TypeName("Carrier", Seq("example")),
      _dracoAspect = DracoAspect(_derivation = Seq(Dracon(node("{}", leaf("K"), leaf("V"))))))
    assert(Drake.emit(td) == "type Carrier from {K, V}\n")
  }

  /** Every type form under a type slot, trees at every depth and string leaves alike. A `value`
   *  under a type slot is an expression, not a type, and ends the slot — as TypeLeafTest reads it. */
  private def typeForms(json: Json, inType: Boolean): Seq[Json] = json.fold[Seq[Json]](
    Seq.empty,
    _ => Seq.empty,
    _ => Seq.empty,
    _ => if (inType) Seq(json) else Seq.empty,
    members => members.flatMap(typeForms(_, inType)),
    fields => {
      val own = if (inType && fields.size == 1 && forms.contains(fields.keys.head)) Seq(json) else Seq.empty
      own ++ fields.toList.flatMap { case (k, v) => typeForms(v, (inType && k != "value") || typeSlots.contains(k)) }
    }
  )

  test("every type form in the main and mods corpus is reproduced") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 195, s"the corpus walk found only ${definitions.size} definitions")

    var trees  = 0
    var leaves = 0
    val differing: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      typeForms(json, inType = false).flatMap { form =>
        if (TypeForm.isTree(form)) trees += 1 else leaves += 1
        val expected = TypeForm.text(form)
        val actual   = of(form)
        if (actual == expected) None else Some(s"$path: ${form.noSpaces}: '$actual' for '$expected'")
      }
    }
    differing.foreach(line => log.info(line))
    assert(trees >= 400 && leaves >= 1500, s"the walk found only $trees trees and $leaves leaves")
    console.info(s"TypeForm over the corpus: ${trees + leaves} type forms reproduced in ${definitions.size} definitions ($trees trees, $leaves leaves), ${differing.size} differing")
    assert(differing.isEmpty, "the definition spells a type form differently from TypeForm.neutral:\n  " + differing.mkString("\n  "))
  }
}
