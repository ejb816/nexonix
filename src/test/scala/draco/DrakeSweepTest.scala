package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Phase-2a sweep: regenerate the plain-type `.drake` corpus from its normative
 *  JSON via `Generator.drake`, adopting the new-model surface (leaf list-blocks
 *  unbracketed, bare `factory`, `globals`, `[]`/`{}` empty-collection defaults).
 *
 *  One-shot by design: run explicitly (`testOnly draco.DrakeSweepTest`), review
 *  the git diff, commit. Re-running is idempotent — a file is rewritten only
 *  when its content differs from emission. Remove this suite once the sweep is
 *  committed and DrakeGenTest's census reads all-match.
 *
 *  AUTHORED-AHEAD EXCLUSIONS — files whose hand-authored surface deliberately
 *  runs ahead of the JSON (target-state syntax discovery the emitter cannot yet
 *  reconstruct); sweeping them would erase authored intent:
 *    - Action.drake / BodyElement.drake: present-empty `rule` / `actor` heads
 *      (presence-significant aspect model; JSON still elides present-empty)
 *    - ActorAspect.drake: same heads, plus the in-flight start/message/signal
 *      element renaming (JSON still setupAction/messageAction/signalAction) */
class DrakeSweepTest extends AnyFunSuite with PersistentTestLog {

  private val authoredAhead: Map[String, String] = Map(
    "draco/Action.json"      -> "present-empty rule/actor heads (presence model not yet in JSON)",
    "draco/BodyElement.json" -> "present-empty rule/actor heads (presence model not yet in JSON)",
    "draco/ActorAspect.json" -> "present-empty heads + start/message/signal rename (Phase 2b)"
  )

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

  private def isPlainType(td: TypeDefinition): Boolean =
    RuleAspect.isEmpty(td.ruleAspect) &&
    ActorAspect.isEmpty(td.actorAspect) &&
    CodecAspect.isEmpty(td.codecAspect)

  test("Phase-2a sweep: regenerate plain-type .drake corpus from JSON") {
    var written, unchanged, excluded, deferred = 0

    discoverResourcePaths().foreach { rp =>
      val td = loadTypeDefinition(rp)
      if (td == TypeDefinition.Null || !isPlainType(td)) deferred += 1
      else if (authoredAhead.contains(rp)) {
        excluded += 1
        log.info(s"$rp: EXCLUDED — ${authoredAhead(rp)}")
      } else {
        val emitted   = Generator.drake(td)
        val drakePath = Paths.get(Main.roots.sourceRoot).resolve(rp.stripSuffix(".json") + ".drake")
        val current   = if (Files.isRegularFile(drakePath)) Some(new String(Files.readAllBytes(drakePath), StandardCharsets.UTF_8)) else None
        if (current.contains(emitted)) unchanged += 1
        else {
          Files.write(drakePath, emitted.getBytes(StandardCharsets.UTF_8))
          written += 1
          log.info(s"$rp: wrote ${drakePath.getFileName}")
        }
      }
    }

    val summary = s"Phase-2a sweep: $written written, $unchanged unchanged, " +
      s"$excluded excluded (authored-ahead), $deferred deferred (rule/actor/codec)"
    console.info(s"  $summary")
    log.info(summary)
  }
}
