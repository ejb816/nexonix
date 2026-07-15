package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Emits .drake surface from each plain-type TypeDefinition JSON
 *  (`Generator.drake`) and verifies it against the hand-authored corpus in
 *  `src/main/resources/draco/` — the drake projection's drift guard, mirroring
 *  DracoGenTest for the Scala projection.
 *
 *  Since the Phase-2a sweep the plain-type corpus is emitter-canonical: every
 *  plain type gets a per-type exact-match test (whitespace-normalized). A
 *  failure means either the emitter regressed or a .drake was hand-edited
 *  without its JSON — resolve by fixing the emitter or re-emitting the file.
 *
 *  Rule / actor / codec aspects are the next emitter increments; `.rule.json` /
 *  `.actor.json` files are filtered at discovery. A plain file that grows a
 *  codec aspect will fail its test loudly (Generator.drake rejects it) — the
 *  signal to build that increment. */
class DrakeGenTest extends AnyFunSuite with PersistentTestLog {

  /** Excluded from comparison — hand-authored surface deliberately AHEAD of the
   *  JSON (target-state syntax discovery the emitter cannot yet reconstruct). */
  private val authoredAhead: Map[String, String] = Map(
    "draco/Action.json"      -> "present-empty rule/actor heads (presence model not yet in JSON)",
    "draco/BodyElement.json" -> "present-empty rule/actor heads (presence model not yet in JSON)",
    "draco/ActorAspect.json" -> "present-empty heads + start/message/signal rename (Phase 2b)"
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
        .filterNot(p => p.endsWith(".rule.json") || p.endsWith(".actor.json"))
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

  /** Same normalization as DracoGenTest: strip trailing whitespace, collapse
   *  blank-line runs, trim leading/trailing blank lines. */
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

  // --- Per-type: Generator.drake output matches the authored .drake ---

  discoverResourcePaths().filterNot(authoredAhead.contains).foreach { rp =>
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
}
