package domains

import draco._
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

/** Example-domain gate: generate Scala from every JSON-backed type in the `src/mods`
  * example domains (aerial / terrestrial / marine / ethereal / world / sentient) and
  * require it to equal the committed `.scala`, whitespace-normalized, as `DracoGenTest`
  * does for the main corpus.
  *
  * This was a report-only discovery map from 2026-06-23 until 2026-10-31: the example
  * domains were hand-written ahead of the Generator, and the map charted the gap as
  * it closed — 11 differences on 2026-10-29, 9 on 2026-10-31, zero once the four
  * ConsumeReport trios took their surfaces and the last nine companions became the
  * generator's output. It still writes the per-type MATCH/DIFF/ERROR/MISSING report
  * (with full diffs) to `target/test-output/ExampleDomainsGenTest.log` and the one-line
  * summary to the console; the summary is now a gate, not a measurement.
  *
  * The walker reads only `.json`; types with no JSON (Sinks, Geodesy, EgoActor) are
  * not example *definitions* and are out of scope by construction.
  */
class ExampleDomainsGenTest extends AnyFunSuite with PersistentTestLog {

  private val resourceRoot = Paths.get("src", "mods", "resources")
  private val scalaRoot    = Paths.get("src", "mods", "scala")

  /** "domains/x/T.json" -> "domains/x/T.scala". Rule-ness is aspect presence, not a
    * name suffix: every type — plain, rule, or actor — emits a bare-named object
    * (mirrors DracoGenTest). */
  private def deriveScalaPath(rp: String): String =
    rp.stripSuffix(".json") + ".scala"

  /** Every `.json` under `src/mods/resources/domains`, as logical paths. */
  private def discover(): Seq[String] = {
    val domainsDir = resourceRoot.resolve("domains")
    Using.resource(Files.walk(domainsDir)) { stream =>
      stream.iterator.asScala
        .filter(Files.isRegularFile(_))
        .map(p => resourceRoot.relativize(p).toString.replace('\\', '/'))
        .filter(_.endsWith(".json"))
        .toList
        .sorted
    }
  }

  private def loadTd(rp: String): TypeDefinition = {
    val s = Source.fromFile(resourceRoot.resolve(rp).toFile)
    val text = try s.mkString finally s.close()
    parser.parse(text).getOrElse(Json.Null).as[TypeDefinition].getOrElse(TypeDefinition.Null)
  }

  private def readHandWritten(scalaPath: String): Option[String] = {
    val f = scalaRoot.resolve(scalaPath).toFile
    if (!f.exists) None
    else {
      val s = Source.fromFile(f)
      try Some(s.mkString) finally s.close()
    }
  }

  /** Whitespace-normalize without erasing structural differences (as DracoGenTest). */
  private def normalize(src: String): String = {
    val lines = src.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).toSeq
    val collapsed = lines.foldLeft(Seq.empty[String]) { (acc, l) =>
      if (l.isEmpty && acc.lastOption.contains("")) acc else acc :+ l
    }
    collapsed.dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse.mkString("\n")
  }

  private def diffReport(hand: String, gen: String): String = {
    val h = hand.split('\n').toSeq
    val g = gen.split('\n').toSeq
    val n = math.max(h.size, g.size)
    val rows = (0 until n).map { i =>
      val a = h.lift(i).getOrElse("")
      val b = g.lift(i).getOrElse("")
      f"${if (a == b) "  " else "!!"} ${i + 1}%3d | $a%-60s | $b"
    }
    "\n        hand-written" + " " * 48 + "| generated\n" + rows.mkString("\n")
  }

  test("every example-domain definition generates its committed Scala (gate)") {
    val rps = discover()
    var matched = 0; var differ = 0; var errored = 0; var missing = 0

    rps.foreach { rp =>
      val scalaPath = deriveScalaPath(rp)
      Try {
        val td = loadTd(rp)
        if (td == TypeDefinition.Null) throw new RuntimeException("JSON did not decode to a TypeDefinition")
        val gen = normalize(DracoGenerator.generate(td))
        readHandWritten(scalaPath) match {
          case None =>
            missing += 1
            log.info(s"[MISSING] $rp  (no hand-written $scalaPath)")
          case Some(handRaw) =>
            val hand = normalize(handRaw)
            if (hand == gen) { matched += 1; log.info(s"[MATCH]   $rp") }
            else { differ += 1; log.info(s"[DIFF]    $rp  vs $scalaPath${diffReport(hand, gen)}\n") }
        }
      }.recover { case e =>
        errored += 1
        log.info(s"[ERROR]   $rp  (${e.getClass.getSimpleName}: ${e.getMessage})")
      }
    }

    val total = rps.size
    log.info(f"%nSUMMARY: $matched%d MATCH / $differ%d DIFF / $errored%d ERROR / $missing%d MISSING  (of $total%d JSON-backed types)")
    console.info(f"  EXAMPLE-DOMAIN GEN MAP: $matched%d match, $differ%d differ, $errored%d error, $missing%d missing (of $total%d) — asserted zero differ; details in the log")

    assert(total > 0, "no example-domain JSON discovered under src/mods/resources/domains")
    assert(missing == 0, s"$missing example definition(s) have no committed Scala — see the log")
    assert(errored == 0, s"$errored example definition(s) failed to load or generate — see the log")
    assert(differ == 0, s"$differ example definition(s) generate Scala that differs from the committed file — see the log")
  }
}
