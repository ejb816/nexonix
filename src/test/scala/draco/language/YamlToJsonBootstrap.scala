package draco.language

import draco._
import io.circe.syntax.EncoderOps
import io.circe.yaml.{parser => yamlParser}
import org.scalatest.funsuite.AnyFunSuite

import java.io.{File, PrintWriter}

/** One-shot bootstrap: for every .yaml file under src/main/resources/draco/,
  * load it as a TypeDefinition and write a sibling .json file containing the
  * canonical JSON encoding. Always overwrites any existing .json sibling so it
  * is safe to re-run whenever the YAML corpus changes.
  *
  * Run via: sbt "testOnly *YamlToJsonBootstrap"
  */
class YamlToJsonBootstrap extends AnyFunSuite {

  private def findYamlFiles (dir: File) : Seq[File] = {
    if (!dir.exists) Seq.empty
    else {
      val files = dir.listFiles().toSeq
      val yamlFiles = files.filter(f => f.isFile && f.getName.endsWith(".yaml"))
      val subdirs = files.filter(_.isDirectory)
      yamlFiles ++ subdirs.flatMap(findYamlFiles)
    }
  }

  private def readFile (f: File) : String = {
    val src = scala.io.Source.fromFile(f)
    try src.mkString finally src.close()
  }

  private def writeFile (f: File, content: String) : Unit = {
    val pw = new PrintWriter(f)
    try pw.write(content) finally pw.close()
  }

  test("Generate sibling JSON for every YAML file under src/main/resources/draco") {
    val root = new File("src/main/resources/draco")
    val yamlFiles = findYamlFiles(root).sortBy(_.getAbsolutePath)

    println()
    println("=" * 100)
    println(s"  YAML→JSON BOOTSTRAP: ${yamlFiles.size} files under ${root.getPath}")
    println("=" * 100)

    val results: Seq[(File, String)] = yamlFiles.map { yamlFile =>
      val outcome =
        try {
          val yamlText = readFile(yamlFile)
          yamlParser.parse(yamlText) match {
            case Left(err) => s"YAML_PARSE_FAIL: ${err.message}"
            case Right(json) =>
              json.as[TypeDefinition] match {
                case Left(err) => s"DECODE_FAIL: ${err.message}"
                case Right(td) =>
                  val jsonPath = yamlFile.getAbsolutePath.replaceAll("\\.yaml$", ".json")
                  val jsonText = td.asJson.spaces2 + "\n"
                  writeFile(new File(jsonPath), jsonText)
                  s"WROTE ${jsonText.length} chars -> ${new File(jsonPath).getName}"
              }
          }
        } catch {
          case e: Exception => s"EXCEPTION: ${e.getMessage}"
        }
      (yamlFile, outcome)
    }

    results.foreach { case (f, status) =>
      println(f"  ${f.getName}%-32s $status")
    }
    println()

    val failures = results.filter { case (_, s) => !s.startsWith("WROTE") }
    assert(
      failures.isEmpty,
      s"${failures.size} of ${results.size} files failed: ${failures.map(_._1.getName).mkString(", ")}"
    )
  }
}
