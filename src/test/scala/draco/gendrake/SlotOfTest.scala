package draco.gendrake

import draco._
import draco.format.dracon.Dracon
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The spelling of a VALUE-TYPE SLOT is definition-backed (Dev, 2026-11-14; GitHub #11):
 *  `gendrake.SlotOf` maps a type form onto the text it takes between a member's name and its
 *  value — the type form's own spelling, parenthesized when it is a function arrow at the top,
 *  so the slot reads as one group. The rule was written twice until then, in `ElementOf` and by
 *  hand in the emitter; both read it now, and with it the section transforms spell their own
 *  types: `DracoAspectOf`, `DomainAspectOf`, `ActorAspectOf` and `DefinitionOf` no longer
 *  receive a type spelling from the emitter.
 */
class SlotOfTest extends AnyFunSuite with PersistentTestLog {

  private def leaf(name: String): Json = Json.fromString(name)
  private def node(op: String, members: Json*): Json = Json.obj(op -> Json.arr(members: _*))
  private def of(form: Json): String = SlotOf(Dracon(form)).value

  test("a slot is its type form, parenthesized only when an arrow is at the top") {
    assert(of(leaf("Int")) == "Int")
    assert(of(node("[]", leaf("T"))) == "[T]")
    assert(of(node("->", leaf("A"), leaf("B"))) == "(A -> B)")
    assert(of(node("->", node("(,)", leaf("TypeElement"), leaf("Int")), node("[]", leaf("Text")))) == "((TypeElement, Int) -> [Text])")
    assert(of(node("()", leaf("Presence"), node("->", leaf("A"), leaf("B")))) == "Presence(A -> B)", "an arrow inside an application needs no pair")
    assert(of(node("(,)")) == "()", "the unit type")
    assert(of(Json.Null) == "" && SlotOf.Null.value == "", "an absent type is the empty text")
  }

  test("the form is read once, and only when the text is read") {
    var reads = 0
    val rendered = SlotOf({ reads += 1; Dracon(node("->", leaf("A"), leaf("B"))) })
    assert(reads == 0)
    assert(rendered.value == "(A -> B)" && rendered.value == "(A -> B)")
    assert(reads == 1)
  }

  private val probe: String =
    """type Holder(T) from [T]
      |  elements
      |    fix apply (T -> Boolean)
      |    fix pairs {Text, T -> T}
      |    dyn each () [
      |      parameters
      |        par step (T -> ())
      |    ]
      |  factory (Holder(T) -> Holder(T))
      |    parameters
      |      par seed T
      |domain draco Draco
      |""".stripMargin

  test("the live emitter spells every slot through the definition") {
    val read = Drake.parse(probe)
    assert(Drake.emit(read) == probe)
    assert(read.dracoAspect.elements.map(e => of(e.valueType.json)) == Seq("(T -> Boolean)", "{Text, T -> T}", "()"))
    assert(Drake.typeSurface(node("->", leaf("A"), leaf("B"))) == "(A -> B)" && Drake.typeSurface(leaf("A")) == "A")
  }

  test("the section transforms spell their own types: no type spelling is passed to them") {
    val read = Drake.parse(probe)
    val section = DracoAspectOf(read).value
    assert(section.linesIterator.next() == "type Holder(T) from [T]")
    assert(section.contains("  factory (Holder(T) -> Holder(T))"))
    val referring = TypeDefinition(TypeName("Member", Seq("example")),
      _domainAspect = DomainAspect(_typeName = TypeName("Home", Seq("example"), Seq(Dracon(node("(,)", leaf("S"), leaf("T")))))))
    assert(DomainAspectOf(referring).value == "domain example Home((S, T))")
  }

  /** Every value type a definition carries, at any depth. */
  private def valueTypes(json: Json): Seq[Json] = json.fold[Seq[Json]](
    Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
    members => members.flatMap(valueTypes),
    fields => fields.toList.flatMap { case (k, v) => if (k == "valueType") Seq(v) else valueTypes(v) }
  )

  test("every value-type slot in the main and mods corpus is reproduced") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 201, s"the corpus walk found only ${definitions.size} definitions")

    var slots  = 0
    var arrows = 0
    val differing: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      val surface = Files.readString(Paths.get(path.stripSuffix(".json") + ".drake"))
      valueTypes(json).flatMap { form =>
        slots += 1
        val actual = of(form)
        if (form.hcursor.downField("->").succeeded) {
          arrows += 1
          assert(actual.startsWith("(") && actual.endsWith(")"), s"$path: '$actual' is not parenthesized")
        }
        if (surface.contains(actual)) None else Some(s"$path: '$actual' is not in the authored surface")
      }
    }
    differing.foreach(line => log.info(line))
    assert(slots >= 1000 && arrows >= 50, s"the walk found only $slots slots and $arrows arrows")
    console.info(s"Slot over the corpus: $slots value-type slots reproduced in ${definitions.size} definitions ($arrows arrows parenthesized), ${differing.size} differing")
    assert(differing.isEmpty, "the definition spells a slot differently from its authored surface:\n  " + differing.mkString("\n  "))
  }
}
