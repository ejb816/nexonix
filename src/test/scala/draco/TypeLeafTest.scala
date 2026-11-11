package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** A type LEAF in a definition is a name (Dev, 2026-11-10; GitHub #11).
 *
 *  A type in the carrier is a type-form tree whose string leaves are names: `[T]`, `{K, V}`,
 *  `F(A, B)`, `A -> B` and the tuple are nodes, never text. Until this suite the drake emitter
 *  kept a second, string-era renderer that respelled a leaf written in the Scala target's
 *  notation (`Seq[T]`, `A => B`); the last such leaf in the corpus was sentient Path's
 *  `Primal(Seq[Waypoint])`, authored `Primal([Waypoint])` the same day, and the emitter now
 *  has one type-form renderer, which writes a leaf verbatim. That is only right while every
 *  leaf is a name, so this suite asserts it: a string leaf in a type slot — a value type, a
 *  type parameter, a bound, a foreign derivation — that is not a dotted name fails by file
 *  and leaf. It is HostTextTest's counterpart for types, with no seam list: there is none.
 */
class TypeLeafTest extends AnyFunSuite with PersistentTestLog {

  private val roots = Seq("src/main/resources", "src/mods/resources")
  private val typeSlots = Set("valueType", "typeParameters", "upperBound", "lowerBound", "derivation")
  private val name = "[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*".r

  private def isName(leaf: String): Boolean = leaf.isEmpty || name.pattern.matcher(leaf).matches

  /** Every string leaf under a type slot. A `value` under a type slot (a formal has none,
   *  but a nested element would) is not a type and ends the slot. */
  private def typeLeaves(json: Json, inType: Boolean): Seq[String] = json.fold[Seq[String]](
    Seq.empty,
    _ => Seq.empty,
    _ => Seq.empty,
    text => if (inType) Seq(text) else Seq.empty,
    members => members.flatMap(typeLeaves(_, inType)),
    fields => fields.toList.flatMap { case (k, v) => typeLeaves(v, (inType && k != "value") || typeSlots.contains(k)) }
  )

  test("every type leaf in the main and mods corpus is a name; the emitter writes one verbatim") {
    assert(isName("Waypoint") && isName("draco.format.dracon.Dracon") && isName(""))
    assert(!isName("Seq[Waypoint]") && !isName("A => B") && !isName("(Int,Int)") && !isName("mutable.Set[T]") && !isName("{K, V}"))

    // The one renderer: a tree in drake's own spelling, a leaf as authored.
    val sequence = Json.obj("[]" -> Json.arr(Json.fromString("Waypoint")))
    assert(Drake.typeSurface(sequence) == "[Waypoint]")
    assert(Drake.typeSurface(Json.fromString("Waypoint")) == "Waypoint")
    assert(Drake.typeSurface(Json.obj("->" -> Json.arr(Json.fromString("A"), Json.fromString("B")))) == "(A -> B)")

    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 194, s"the corpus walk found only ${definitions.size} definitions")

    var leaves = 0
    val host: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      val all  = typeLeaves(json, inType = false)
      leaves += all.size
      all.filterNot(isName).map(leaf => s"$path: $leaf")
    }
    host.foreach(line => log.info(line))
    console.info(s"type leaves: $leaves in ${definitions.size} definitions, ${host.size} carrying host type syntax")
    assert(host.isEmpty, "a type leaf is not a name — author the type as a drake type form:\n  " + host.mkString("\n  "))
  }

  test("sentient Path derives Primal of a sequence, as a tree, and its surface says so in drake") {
    val json = parser.parse(Files.readString(Paths.get("src/mods/resources/domains/sentient/Path.json"))).fold(e => fail(e.toString), identity)
    val td   = json.as[TypeDefinition].fold(e => fail(e.toString), identity)
    val surface = Files.readString(Paths.get("src/mods/resources/domains/sentient/Path.drake"))
    assert(surface.linesIterator.next() == "type Path from Sentient draco Primal([Waypoint])")
    assert(Drake.emit(td) == surface)
    assert(typeLeaves(json, inType = false).contains("Waypoint") && !json.noSpaces.contains("Seq["))
  }
}
