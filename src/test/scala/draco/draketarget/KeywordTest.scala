package draco.draketarget

import draco._
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** Drake's element keywords are a definition (Dev, 2026-11-12; GitHub #11): `draketarget.Keyword`
 *  carries the table as globals — `kinds` (a kind's name and the keyword of its member line),
 *  `table` (their map) and `strict` (the strictness word). An element carries its kind's own
 *  definition, so the emitter reads the kind as that definition's name and looks its keyword up;
 *  before this the keyword was a match on the host class, which a definition in main could not
 *  make. The parser's member keywords are the table's values.
 */
class KeywordTest extends AnyFunSuite {

  private val specified: Seq[(String, String)] = Seq(
    "Fixed" -> "fix", "Mutable" -> "mut", "Dynamic" -> "dyn", "Parameter" -> "par", "Local" -> "loc",
    "Monadic" -> "mon", "Variable" -> "var", "Condition" -> "con", "Case" -> "case")

  /** The three kinds that are sections of a definition, never member lines. */
  private val sections = Set("Action", "Factory", "Pattern")

  private val probe: String =
    """type Probe
      |  elements
      |    fix a Int 1
      |    mut b Int 2
      |    now fix c Int 3
      |    dyn d Int [
      |      parameters
      |        par x Int
      |      body
      |        loc y Int x
      |        mon println(y)
      |        fix value Int y
      |    ]
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
      |""".stripMargin

  private def kinds(json: Json): Seq[String] = json.fold(
    Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
    members => members.flatMap(kinds),
    fields => fields.toList.flatMap { case (k, v) => if (k == "kind") v.asString.toSeq else kinds(v) }
  )

  test("the table is the nine member kinds, each kind and each keyword once, and the strictness word") {
    assert(Keyword.kinds == specified)
    assert(Keyword.kinds.map(_._1).distinct.size == 9 && Keyword.kinds.map(_._2).distinct.size == 9)
    assert(Keyword.table == specified.toMap)
    assert(Keyword.strict == "now" && !Keyword.table.values.exists(_ == Keyword.strict))
    assert(sections.forall(kind => !Keyword.table.contains(kind)))
  }

  test("an element's kind is the name of the definition it carries, read or decoded") {
    val read = Drake.parse(probe)
    val elements = read.dracoAspect.elements
    assert(elements.map(_.typeDefinition.typeName.name) == Seq("Fixed", "Mutable", "Fixed", "Dynamic"))
    val decoded = TypeDefinition.decoder.decodeJson(TypeDefinition.encoder(read)).fold(error => fail(error.toString), identity)
    assert(decoded.dracoAspect.elements.map(_.typeDefinition.typeName.name) == Seq("Fixed", "Mutable", "Fixed", "Dynamic"))
    assert(kinds(TypeDefinition.encoder(read)).toSet -- sections == Keyword.table.keySet)
  }

  test("the emitter writes each member line with its kind's keyword and the parser reads it back") {
    val read = Drake.parse(probe)
    assert(Drake.emit(read) == probe)
    val decoded = TypeDefinition.decoder.decodeJson(TypeDefinition.encoder(read)).fold(error => fail(error.toString), identity)
    assert(Drake.emit(decoded) == probe)
    Keyword.table.values.foreach { keyword =>
      assert(probe.linesIterator.exists(line => line.trim.stripPrefix(Keyword.strict + " ").startsWith(keyword + " ")), keyword)
    }
    assert(read.dracoAspect.elements.map(_.now) == Seq(false, false, true, false))
  }

  test("every element kind in the main and mods corpus has a row or is a section") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 198, s"the corpus walk found only ${definitions.size} definitions")
    val found = definitions.flatMap { path =>
      kinds(parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity))
    }
    assert(found.size >= 1000, s"the walk found only ${found.size} elements")
    assert(found.toSet -- sections == Keyword.table.keySet, "a kind without a row, or a row no definition uses")
  }
}
