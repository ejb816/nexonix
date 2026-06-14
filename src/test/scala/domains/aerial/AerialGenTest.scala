package domains.aerial

import draco._
import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

/** Generate-then-compile loop for the Aerial actor + rule.
  *
  * The actor (`Consumer`) and rule (`ConsumeReport`) Scala is Generator-PRODUCED
  * (novel emission shapes, not hand-templated like the domain/shell). This test
  * loads their JSON, generates the Scala, and writes it into
  * `src/mods/scala/domains/aerial/`. It also prints each result so the emission can
  * be reviewed before compiling.
  *
  * Workflow:
  *   1. sbt "testOnly domains.aerial.AerialGenTest"   // writes Consumer.scala + ConsumeReportRule.scala
  *   2. reload / compile                              // picks up the generated sources
  *   3. run the behavioral test (added once this compiles)
  *
  * It writes source, so it is not part of the normal suite's assertions — running
  * it is a deliberate generation step.
  */
class AerialGenTest extends AnyFunSuite {

  private val outDir = Paths.get("src", "mods", "scala", "domains", "aerial")

  private def emit(fileName: String, scala: String): Unit = {
    val path = outDir.resolve(fileName)
    Files.write(path, scala.getBytes(StandardCharsets.UTF_8))
    println(s"\n--- wrote $path ---\n$scala")
  }

  test("generate Consumer actor and ConsumeReport rule") {
    val consumer = Generator.loadType(TypeName("Consumer", _namePackage = Seq("domains", "aerial")))
    val consumeReport = Generator.loadRuleType(TypeName("ConsumeReport", _namePackage = Seq("domains", "aerial")))

    emit("Consumer.scala", Generator.generate(consumer))
    emit("ConsumeReportRule.scala", Generator.generate(consumeReport))

    assert(Files.exists(outDir.resolve("Consumer.scala")))
    assert(Files.exists(outDir.resolve("ConsumeReportRule.scala")))
  }
}
