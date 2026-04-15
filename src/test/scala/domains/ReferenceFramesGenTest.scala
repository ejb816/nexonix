package domains

import draco._
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.net.URI
import scala.io.Source

/** Generates Scala source from the 5 reference-frame JSON type definitions
 *  (Cosmocentric + Egocentric/Geocentric/Heliocentric/Galactocentric) and verifies:
 *
 *    Per-frame:
 *      - JSON parses to a TypeDefinition
 *      - Generator output matches the committed hand-written .scala file
 *        (whitespace-normalized; this catches drift between JSON and emitted Scala)
 *
 *    Family-wide:
 *      - Cosmocentric compiles standalone (it has no cross-frame deps)
 *      - All 5 frames compile together as one unit (compileMulti) — this is
 *        the right verification path for the sub-frames, since each
 *        sub-frame's `extends Cosmocentric` requires Cosmocentric's class
 *        on the compiler's classpath, which RuntimeCompiler.compile (single-source)
 *        does not provide.
 *
 *  Total: 5 (parse) + 5 (match) + 1 (Cosmocentric solo) + 1 (multi) = 12 tests.
 */
class ReferenceFramesGenTest extends AnyFunSuite {

  private case class Frame(name: String, pkg: String) {
    val jsonPath: String   = s"domains/$pkg/$name.json"
    val scalaPath: String  = s"domains/$pkg/$name.scala"
  }

  private val frames: Seq[Frame] = Seq(
    Frame("Cosmocentric",   "cosmocentric"),
    Frame("Egocentric",     "egocentric"),
    Frame("Geocentric",     "geocentric"),
    Frame("Heliocentric",   "heliocentric"),
    Frame("Galactocentric", "galactocentric")
  )

  private def loadTypeDefinition(logicalPath: String): TypeDefinition = {
    val sourceContent = SourceContent(Test.roots.sourceRoot, logicalPath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
  }

  /** Hand-written Scala lives under src/test/scala/ — Test.roots.sinkRoot
   *  (configured in draco.Test) resolves there. */
  private def readHandWritten(logicalPath: String): String = {
    val uri: URI = Test.roots.sinkRoot.resolve(URI.create(logicalPath))
    val src = Source.fromURI(uri)
    try src.mkString finally src.close()
  }

  /** Normalize whitespace without erasing structural differences:
   *    - strip trailing whitespace on each line
   *    - collapse runs of blank lines into one
   *    - strip leading and trailing blank lines
   *  Keeps indentation, token order, and substantive content intact. */
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
      f"$marker ${i + 1}%3d | ${h}%-60s | $g"
    }
    "\n        hand-written" + " " * 48 + "| generated\n" + rows.mkString("\n")
  }

  // --- Per-frame: JSON parses ---

  frames.foreach { frame =>
    test(s"${frame.name}: JSON parses") {
      val td = loadTypeDefinition(frame.jsonPath)
      assert(td != TypeDefinition.Null, s"failed to parse ${frame.jsonPath}")
      assert(td.typeName.name == frame.name)
    }
  }

  // --- Per-frame: Generator output matches hand-written ---

  frames.foreach { frame =>
    test(s"${frame.name}: Generator output matches hand-written (whitespace-normalized)") {
      val td       = loadTypeDefinition(frame.jsonPath)
      val genNorm  = normalize(Generator.generate(td))
      val handNorm = normalize(readHandWritten(frame.scalaPath))

      if (genNorm != handNorm) {
        println(s"\n--- ${frame.name}: generated (normalized) ---\n$genNorm")
        println(s"\n--- ${frame.name}: hand-written (normalized) ---\n$handNorm")
        fail(
          s"${frame.name}: generated source differs from hand-written.\n" +
          s"Resolve by either (a) updating the JSON / Generator so emission matches the hand-written file, " +
          s"or (b) replacing the hand-written file with the generated output:\n" +
          s"    bin/draco-gen generate src/test/resources/${frame.jsonPath} > src/test/scala/${frame.scalaPath}\n" +
          diffReport(handNorm, genNorm)
        )
      }
    }
  }

  // --- Family-wide: Cosmocentric compiles standalone ---

  test("Cosmocentric: Generator output compiles standalone") {
    val td     = loadTypeDefinition("domains/cosmocentric/Cosmocentric.json")
    val source = Generator.generate(td)
    assert(source != null && source.nonEmpty, "Generator produced empty source")

    RuntimeCompiler.compile(source, "Cosmocentric.scala") match {
      case Right(_)     => // pass
      case Left(errors) => fail(s"Cosmocentric compile failed:\n  ${errors.mkString("\n  ")}")
    }
  }

  // --- Family-wide: all 5 frames compile together via compileMulti ---

  test("Reference frames: all 5 compile together as one unit (compileMulti)") {
    val pairs: Seq[(String, String)] = frames.map { frame =>
      val td     = loadTypeDefinition(frame.jsonPath)
      val source = Generator.generate(td)
      assert(source != null && source.nonEmpty, s"Generator produced empty source for ${frame.name}")
      (source, s"${frame.name}.scala")
    }

    RuntimeCompiler.compileMulti(pairs) match {
      case Right(_) => // pass
      case Left(errors) =>
        val names = frames.map(_.name).mkString(", ")
        fail(s"compileMulti failed for [$names]:\n  ${errors.mkString("\n  ")}")
    }
  }
}
