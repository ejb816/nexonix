package draco.language

import draco._
import io.circe.parser
import io.circe.syntax.EncoderOps
import io.circe.yaml.{parser => yamlParser}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

/** Round-trip every TypeDefinition JSON through YAML and back, then compare the
  * canonical encoded JSON of the original vs the YAML-round-tripped TypeDefinition.
  *
  * Pipeline tested per file:
  *   read JSON → decode to TypeDefinition → YAML.emit → yamlParser.parse → decode to TypeDefinition
  *   then compare originalTd.asJson == roundTrippedTd.asJson (structural Json equality).
  *
  * Outcomes:
  *   PASS              — full round-trip preserves the TypeDefinition data
  *   JSON_PARSE_FAIL   — JSON file is malformed (pre-existing, unrelated to YAML)
  *   JSON_DECODE_FAIL  — JSON shape doesn't decode to TypeDefinition (pre-existing)
  *   YAML_PARSE_FAIL   — emitted YAML can't be re-parsed by circe-yaml
  *   YAML_DECODE_FAIL  — re-parsed YAML can't decode back to TypeDefinition
  *   STRUCTURAL_DIFF   — round-trip succeeded but the data changed
  *
  * The test fails only on YAML-attributable outcomes (YAML_*, STRUCTURAL_DIFF, EXCEPTION).
  * JSON_PARSE_FAIL / JSON_DECODE_FAIL are reported but don't fail the test, so a
  * pre-existing JSON regression doesn't masquerade as a YAML problem.
  */
class YAMLRoundTripTest extends AnyFunSuite {

  private case class Outcome(path: String, status: String, detail: String = "") {
    def isYamlFailure: Boolean =
      status.startsWith("YAML_") || status == "STRUCTURAL_DIFF" || status == "EXCEPTION"
  }

  private def findJsonFiles(dir: File): Seq[File] = {
    if (!dir.exists) Seq.empty
    else {
      val files = dir.listFiles().toSeq
      val jsonFiles = files.filter(f => f.isFile && f.getName.endsWith(".json"))
      val subdirs = files.filter(_.isDirectory)
      jsonFiles ++ subdirs.flatMap(findJsonFiles)
    }
  }

  private def shortPath(f: File, root: File): String = {
    val rootPath = root.getAbsolutePath
    val abs = f.getAbsolutePath
    if (abs.startsWith(rootPath)) abs.substring(rootPath.length).stripPrefix("/")
    else abs
  }

  private def readFile(f: File): String = {
    val src = scala.io.Source.fromFile(f)
    try src.mkString finally src.close()
  }

  private def roundTrip(file: File, root: File): Outcome = {
    val short = shortPath(file, root)
    val jsonText =
      try readFile(file)
      catch { case e: Exception => return Outcome(short, "EXCEPTION", s"read: ${e.getMessage}") }

    parser.parse(jsonText) match {
      case Left(err) =>
        Outcome(short, "JSON_PARSE_FAIL", err.message)

      case Right(originalJson) =>
        originalJson.as[TypeDefinition] match {
          case Left(err) =>
            Outcome(short, "JSON_DECODE_FAIL", err.message)

          case Right(originalTd) =>
            val originalCanonical = originalTd.asJson
            try {
              val yaml = YAML.emit(originalTd)
              yamlParser.parse(yaml) match {
                case Left(err) =>
                  Outcome(short, "YAML_PARSE_FAIL", err.message)

                case Right(parsedJson) =>
                  parsedJson.as[TypeDefinition] match {
                    case Left(err) =>
                      Outcome(short, "YAML_DECODE_FAIL", err.message)

                    case Right(roundTrippedTd) =>
                      val rtCanonical = roundTrippedTd.asJson
                      if (rtCanonical == originalCanonical) Outcome(short, "PASS")
                      else
                        Outcome(
                          short,
                          "STRUCTURAL_DIFF",
                          s"original:\n${originalCanonical.spaces2}\n--- round-tripped:\n${rtCanonical.spaces2}"
                        )
                  }
              }
            } catch {
              case e: Exception => Outcome(short, "EXCEPTION", e.getMessage)
            }
        }
    }
  }

  test("YAML round-trip preserves TypeDefinition over the full corpus") {
    val mainRoot = new File(Generator.main.sourceRoot)
    val testRoot = new File(Test.roots.sourceRoot)
    val mainFiles = findJsonFiles(mainRoot).map(f => (f, mainRoot))
    val testFiles = findJsonFiles(testRoot).map(f => (f, testRoot))
    val allFiles = (mainFiles ++ testFiles).sortBy(_._1.getAbsolutePath)

    val outcomes: Seq[Outcome] = allFiles.map { case (f, root) => roundTrip(f, root) }

    val byStatus: Map[String, Int] = outcomes.groupBy(_.status).view.mapValues(_.size).toMap
    val total = outcomes.size
    val passed = byStatus.getOrElse("PASS", 0)
    val yamlFailures = outcomes.count(_.isYamlFailure)

    println()
    println("=" * 100)
    println(f"  YAML ROUND-TRIP REPORT: $passed%d / $total%d passed, $yamlFailures%d YAML failures")
    println("=" * 100)
    byStatus.toSeq.sortBy(-_._2).foreach { case (s, n) => println(f"    $s%-18s $n%d") }

    val failures = outcomes.filterNot(_.status == "PASS")
    if (failures.nonEmpty) {
      println()
      println("DETAILS:")
      failures.foreach { o =>
        println(s"  [${o.status}] ${o.path}")
        if (o.detail.nonEmpty) {
          val truncated = if (o.detail.length > 800) o.detail.take(800) + "\n    ...(truncated)" else o.detail
          truncated.linesIterator.foreach(line => println(s"      $line"))
        }
      }
    }
    println()

    assert(
      yamlFailures == 0,
      s"$yamlFailures of $total files exhibited YAML round-trip failures (see report above)"
    )
  }
}
