package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}

/** Emits .drake surface from TypeDefinition JSON (`Generator.drake`) and verifies
 *  it against the hand-authored corpus in `src/main/resources/draco/`.
 *
 *  Two tiers:
 *    1. EXACT MATCH — the new-model files (leaf list-blocks unbracketed, bare
 *       `factory`) must reproduce byte-for-byte (whitespace-normalized). These
 *       are the emitter's ground truth; a diff here is an emitter bug.
 *    2. CENSUS (report-only) — every other plain-type JSON is emitted and
 *       diffed against its hand-authored .drake. The corpus predates the
 *       new-model bracket/keyword conventions, so diffs are expected: the
 *       census output IS the Phase-2a sweep worklist, not a failure. The test
 *       fails only if emission itself throws on a plain type.
 *
 *  Rule / actor / codec aspects are the next emitter increments; those files
 *  are skipped and counted. */
class DrakeGenTest extends AnyFunSuite with PersistentTestLog {

  /** New-model files: emission must match the authored surface exactly. */
  private val exactMatchTargets: Seq[String] = Seq(
    "draco/Dictionary.json",
    "draco/ContentSink.json",
    "draco/SourceContent.json"
  )

  private def deriveDrakePath(resourcePath: String): String =
    resourcePath.stripSuffix(".json") + ".drake"

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

  private def loadTypeDefinition(rp: String): TypeDefinition = {
    val sourceContent = SourceContent(Main.roots.sourceRoot, rp)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
  }

  private def readDrake(drakePath: String): Option[String] = {
    val path = Paths.get(Main.roots.sourceRoot).resolve(drakePath)
    if (Files.isRegularFile(path)) Some(new String(Files.readAllBytes(path))) else None
  }

  private def isPlainType(td: TypeDefinition): Boolean =
    RuleAspect.isEmpty(td.ruleAspect) &&
    ActorAspect.isEmpty(td.actorAspect) &&
    CodecAspect.isEmpty(td.codecAspect)

  /** Same normalization as DracoGenTest: strip trailing whitespace, collapse
   *  blank-line runs, trim leading/trailing blank lines. Indentation, token
   *  order, and substantive content are preserved. */
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
    "\n        hand-authored" + " " * 47 + "| emitted\n" + rows.mkString("\n")
  }

  // --- Tier 1: new-model files reproduce exactly ---

  exactMatchTargets.foreach { rp =>
    val drakePath = deriveDrakePath(rp)
    test(s"$rp: Generator.drake matches $drakePath (whitespace-normalized)") {
      val td = loadTypeDefinition(rp)
      assert(td != TypeDefinition.Null, s"failed to parse $rp")
      val authored = readDrake(drakePath).getOrElse(fail(s"missing $drakePath"))
      val genNorm  = normalize(Generator.drake(td))
      val handNorm = normalize(authored)
      if (genNorm != handNorm) {
        log.info(s"\n--- $rp: emitted (normalized) ---\n$genNorm")
        log.info(s"\n--- $drakePath: hand-authored (normalized) ---\n$handNorm")
        fail(s"$rp: emitted drake differs from hand-authored $drakePath.\n" + diffReport(handNorm, genNorm))
      }
    }
  }

  // --- Tier 2: census over the plain corpus (report-only; the Phase-2a worklist) ---

  test("drake census: every plain-type JSON emits without error; diffs vs authored corpus reported") {
    val all = discoverResourcePaths()
    var matches, diffs, missing, deferred = 0
    var errors = Seq.empty[String]

    all.foreach { rp =>
      val td = loadTypeDefinition(rp)
      if (td == TypeDefinition.Null || !isPlainType(td)) deferred += 1
      else Try(Generator.drake(td)) match {
        case Failure(e) =>
          errors :+= s"$rp: ${e.getMessage}"
        case Success(emitted) =>
          readDrake(deriveDrakePath(rp)) match {
            case None => missing += 1
              log.info(s"$rp: no hand-authored .drake")
            case Some(authored) =>
              val genNorm  = normalize(emitted)
              val handNorm = normalize(authored)
              if (genNorm == handNorm) matches += 1
              else {
                diffs += 1
                log.info(s"\n=== $rp: differs from authored ===" + diffReport(handNorm, genNorm))
              }
          }
      }
    }

    val summary = s"drake census: $matches match, $diffs differ (Phase-2a worklist), " +
      s"$missing without .drake, $deferred deferred (rule/actor/codec), ${errors.size} emission errors"
    console.info(s"  $summary")
    log.info(summary)
    errors.foreach(e => log.info(s"EMISSION ERROR: $e"))
    assert(errors.isEmpty, s"emission threw on plain types:\n  ${errors.mkString("\n  ")}")
  }
}
