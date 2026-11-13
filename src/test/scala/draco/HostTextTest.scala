package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The host text left in definition VALUES is bounded by a named seam list (Dev, 2026-11-10).
 *
 *  A value is a drake expression tree whose string leaves are names and literals; a leaf that
 *  still carries the Scala target's own syntax — a `=>` or placeholder lambda, `try`, `new`,
 *  a host `if`, a `match`, a brace block — is host-opaque text the engine passes through
 *  verbatim, dead for any other target (README, "Language-specific residues"; GitHub #59
 *  names this tail as the metric that must not move the wrong way). Measured on 2026-11-10
 *  over 1984 value leaves: 24 such leaves, three of them in ordinary definitions
 *  (ActorAspect's lambda, CLI's and SourceContent's try/finally), re-authored that day as a
 *  drake lambda and two `bracket` calls. What remains sits where host realization IS the
 *  content: the Dracon wrapper, the Circe binding, the definition carrier, the knowledge
 *  service, the circe decode at the loader, the runtime lookup of the example rules, and
 *  the Editor's guarded transform run.
 *
 *  This suite asserts that list. A host-syntax leaf in any other definition fails, naming
 *  the file and the leaf; so does a seam whose count moved — up, because the residue grew,
 *  or down, because a leaf was re-authored and the number below is now stale. The list is
 *  meant to shrink; the commit that shrinks it updates it.
 */
class HostTextTest extends AnyFunSuite with PersistentTestLog {

  private val roots = Seq("src/main/resources", "src/mods/resources")

  /** definition -> host-syntax value leaves it is known to carry. */
  private val seams: Map[String, Int] = Map(
    "src/main/resources/draco/CLI.json"                             -> 1, // the circe decode: _.as[TypeDefinition]
    "src/main/resources/draco/TypeLoader.json"                      -> 1, // the circe decode
    "src/main/resources/draco/Rule.json"                            -> 1, // new KnowledgeService ()
    "src/main/resources/draco/format/dracon/Dracon.json"            -> 5, // the wrapper over the host Json
    "src/main/resources/draco/generator/carrier/DefinitionPath.json" -> 3, // the carrier: URL, File, class path
    "src/main/resources/draco/scalatarget/CirceCodecs.json"         -> 2, // the Circe binding
    "src/mods/resources/domains/aerial/OriginateReport.json"        -> 1, // the runtime lookup: get[Dracon => Unit]
    "src/mods/resources/domains/ethereal/OriginateReport.json"      -> 1,
    "src/mods/resources/domains/marine/OriginateReport.json"        -> 1,
    "src/mods/resources/domains/terrestrial/OriginateReport.json"   -> 1,
    "src/mods/resources/draco/dreams/editor/TransformBinding.json"  -> 4  // the guarded transform run
  )

  private val markers: Seq[(String, scala.util.matching.Regex)] = Seq(
    "lambda"      -> "=>".r,
    "placeholder" -> "(?<![A-Za-z0-9_])_\\.".r,
    "try"         -> "\\btry\\b".r,
    "new"         -> "\\bnew ".r,
    "if"          -> "\\bif \\(".r,
    "match"       -> "\\bmatch\\b".r,
    "block"       -> "\\{".r
  )

  /** Quoted text is a literal, not syntax: an interpolated message may say anything. */
  private def unquoted(leaf: String): String = leaf.replaceAll("\"(?:[^\"\\\\]|\\\\.)*\"", "\"\"")

  private def hostSyntax(leaf: String): Seq[String] = {
    val text = unquoted(leaf)
    markers.collect { case (name, rx) if rx.findFirstIn(text).nonEmpty => name }
  }

  /** Every string leaf under a `value`, the type slots excepted. */
  private def valueLeaves(json: Json, underValue: Boolean, key: String): Seq[String] = json.fold[Seq[String]](
    Seq.empty,
    _ => Seq.empty,
    _ => Seq.empty,
    text => if (underValue && key != "valueType") Seq(text) else Seq.empty,
    members => members.flatMap(valueLeaves(_, underValue, key)),
    fields => fields.toList.flatMap { case (k, v) => valueLeaves(v, underValue || k == "value", k) }
  )

  test("host text in definition values sits only at the named seams, at the counts recorded") {
    // The detector reads syntax, not words.
    assert(hostSyntax("aa => aa.message.body.isEmpty") == Seq("lambda"))
    assert(hostSyntax("_.as[TypeDefinition]") == Seq("placeholder"))
    assert(hostSyntax("try source.mkString finally source.close()") == Seq("try"))
    assert(hostSyntax("s\"binding does not match ${subject.namePath} => refused\"").isEmpty, "a quoted message is a literal")
    assert(hostSyntax("td.typeName.namePackage").isEmpty && hostSyntax("_namePackage").isEmpty, "a name is not host text")

    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 184, s"the corpus walk found only ${definitions.size} definitions")

    var leaves = 0
    val found: Seq[(String, Seq[String])] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      val all  = valueLeaves(json, underValue = false, key = "")
      leaves += all.size
      val host = all.filter(hostSyntax(_).nonEmpty)
      host.foreach(leaf => log.info(s"$path [${hostSyntax(leaf).mkString(",")}] $leaf"))
      if (host.isEmpty) None else Some(path -> host)
    }
    val outside = found.filterNot { case (path, _) => seams.contains(path) }
    val moved   = seams.toSeq.sortBy(_._1).flatMap { case (path, expected) =>
      val actual = found.collectFirst { case (`path`, host) => host.size }.getOrElse(0)
      if (actual == expected) None else Some(s"$path: $actual host-syntax leaves, recorded $expected")
    }
    console.info(s"host text in values: ${found.map(_._2.size).sum} leaves of $leaves in ${found.size} definitions, " +
      s"${outside.map(_._2.size).sum} outside the ${seams.size} named seams")

    assert(outside.isEmpty, "host text outside the named seams — author it in drake, or name the seam:\n" +
      outside.flatMap { case (path, host) => host.map(leaf => s"  $path: $leaf") }.mkString("\n"))
    assert(moved.isEmpty, "a seam's host text moved — update the recorded count in the same commit:\n  " + moved.mkString("\n  "))
  }

  /** The four host spellings of an empty collection the emitter collapsed onto `[]` and `{}`
   *  until 2026-11-12 (GitHub #11), as a rendered value. */
  private val hostEmpty = Set("Seq.empty", "Seq()", "Set.empty", "Set()")

  /** Every element in a definition — an object carrying a `kind` — as (name, value). */
  private def elements(json: Json): Seq[(String, Json)] = json.fold[Seq[(String, Json)]](
    Seq.empty, _ => Seq.empty, _ => Seq.empty, _ => Seq.empty,
    members => members.flatMap(elements),
    fields => {
      val own = if (fields.contains("kind"))
        Seq((fields("name").flatMap(_.asString).getOrElse(""), fields("value").getOrElse(Json.Null))) else Seq.empty
      own ++ fields.values.flatMap(elements)
    }
  )

  test("no value spells an empty collection the host's way and no element name carries a bracket") {
    // The emitter and the parser carry both as authored: the two string-era respellings are gone.
    val probe = "type Probe\n  elements\n    fix xs [Int] []\n    fix ys {Int} {}\ndomain draco Draco\n"
    val read  = Drake.parse(probe)
    assert(Drake.emit(read) == probe)
    assert(read.dracoAspect.elements.map(_.value.json) == Seq(Json.obj("[]" -> Json.arr()), Json.obj("{}" -> Json.arr())))
    assert(Drake.expression(Json.fromString("Seq()")) == "Seq()", "a host spelling is written as it stands, not collapsed")

    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 198, s"the corpus walk found only ${definitions.size} definitions")

    var count = 0
    val found: Seq[String] = definitions.flatMap { path =>
      val json = parser.parse(Files.readString(Paths.get(path))).fold(error => fail(s"$path: $error"), identity)
      elements(json).flatMap { case (name, value) =>
        count += 1
        val spelled  = if (value.isNull) "" else Drake.expression(value)
        val emptyHit = if (hostEmpty(spelled)) Seq(s"$path: '$name' has the value $spelled — author [] or {}") else Seq.empty
        val nameHit  = if (name.exists("[]()".contains(_))) Seq(s"$path: element name '$name' carries a bracket") else Seq.empty
        emptyHit ++ nameHit
      }
    }
    found.foreach(line => log.info(line))
    assert(count >= 1000, s"the walk found only $count elements")
    assert(found.isEmpty, "a host spelling the emitter no longer respells:\n  " + found.mkString("\n  "))
  }
}
