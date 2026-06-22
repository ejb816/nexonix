package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class GenerateAndCompileTest extends AnyFunSuite with PersistentTestLog {

  private val resourceRoot = Generator.main.sourceRoot

  /** TypeElement hierarchy — sealed-trait family that must compile together */
  private val typeElementGroup: Seq[String] = Seq(
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

  /** TypeDefinition + Aspects family — interrelated through `Aspects` */
  private val typeDefinitionGroup: Seq[String] = Seq(
    "draco/TypeDefinition.json",
    "draco/Aspects.json",
    "draco/DracoAspect.json",
    "draco/DomainAspect.json",
    "draco/RuleAspect.json",
    "draco/ActorAspect.json"
  )

  /** All other types defined under `src/main/resources/draco/` root. Compiled as a
    * single unit so cross-references between members resolve within the group. */
  private val dracoCoreGroup: Seq[String] = Seq(
    "draco/Actor.json",
    "draco/ActorType.json",
    "draco/CLI.json",
    "draco/ContentSink.json",
    "draco/Dictionary.json",
    "draco/Domain.json",
    "draco/DomainDictionary.json",
    "draco/DomainTransform.json",
    "draco/DomainType.json",
    "draco/Draco.json",
    "draco/DracoType.json",
    "draco/Holon.json",
    "draco/Main.json",
    "draco/Primal.json",
    "draco/REPL.json",
    "draco/Rule.json",
    "draco/RuleType.json",
    "draco/SourceContent.json",
    "draco/Test.json",
    "draco/Type.json",
    "draco/TypeDictionary.json",
    "draco/TypeName.json",
    "draco/TypeTransform.json",
    "draco/Value.json"
  )

  private def loadTypeDefinition(rp: String): Either[String, (TypeDefinition, String)] = {
    try {
      val sourceContent = SourceContent(resourceRoot, rp)
      val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
      val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(null)
      if (td == null || td == TypeDefinition.Null)
        Left(s"Failed to parse TypeDefinition from $rp")
      else
        Right((td, td.typeName.name))
    } catch {
      case e: Exception => Left(s"Load exception for $rp: ${e.getMessage}")
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
    def status: String =
      if (compiled) "PASS"
      else if (sourceGenerated) "COMPILE_FAIL"
      else if (jsonParsed) "GENERATE_FAIL"
      else "JSON_FAIL"
  }

  private def testGroup(groupName: String, resourcePaths: Seq[String]): TestRecord = {
    val loaded: Seq[Either[String, (TypeDefinition, String)]] = resourcePaths.map(loadTypeDefinition)
    val failures = loaded.collect { case Left(err) => err }
    if (failures.nonEmpty) {
      return TestRecord(groupName, resourcePaths.mkString(", "), jsonParsed = false,
        sourceGenerated = false, compiled = false, errors = failures)
    }

    val tds = loaded.collect { case Right((td, _)) => td }
    val typeNames = loaded.collect { case Right((_, name)) => name }
    generateSourceMulti(tds) match {
      case Left(err) =>
        TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
          sourceGenerated = false, compiled = false, errors = Seq(err))
      case Right(source) =>
        Generator.compile(source, s"$groupName.scala") match {
          case Right(_) =>
            TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
              sourceGenerated = true, compiled = true, errors = Seq.empty)
          case Left(errs) =>
            // Dump the generated source so the failing type/line can be inspected
            val safeName = groupName.replaceAll("[^A-Za-z0-9]+", "_")
            val dumpPath = s"/tmp/$safeName-generated.scala"
            try {
              val pw = new java.io.PrintWriter(dumpPath)
              try pw.write(source) finally pw.close()
              log.info(s"\n[generated source dumped to $dumpPath]")
              log.info(s"[group members: ${typeNames.mkString(", ")}]")
            } catch { case _: Throwable => () }
            TestRecord(groupName, s"[${tds.size} types]", jsonParsed = true,
              sourceGenerated = true, compiled = false, errors = errs.take(20))
        }
    }
  }

  private def reportRecord(r: TestRecord): Unit = {
    log.info("")
    log.info("=" * 100)
    log.info(f"  ${r.status}%-14s ${r.typeName}  ${r.resourcePath}")
    log.info("=" * 100)
    if (r.errors.nonEmpty) {
      log.info("")
      log.info("DETAILS:")
      r.errors.foreach(e => log.info(s"  $e"))
    }
    log.info("")
  }

  test("TypeElement group: generate and compile") {
    val record = testGroup("TypeElement hierarchy", typeElementGroup)
    reportRecord(record)
    assert(record.compiled, s"TypeElement group failed: ${record.errors.mkString("; ")}")
  }

  test("TypeDefinition group: generate and compile") {
    val record = testGroup("TypeDefinition family", typeDefinitionGroup)
    reportRecord(record)
    assert(record.compiled, s"TypeDefinition group failed: ${record.errors.mkString("; ")}")
  }

  test("Draco core group: generate and compile") {
    val record = testGroup("Draco core types", dracoCoreGroup)
    reportRecord(record)
    assert(record.compiled, s"Draco core group failed: ${record.errors.mkString("; ")}")
  }
}
