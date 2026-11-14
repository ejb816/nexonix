package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The spelling of a FORMAL type parameter is definition-backed (Dev, 2026-11-13; GitHub #11):
 *  `gendrake.FormalOf` maps one `TypeParameter` — a name, an optional lower and upper bound, each
 *  a type form — onto drake's own text, `V1 >: V`, `T <: Product`. It was spelled twice until
 *  then: for a type header inside `DracoAspectOf`, and for a method by hand in the emitter. Both
 *  read it now, and `ElementOf` receives a method's formals as data instead of a spelled name.
 */
class FormalOfTest extends AnyFunSuite with PersistentTestLog {

  private def leaf(name: String): Json = Json.fromString(name)
  private def node(op: String, members: Json*): Json = Json.obj(op -> Json.arr(members: _*))
  private def of(formal: TypeParameter): String = FormalOf(formal).value
  private def formal(name: String, upper: Json = Json.Null, lower: Json = Json.Null): TypeParameter =
    TypeParameter(name, Dracon(upper), Dracon(lower))

  test("a formal is its name, then its lower bound, then its upper bound, each as a type form") {
    assert(of(formal("R")) == "R")
    assert(of(formal("T", leaf("Product"))) == "T <: Product")
    assert(of(formal("V1", lower = leaf("V"))) == "V1 >: V")
    assert(of(formal("T", leaf("Hi"), leaf("Lo"))) == "T >: Lo <: Hi", "the lower bound reads first")
    assert(of(formal("F", node("[]", leaf("T")))) == "F <: [T]")
    assert(of(formal("F", node("->", node("->", leaf("A"), leaf("B")), leaf("C")))) == "F <: (A -> B) -> C")
  }

  test("the formal is read once, and only when the text is read") {
    var reads = 0
    val rendered = FormalOf({ reads += 1; formal("T", leaf("Product")) })
    assert(reads == 0)
    assert(rendered.value == "T <: Product" && rendered.value == "T <: Product")
    assert(reads == 1)
  }

  private val probe: String =
    """type Holder(T <: Product, S)
      |  elements
      |    dyn updated(V1 >: V) {K, V1} [
      |      parameters
      |        par key K
      |      body
      |        fix value {K, V1} this
      |    ]
      |    dyn plain Int 1
      |    dyn fold(R) R
      |domain draco Draco
      |""".stripMargin

  test("the live emitter spells a header's and a method's formals through the definition") {
    val read = Drake.parse(probe)
    assert(read.dracoAspect.typeParameters.map(of) == Seq("T <: Product", "S"))
    assert(Drake.emit(read) == probe)
    assert(Drake.dracoAspectText(read).linesIterator.next() == "type Holder(T <: Product, S)")
  }

  test("the formals are read from the element, and the emitter still refuses invalid ones") {
    val read = Drake.parse(probe)
    val methods = read.dracoAspect.elements
    assert(methods.map(_.typeParameters.map(_.name)) == Seq(Seq("V1"), Seq.empty, Seq("R")))
    val json = TypeDefinition.encoder(read)
    val doubled = json.hcursor.downField("dracoAspect").downField("elements").downN(2).downField("typeParameters")
      .withFocus(formals => Json.arr(formals.asArray.get ++ formals.asArray.get: _*)).top.get
    val broken = doubled.as[TypeDefinition].fold(error => fail(error.toString), identity)
    assertThrows[RuntimeException](Drake.emit(broken))
  }

  /** Every list of formal records a definition carries, at any depth: a header's, a method's. */
  private def formalLists(json: Json): Seq[Vector[Json]] = json.fold[Seq[Vector[Json]]](
    Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
    members => members.flatMap(formalLists),
    fields => fields.toList.flatMap { case (k, v) =>
      val records = v.asArray.filter(a => k == "typeParameters" && a.nonEmpty && a.forall(m => m.isObject && m.hcursor.downField("name").succeeded))
      records.map(Seq(_)).getOrElse(formalLists(v))
    }
  )

  test("every formal in the main and mods corpus is reproduced") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 200, s"the corpus walk found only ${definitions.size} definitions")

    var lists   = 0
    var formals = 0
    var bounded = 0
    val differing: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      val surface = Files.readString(Paths.get(path.stripSuffix(".json") + ".drake"))
      formalLists(json).flatMap { records =>
        val parameters = records.map(_.as[TypeParameter].fold(error => fail(s"$path: $error"), identity))
        lists += 1
        formals += parameters.size
        bounded += parameters.count(p => !p.upperBound.isNull || !p.lowerBound.isNull)
        val actual = parameters.map(of).mkString("(", ", ", ")")
        if (surface.contains(actual)) None else Some(s"$path: '$actual' is not in the authored surface")
      }
    }
    differing.foreach(line => log.info(line))
    assert(lists >= 10 && formals >= 10, s"the walk found only $lists lists and $formals formals")
    console.info(s"Formal over the corpus: $formals formals reproduced in ${definitions.size} definitions ($lists lists, $bounded bounded), ${differing.size} differing")
    assert(differing.isEmpty, "the definition spells a formal differently from its authored surface:\n  " + differing.mkString("\n  "))
  }
}
