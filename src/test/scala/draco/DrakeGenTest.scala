package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Emits .drake surface from each plain-type TypeDefinition JSON
 *  (`Drake.emit`) and verifies it against the hand-authored corpus in
 *  `src/main/resources/draco/` — the drake projection's drift guard, mirroring
 *  DracoGenTest for the Scala projection.
 *
 *  Since the Phase-2a sweep the plain-type corpus is emitter-canonical: every
 *  plain type gets a per-type exact-match test (whitespace-normalized). A
 *  failure means either the emitter regressed or a .drake was hand-edited
 *  without its JSON — resolve by fixing the emitter or re-emitting the file.
 *
 *  Plain types and rules are covered; every type maps to its base-name `.drake`
 *  (AddNaturalSequence.json -> AddNaturalSequence.drake). Rule-/actor-ness is
 *  carried by the aspect, not a name suffix. Codec payload lists are covered;
 *  unsupported discriminator syntax still fails loudly rather than being dropped. */
class DrakeGenTest extends AnyFunSuite with PersistentTestLog {

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
        .filterNot(p => p.endsWith(".actor.json"))
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

  // --- Per-type: Drake.emit output matches the authored .drake ---

  discoverResourcePaths().foreach { rp =>
    val drakePath = deriveDrakePath(rp)
    test(s"$rp: Drake.emit matches $drakePath (whitespace-normalized)") {
      val td = loadTypeDefinition(rp)
      assert(td != TypeDefinition.Null, s"failed to parse $rp")
      val authored = readDrake(drakePath).getOrElse(fail(s"missing $drakePath"))
      val genNorm  = normalize(Drake.emit(td))
      val handNorm = normalize(authored)
      if (genNorm != handNorm) {
        log.info(s"\n--- $rp: emitted (normalized) ---\n$genNorm")
        log.info(s"\n--- $drakePath: hand-authored (normalized) ---\n$handNorm")
        fail(s"$rp: emitted drake differs from hand-authored $drakePath.\n" + diffReport(handNorm, genNorm))
      }
    }
  }

  // --- Mods definitions: Drake.emit matches every authored surface under src/mods/resources ---
  //
  // The staged draco definitions (dreams, service, format/yaml) and the example domains live
  // under src/mods/resources, outside the draco walk above. From 2026-09-10 to 2026-11-02 only
  // the ACTOR definitions under domains were gated here, the plain and rule mods types named
  // "a later pass" that never came; since 2026-11-02 every mods definition with an authored
  // .drake gets the same exact-match test as the main corpus, and the report-only headline
  // counts every mods definition still lacking one — zero, Yaml having taken its surface the
  // same day. The log phrase "pending .drake authoring" is kept, the gate grep reading it.

  private val modsResourceRoot =
    Paths.get(Main.roots.sourceRoot.resolve(java.net.URI.create("../../mods/resources/")))

  private def discoverModsPaths(): Seq[String] =
    if (!Files.isDirectory(modsResourceRoot)) Seq.empty
    else Using.resource(Files.walk(modsResourceRoot)) { stream =>
      stream.iterator.asScala
        .filter(p => Files.isRegularFile(p))
        .map(p => modsResourceRoot.relativize(p).toString.replace('\\', '/'))
        .filter(_.endsWith(".json"))
        .toList
        .sorted
    }

  private def loadModsTypeDefinition(rp: String): TypeDefinition = {
    val bytes = Files.readAllBytes(modsResourceRoot.resolve(rp))
    parser.parse(new String(bytes)).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
  }

  private val modsPaths: Seq[String] = discoverModsPaths()

  modsPaths.foreach { rp =>
    val drakePath = deriveDrakePath(rp)
    if (Files.isRegularFile(modsResourceRoot.resolve(drakePath))) {
      test(s"mods $rp: Drake.emit matches $drakePath (whitespace-normalized)") {
        val td = loadModsTypeDefinition(rp)
        assert(td != TypeDefinition.Null, s"failed to parse mods $rp")
        val authored = new String(Files.readAllBytes(modsResourceRoot.resolve(drakePath)))
        val genNorm  = normalize(Drake.emit(td))
        val handNorm = normalize(authored)
        if (genNorm != handNorm) {
          log.info(s"\n--- mods $rp: emitted (normalized) ---\n$genNorm")
          log.info(s"\n--- $drakePath: hand-authored (normalized) ---\n$handNorm")
          fail(s"mods $rp: emitted drake differs from hand-authored $drakePath.\n" + diffReport(handNorm, genNorm))
        }
      }
    }
  }

  test("mods definitions still lacking a .drake (report only)") {
    val pending = modsPaths.filterNot(rp => Files.isRegularFile(modsResourceRoot.resolve(deriveDrakePath(rp))))
    log.info(s"mods definitions pending .drake authoring (${pending.size}): ${pending.mkString(", ")}")
    succeed
  }
}
