package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class GeneratorDefinitionToSourceTest extends AnyFunSuite {

  /** Output path with .scala.generated extension */
  private def generatedOutputPath(typeName: String): String =
    s"draco/$typeName.scala.generated"

  /** Derive the actual source path from a TypeDefinition's typeName */
  private def actualSourcePath(td: TypeDefinition): String =
    td.typeName.namePackage.mkString("/") + "/" + td.typeName.name + ".scala"

  /** Read actual source file from src/main/scala, returns None if not found */
  private def readActualSource(sourcePath: String): Option[Seq[String]] = {
    try {
      val sc = SourceContent(Generator.main.sinkRoot, sourcePath)
      Some(sc.sourceLines)
    } catch {
      case _: Exception => None
    }
  }

  /** Compute a simple line-by-line diff between generated and actual source */
  private def computeDiff(generated: String, actual: Seq[String]): String = {
    val generatedLines = generated.linesIterator.toSeq
    val maxLines = math.max(generatedLines.size, actual.size)
    val diffs = (0 until maxLines).flatMap { i =>
      val gen = if (i < generatedLines.size) Some(generatedLines(i)) else None
      val act = if (i < actual.size) Some(actual(i)) else None
      (gen, act) match {
        case (Some(g), Some(a)) if g.trim != a.trim =>
          Some(
            s" * Line ${i + 1}:\n" +
            s" *   generated: $g\n" +
            s" *   actual:    $a"
          )
        case (Some(g), None) =>
          Some(
            s" * Line ${i + 1}:\n" +
            s" *   generated: $g\n" +
            s" *   actual:    (absent)"
          )
        case (None, Some(a)) =>
          Some(
            s" * Line ${i + 1}:\n" +
            s" *   generated: (absent)\n" +
            s" *   actual:    $a"
          )
        case _ => None
      }
    }
    if (diffs.isEmpty) " * No differences found."
    else diffs.mkString("\n")
  }

  /** Append diff as a multiline comment to the generated source */
  private def appendDiff(generatedSource: String, td: TypeDefinition): String = {
    val sourcePath = actualSourcePath(td)
    readActualSource(sourcePath) match {
      case Some(actualLines) =>
        val diff = computeDiff(generatedSource, actualLines)
        s"$generatedSource\n/* --- Diff: generated vs $sourcePath ---\n$diff\n */\n"
      case None =>
        s"$generatedSource\n/* --- No actual source found at $sourcePath --- */\n"
    }
  }

  private def generateAndVerify(resourcePath: String, typeName: String): Unit = {
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    val generatedSource = Generator.generate(td)
    val output = appendDiff(generatedSource, td)
    println(output)

    val outputPath = generatedOutputPath(typeName)
    val contentSink: ContentSink = ContentSink(Generator.generated.sinkRoot, outputPath)
    contentSink.write(output)
  }

  private def generateMultiAndVerify(resourcePaths: Seq[String], typeName: String): Unit = {
    val typeDefinitions: Seq[TypeDefinition] = resourcePaths.map { rp =>
      val sourceContent = SourceContent(Generator.main.sourceRoot, rp)
      val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
      jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    }
    typeDefinitions.foreach(td => println(td.typeName.name))

    val generatedSource = Generator.generate(typeDefinitions)
    // Use first type for diff comparison (the root of the hierarchy)
    val output = appendDiff(generatedSource, typeDefinitions.head)
    println(output)

    val outputPath = generatedOutputPath(typeName)
    val contentSink: ContentSink = ContentSink(Generator.generated.sinkRoot, outputPath)
    contentSink.write(output)
  }

  test("Generate Actor") {
    generateAndVerify("draco/Actor.json", "Actor")
  }

  test("Generate ActorInstance") {
    generateAndVerify("draco/ActorInstance.json", "ActorInstance")
  }

  test("Generate ActorType") {
    generateAndVerify("draco/ActorType.json", "ActorType")
  }

  test("Generate ContentSink") {
    generateAndVerify("draco/ContentSink.json", "ContentSink")
  }

  test("Generate Dictionary") {
    generateAndVerify("draco/Dictionary.json", "Dictionary")
  }

  test("Generate Domain") {
    generateAndVerify("draco/Domain.json", "Domain")
  }

  test("Generate DomainDictionary") {
    generateAndVerify("draco/DomainDictionary.json", "DomainDictionary")
  }

  test("Generate DomainInstance") {
    generateAndVerify("draco/DomainInstance.json", "DomainInstance")
  }

  test("Generate DomainType") {
    generateAndVerify("draco/DomainType.json", "DomainType")
  }

  test("Generate Draco") {
    generateAndVerify("draco/Draco.json", "Draco")
  }

  test("Generate DracoType") {
    generateAndVerify("draco/DracoType.json", "DracoType")
  }

  test("Generate Extensible") {
    generateAndVerify("draco/Extensible.json", "Extensible")
  }

  test("Generate Main") {
    generateAndVerify("draco/Main.json", "Main")
  }

  test("Generate Primal") {
    generateAndVerify("draco/Primal.json", "Primal")
  }

  test("Generate Rule") {
    generateAndVerify("draco/Rule.json", "Rule")
  }

  test("Generate RuleInstance") {
    generateAndVerify("draco/RuleInstance.json", "RuleInstance")
  }

  test("Generate RuleType") {
    generateAndVerify("draco/RuleType.json", "RuleType")
  }

  test("Generate Specifically") {
    generateAndVerify("draco/Specifically.json", "Specifically")
  }

  test("Generate SourceContent") {
    generateAndVerify("draco/SourceContent.json", "SourceContent")
  }

  test("Generate Test") {
    generateAndVerify("draco/Test.json", "Test")
  }

  test("Generate Type") {
    generateAndVerify("draco/Type.json", "Type")
  }

  test("Generate TypeDefinition") {
    generateAndVerify("draco/TypeDefinition.json", "TypeDefinition")
  }

  test("Generate TypeDictionary") {
    generateAndVerify("draco/TypeDictionary.json", "TypeDictionary")
  }

  test("Generate TypeElement hierarchy") {
    generateMultiAndVerify(
      Seq(
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
      ),
      "TypeElement"
    )
  }

  test("Generate TypeInstance") {
    generateAndVerify("draco/TypeInstance.json", "TypeInstance")
  }

  test("Generate TypeName") {
    generateAndVerify("draco/TypeName.json", "TypeName")
  }

  test("Generate Value") {
    generateAndVerify("draco/Value.json", "Value")
  }
}
