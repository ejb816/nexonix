package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite
import java.io.File

class GenerateAndCompileTest extends AnyFunSuite {

  private val resourceRoot = Generator.main.sourceRoot

  /** TypeElement hierarchy — must be compiled as one unit (sealed traits) */
  private val typeElementGroup: Set[String] = Set(
    "draco/TypeElement.json",
    "draco/BodyElement.json",
    "draco/Fixed.json",
    "draco/Mutable.json",
    "draco/Dynamic.json",
    "draco/Parameter.json",
    "draco/Monadic.json",
    "draco/Pattern.json",
    "draco/Action.json",
    "draco/Condition.json",
    "draco/Variable.json",
    "draco/Factory.json"
  )

  private def findJsonFiles(dir: File): Seq[File] = {
    if (!dir.exists) Seq.empty
    else {
      val files = dir.listFiles().toSeq
      val jsonFiles = files.filter(f => f.isFile && f.getName.endsWith(".json"))
      val subdirs = files.filter(_.isDirectory)
      jsonFiles ++ subdirs.flatMap(findJsonFiles)
    }
  }

  private def resourcePath(file: File): String = {
    val root = new File(resourceRoot).getAbsolutePath
    file.getAbsolutePath.stripPrefix(root).stripPrefix("/")
  }

  private def loadTypeDefinition(rp: String): Either[String, (TypeDefinition, String)] = {
    try {
      val sourceContent = SourceContent(resourceRoot, rp)
      val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
      val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
      if (td == null || td == TypeDefinition.Null)
        Left("Failed to parse TypeDefinition from JSON")
      else
        Right((td, td.typeName.name))
    } catch {
      case e: Exception => Left(s"JSON load exception: ${e.getMessage}")
    }
  }

  private def generateSource(td: TypeDefinition): Either[String, String] = {
    try {
      val source = Generator.generate(td)
      if (source == null || source.isEmpty) Left("Generator produced empty source")
      else Right(source)
    } catch {
      case e: Exception => Left(s"Generator exception: ${e.getMessage}")
    }
  }

  private def generateSourceMulti(tds: Seq[TypeDefinition]): Either[String, String] = {
    try {
      val source = Generator.generate(tds)
      if (source == null || source.isEmpty) Left("Generator produced empty source")
      else Right(source)
    } catch {
      case e: Exception => Left(s"Generator exception: ${e.getMessage}")
    }
  }

  case class TestRecord(
    typeName: String,
    resourcePath: String,
    jsonParsed: Boolean,
    sourceGenerated: Boolean,
    compiled: Boolean,
    errors: Seq[String]
  ) {
    def status: String = if (compiled) "PASS" else if (sourceGenerated) "COMPILE_FAIL" else if (jsonParsed) "GENERATE_FAIL" else "JSON_FAIL"

    def summary: String = {
      val errorSummary = if (errors.nonEmpty) s"  ${errors.head}" else ""
      f"  $status%-14s $typeName%-30s $resourcePath$errorSummary"
    }
  }

  private def testSingle(rp: String): TestRecord = {
    loadTypeDefinition(rp) match {
      case Left(err) =>
        TestRecord(rp, rp, jsonParsed = false, sourceGenerated = false, compiled = false, errors = Seq(err))
      case Right((td, name)) =>
        generateSource(td) match {
          case Left(err) =>
            TestRecord(name, rp, jsonParsed = true, sourceGenerated = false, compiled = false, errors = Seq(err))
          case Right(source) =>
            val fileName = s"${name.replaceAll("\\[.*", "")}.scala"
            RuntimeCompiler.compile(source, fileName) match {
              case Right(_) =>
                TestRecord(name, rp, jsonParsed = true, sourceGenerated = true, compiled = true, errors = Seq.empty)
              case Left(errs) =>
                TestRecord(name, rp, jsonParsed = true, sourceGenerated = true, compiled = false, errors = errs.take(3))
            }
        }
    }
  }

  private def testGroup(groupName: String, resourcePaths: Seq[String]): TestRecord = {
    val loaded: Seq[Either[String, (TypeDefinition, String)]] = resourcePaths.map(loadTypeDefinition)
    val failures = loaded.collect { case Left(err) => err }
    if (failures.nonEmpty) {
      return TestRecord(groupName, resourcePaths.mkString(", "), jsonParsed = false,
        sourceGenerated = false, compiled = false, errors = failures)
    }

    val tds = loaded.collect { case Right((td, _)) => td }
    generateSourceMulti(tds) match {
      case Left(err) =>
        TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
          sourceGenerated = false, compiled = false, errors = Seq(err))
      case Right(source) =>
        RuntimeCompiler.compile(source, s"$groupName.scala") match {
          case Right(_) =>
            TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
              sourceGenerated = true, compiled = true, errors = Seq.empty)
          case Left(errs) =>
            TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
              sourceGenerated = true, compiled = false, errors = errs.take(3))
        }
    }
  }

  test("Generate and compile all type definitions") {
    val rootDir = new File(resourceRoot)
    val jsonFiles = findJsonFiles(rootDir).sortBy(_.getAbsolutePath)
    val allPaths = jsonFiles.map(resourcePath)

    // Split into individual types and the TypeElement group
    val individualPaths = allPaths.filterNot(typeElementGroup.contains)
    val groupPaths = allPaths.filter(typeElementGroup.contains)

    // Test individual types
    val individualRecords: Seq[TestRecord] = individualPaths.map(testSingle)

    // Test TypeElement hierarchy as one compile unit
    val groupRecord: Seq[TestRecord] = if (groupPaths.nonEmpty) {
      Seq(testGroup("TypeElement hierarchy", groupPaths))
    } else Seq.empty

    val records = individualRecords ++ groupRecord

    val passed = records.count(_.compiled)
    val failed = records.count(r => !r.compiled)

    println()
    println("=" * 100)
    println(f"  GENERATE AND COMPILE REPORT: $passed%d passed, $failed%d failed, ${records.size}%d total")
    println("=" * 100)
    println()
    records.sortBy(r => (!r.compiled, r.typeName)).foreach(r => println(r.summary))
    println()
    println("=" * 100)

    if (failed > 0) {
      println()
      println("FAILURES:")
      println()
      records.filter(!_.compiled).foreach { r =>
        println(s"  ${r.typeName} (${r.resourcePath}):")
        r.errors.foreach(e => println(s"    $e"))
        println()
      }
    }
  }
}
