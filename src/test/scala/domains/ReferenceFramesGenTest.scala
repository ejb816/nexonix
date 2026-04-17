package domains

import draco._
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.net.URI
import scala.io.Source

/** Generates Scala source from the reference-frame JSON type definitions and
 *  verifies generator/hand-written equivalence plus compilation.
 *
 *  Families (19 types total):
 *    - Cosmocentric (super-domain, no leaves)
 *    - Egocentric     + Bearing, Reach  → Percept
 *    - Geocentric     + Position, Altitude, Heading → Fix
 *    - Heliocentric   + Elements, Epoch → Ephemeris
 *    - Galactocentric + Parallax, ProperMotion, RadialVelocity → Trajectory
 *
 *  Per type:
 *    - JSON parses to a TypeDefinition
 *    - Generator output matches the committed hand-written .scala file
 *      (whitespace-normalized; this catches drift between JSON and emitted Scala)
 *
 *  Per family (super + sub-frame + leaves + assembly):
 *    - All family members compile together as one unit (compileMulti).
 *      Single-source `compile` is inadequate once members have
 *      inter-file dependencies (extends a sibling, Holon[(Bearing, Reach)], etc.);
 *      this is the only correct compilation verification.
 *
 *  Universe-wide:
 *    - All 19 types compile together as one unit.
 */
class ReferenceFramesGenTest extends AnyFunSuite {

  /** A reference-frame type (frame itself, leaf, or assembly). */
  private case class Ty(name: String, pkg: String) {
    val jsonPath:  String = s"domains/$pkg/$name.json"
    val scalaPath: String = s"domains/$pkg/$name.scala"
  }

  /** A reference-frame family: super + sub-frame + its leaves + its assembly.
   *  The `members` field includes the super-domain (Cosmocentric) so each
   *  family compiles self-contained under compileMulti. */
  private case class Family(name: String, members: Seq[Ty])

  private val cosmocentric = Ty("Cosmocentric", "cosmocentric")

  private val egocentric     = Ty("Egocentric",     "egocentric")
  private val bearing        = Ty("Bearing",        "egocentric")
  private val reach          = Ty("Reach",          "egocentric")
  private val percept        = Ty("Percept",        "egocentric")

  private val geocentric     = Ty("Geocentric",     "geocentric")
  private val position       = Ty("Position",       "geocentric")
  private val altitude       = Ty("Altitude",       "geocentric")
  private val heading        = Ty("Heading",        "geocentric")
  private val fix            = Ty("Fix",            "geocentric")

  private val heliocentric   = Ty("Heliocentric",   "heliocentric")
  private val elements       = Ty("Elements",       "heliocentric")
  private val epoch          = Ty("Epoch",          "heliocentric")
  private val ephemeris      = Ty("Ephemeris",      "heliocentric")

  private val galactocentric = Ty("Galactocentric", "galactocentric")
  private val parallax       = Ty("Parallax",       "galactocentric")
  private val properMotion   = Ty("ProperMotion",   "galactocentric")
  private val radialVelocity = Ty("RadialVelocity", "galactocentric")
  private val trajectory     = Ty("Trajectory",     "galactocentric")

  private val families: Seq[Family] = Seq(
    Family("Cosmocentric",   Seq(cosmocentric)),
    Family("Egocentric",     Seq(cosmocentric, egocentric, bearing, reach, percept)),
    Family("Geocentric",     Seq(cosmocentric, geocentric, position, altitude, heading, fix)),
    Family("Heliocentric",   Seq(cosmocentric, heliocentric, elements, epoch, ephemeris)),
    Family("Galactocentric", Seq(cosmocentric, galactocentric, parallax, properMotion, radialVelocity, trajectory))
  )

  /** All 19 types, deduplicated (Cosmocentric appears in every family). */
  private val allTypes: Seq[Ty] = families.flatMap(_.members).distinct

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

  // --- Per-type: JSON parses ---

  allTypes.foreach { ty =>
    test(s"${ty.name}: JSON parses") {
      val td = loadTypeDefinition(ty.jsonPath)
      assert(td != TypeDefinition.Null, s"failed to parse ${ty.jsonPath}")
      assert(td.typeName.name == ty.name)
    }
  }

  // --- Per-type: Generator output matches hand-written ---

  allTypes.foreach { ty =>
    test(s"${ty.name}: Generator output matches hand-written (whitespace-normalized)") {
      val td       = loadTypeDefinition(ty.jsonPath)
      val genNorm  = normalize(Generator.generate(td))
      val handNorm = normalize(readHandWritten(ty.scalaPath))

      if (genNorm != handNorm) {
        println(s"\n--- ${ty.name}: generated (normalized) ---\n$genNorm")
        println(s"\n--- ${ty.name}: hand-written (normalized) ---\n$handNorm")
        fail(
          s"${ty.name}: generated source differs from hand-written.\n" +
          s"Resolve by either (a) updating the JSON / Generator so emission matches the hand-written file, " +
          s"or (b) replacing the hand-written file with the generated output:\n" +
          s"    bin/draco-gen generate src/test/resources/${ty.jsonPath} > src/test/scala/${ty.scalaPath}\n" +
          diffReport(handNorm, genNorm)
        )
      }
    }
  }

  // --- Per-family: compileMulti over super + sub-frame + leaves + assembly ---

  families.foreach { family =>
    test(s"${family.name} family: all ${family.members.size} members compile together (compileMulti)") {
      val pairs: Seq[(String, String)] = family.members.map { ty =>
        val td     = loadTypeDefinition(ty.jsonPath)
        val source = Generator.generate(td)
        assert(source != null && source.nonEmpty, s"Generator produced empty source for ${ty.name}")
        (source, s"${ty.name}.scala")
      }

      RuntimeCompiler.compileMulti(pairs) match {
        case Right(_) => // pass
        case Left(errors) =>
          val names = family.members.map(_.name).mkString(", ")
          fail(s"${family.name} compileMulti failed for [$names]:\n  ${errors.mkString("\n  ")}")
      }
    }
  }

  // --- Universe-wide: all 19 types compile together ---

  test(s"Reference frames: all ${allTypes.size} types compile together as one unit (compileMulti)") {
    val pairs: Seq[(String, String)] = allTypes.map { ty =>
      val td     = loadTypeDefinition(ty.jsonPath)
      val source = Generator.generate(td)
      assert(source != null && source.nonEmpty, s"Generator produced empty source for ${ty.name}")
      (source, s"${ty.name}.scala")
    }

    RuntimeCompiler.compileMulti(pairs) match {
      case Right(_) => // pass
      case Left(errors) =>
        val names = allTypes.map(_.name).mkString(", ")
        fail(s"Universe compileMulti failed for [$names]:\n  ${errors.mkString("\n  ")}")
    }
  }
}
