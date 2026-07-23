package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.net.URI
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Generates Scala source from each TypeDefinition in `src/main/resources/draco/`
 *  and verifies generator/hand-written equivalence against `src/main/scala/draco/`.
 *
 *  Iteration source = filesystem walk over the resource tree. New TypeDefinitions
 *  are auto-included; types that should be skipped go into the `excluded` map below
 *  with a documentation comment as the value. The TypeElement sealed-trait family
 *  is excluded from per-type tests and validated as a multi-type group at the bottom.
 *
 *  JSON is the normative source form for type definitions; the walker reads only
 *  `.json` files.
 *
 *  NOTE: many hand-written files in `src/main/scala/draco/` predate the current
 *  generator and intentionally diverge. Failures here are expected and informative —
 *  each surfaces a place where hand-written Scala has drifted from JSON +
 *  Generator emission.
 */
class DracoGenTest extends AnyFunSuite with PersistentTestLog {

  /** A draco type. resourcePath is logical (within `Main.roots.sourceRoot`),
   *  scalaPath is logical (within `Main.roots.sinkRoot`). */
  private case class Ty(resourcePath: String, scalaPath: String)

  /** Map a resource path to its corresponding hand-written .scala path. Rule-ness is
   *  aspect presence, not a name suffix: every type — plain, rule, or actor — emits an
   *  object named for the bare concept (`draco/X.json` -> `draco/X.scala`). */
  private def deriveScalaPath(resourcePath: String): String =
    resourcePath.stripSuffix(".json") + ".scala"

  /** Walk `src/main/resources/draco/` and collect every regular `.json` file as
   *  a logical resource path (relative to the resource root). */
  private def discoverResourcePaths(): Seq[String] = {
    val resourceRoot = Paths.get(Main.roots.sourceRoot)
    val dracoDir     = resourceRoot.resolve("draco")
    Using.resource(Files.walk(dracoDir)) { stream =>
      stream.iterator.asScala
        .filter(p => Files.isRegularFile(p))
        .map(p => resourceRoot.relativize(p).toString.replace('\\', '/'))
        .filter(_.endsWith(".json"))
        .toList
        .sorted
    }
  }

  /** Excluded from per-type comparison, with reason documented as the value.
   *  All 13 TypeElement family members live in a single hand-written
   *  `TypeElement.scala`; they're validated as a multi-type group at the bottom. */
  private val excluded: Map[String, String] = Map(
    "draco/TypeElement.json"  -> "Sealed-trait family root; validated by group test below",
    "draco/BodyElement.json"  -> "TypeElement family member; validated by group test below",
    "draco/Fixed.json"        -> "TypeElement family member; validated by group test below",
    "draco/Mutable.json"      -> "TypeElement family member; validated by group test below",
    "draco/Dynamic.json"      -> "TypeElement family member; validated by group test below",
    "draco/Parameter.json"    -> "TypeElement family member; validated by group test below",
    "draco/Monadic.json"      -> "TypeElement family member; validated by group test below",
    "draco/Pattern.json"      -> "TypeElement family member; validated by group test below",
    "draco/Action.json"       -> "TypeElement family member; validated by group test below",
    "draco/Condition.json"    -> "TypeElement family member; validated by group test below",
    "draco/Variable.json"     -> "TypeElement family member; validated by group test below",
    "draco/Factory.json"      -> "TypeElement family member; validated by group test below",
    "draco/Local.json"        -> "TypeElement family member; validated by group test below"
  )

  /** Excluded from per-type *comparison* only — JSON parses test still runs.
   *  Two reasons in this set:
   *    (a) Hand-written .scala carries Scala-only logic (helper methods,
   *        computations, framework-specific imports) that doesn't translate
   *        cross-language and so doesn't belong in the JSON shape per
   *        feedback_haskell_test / feedback_json_authoring_surface.
   *    (b) Hand-written .scala carries vestigial metadata (`name`/`description`
   *        constants) that only the Scala source uses for self-description;
   *        propagating it through inheritance overrides is generator-mechanics
   *        burden disproportionate to the value. */
  private val comparisonOnlyExcluded: Map[String, String] = Map.empty

  /** TypeElement sealed-trait family — generated as one Scala source file
   *  (TypeElement.scala) by `Generator.generate(Seq[TypeDefinition])`.
   *  Order matters: parent first, then BodyElement, then leaves. */
  private val typeElementFamily: Seq[String] = Seq(
    "draco/TypeElement.json",
    "draco/BodyElement.json",
    "draco/Fixed.json",
    "draco/Mutable.json",
    "draco/Dynamic.json",
    "draco/Parameter.json",
    "draco/Monadic.json",
    "draco/Pattern.json",
    "draco/Action.json",
    "draco/Condition.json",
    "draco/Variable.json",
    "draco/Factory.json",
    "draco/Local.json"
  )

  private val perTypeTypes: Seq[Ty] =
    discoverResourcePaths()
      .filterNot(excluded.contains)
      .map(rp => Ty(rp, deriveScalaPath(rp)))

  // --- Loading ---

  private def loadTypeDefinition(rp: String): TypeDefinition = {
    val sourceContent = SourceContent(Main.roots.sourceRoot, rp)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
  }

  /** Hand-written Scala lives under src/main/scala/ — Main.roots.sinkRoot. */
  private def readHandWritten(scalaPath: String): String = {
    val uri: URI = Main.roots.sinkRoot.resolve(URI.create(scalaPath))
    val src = Source.fromURI(uri)
    try src.mkString finally src.close()
  }

  /** Normalize whitespace without erasing structural differences:
   *    - strip trailing whitespace on each line
   *    - collapse runs of blank lines into one
   *    - strip leading/trailing blank lines
   *  Indentation, token order, and substantive content are preserved. */
  private def normalize(source: String): String = {
    val lines = source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    val collapsed = lines.foldLeft(Seq.empty[String]) { (acc, line) =>
      if (line.isEmpty && acc.lastOption.contains("")) acc else acc :+ line
    }
    collapsed.dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  private def diffReport(handNorm: String, genNorm: String): String = {
    val handLines = handNorm.split('\n').toSeq
    val genLines  = genNorm.split('\n').toSeq
    val maxLen    = math.max(handLines.size, genLines.size)
    val rows = (0 until maxLen).map { i =>
      val h = handLines.lift(i).getOrElse("")
      val g = genLines.lift(i).getOrElse("")
      val marker = if (h == g) "  " else "!!"
      f"$marker ${i + 1}%3d | $h%-60s | $g"
    }
    "\n        hand-written" + " " * 48 + "| generated\n" + rows.mkString("\n")
  }

  // --- Per-type: parses ---

  perTypeTypes.foreach { ty =>
    test(s"${ty.resourcePath}: parses") {
      val td = loadTypeDefinition(ty.resourcePath)
      assert(td != TypeDefinition.Null, s"failed to parse ${ty.resourcePath}")
    }
  }

  // --- Per-type: Generator output matches hand-written ---

  perTypeTypes.filterNot(ty => comparisonOnlyExcluded.contains(ty.resourcePath)).foreach { ty =>
    test(s"${ty.resourcePath}: Generator output matches ${ty.scalaPath} (whitespace-normalized)") {
      val td       = loadTypeDefinition(ty.resourcePath)
      val genNorm  = normalize(Generator.generate(td))
      val handNorm = normalize(readHandWritten(ty.scalaPath))

      if (genNorm != handNorm) {
        log.info(s"\n--- ${ty.resourcePath}: generated (normalized) ---\n$genNorm")
        log.info(s"\n--- ${ty.scalaPath}: hand-written (normalized) ---\n$handNorm")
        fail(
          s"${ty.resourcePath}: generated source differs from hand-written ${ty.scalaPath}.\n" +
          s"Resolve by either (a) updating the JSON / Generator so emission matches the hand-written file, " +
          s"or (b) replacing the hand-written file with the generated output:\n" +
          s"    bin/draco-gen generate src/main/resources/${ty.resourcePath} > src/main/scala/${ty.scalaPath}\n" +
          diffReport(handNorm, genNorm)
        )
      }
    }
  }

  // --- TypeElement family: multi-type generation matches TypeElement.scala ---

  test("TypeElement family: multi-type Generator output matches draco/TypeElement.scala (whitespace-normalized)") {
    val tds      = typeElementFamily.map(loadTypeDefinition)
    val genNorm  = normalize(Generator.generate(tds))
    val handNorm = normalize(readHandWritten("draco/TypeElement.scala"))

    if (genNorm != handNorm) {
      log.info(s"\n--- TypeElement family: generated (normalized) ---\n$genNorm")
      log.info(s"\n--- draco/TypeElement.scala: hand-written (normalized) ---\n$handNorm")
      fail(
        s"TypeElement family: generated source differs from hand-written draco/TypeElement.scala.\n" +
        diffReport(handNorm, genNorm)
      )
    }
  }
}
