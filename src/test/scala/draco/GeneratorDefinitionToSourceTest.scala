package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.StoreReporter

class GeneratorDefinitionToSourceTest extends AnyFunSuite {

  private val generatedPrefix = "generated"

  /** Rewrite package declaration to generated.{original} so generated types
    * coexist with the real framework types without shadowing. */
  private def rewritePackage(source: String): String = {
    source.replaceFirst("(?m)^package ", s"package $generatedPrefix.")
  }

  /** Output path under generated/ with .scala extension */
  private def generatedOutputPath(typeName: String): String =
    s"$generatedPrefix/draco/$typeName.scala"

  private def compileCheck(outputPath: String): Unit = {
    val generatedFile = Paths.get(Generator.test.sinkRoot.resolve(outputPath)).toString
    try {
      val settings = new Settings()
      settings.classpath.value = System.getProperty("java.class.path")
      settings.usejavacp.value = true
      settings.stopAfter.value = List("typer")

      val reporter = new StoreReporter(settings)
      val compiler = new Global(settings, reporter)
      val run = new compiler.Run()
      run.compile(List(generatedFile))

      if (reporter.hasErrors) {
        reporter.infos.foreach(info => println(s"${info.severity}: ${info.msg}"))
        fail(s"Generated source ($outputPath) did not compile")
      } else {
        println(s"Generated source ($outputPath) compiled successfully")
      }
    } catch {
      case _: scala.reflect.internal.MissingRequirementError =>
        println("Skipping compilation check (scala-library not on embedded compiler classpath)")
    }
  }

  private def generateAndVerify(resourcePath: String, typeName: String): Unit = {
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    val generatedSource = rewritePackage(Generator.generate(td))
    println(generatedSource)

    val outputPath = generatedOutputPath(typeName)
    val contentSink: ContentSink = ContentSink(Generator.test.sinkRoot, outputPath)
    contentSink.write(generatedSource)
    compileCheck(outputPath)
  }

  private def generateActorAndVerify(resourcePath: String, typeName: String): Unit = {
    val sourceContent = SourceContent(Generator.main.sourceRoot, resourcePath)
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    println(jsonContent.spaces2)

    val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    val generatedSource = rewritePackage(Generator.generate(td, ActorDefinition.Null))
    println(generatedSource)

    val outputPath = generatedOutputPath(typeName)
    val contentSink: ContentSink = ContentSink(Generator.test.sinkRoot, outputPath)
    contentSink.write(generatedSource)
    compileCheck(outputPath)
  }

  private def generateMultiAndVerify(resourcePaths: Seq[String], typeName: String): Unit = {
    val typeDefinitions: Seq[TypeDefinition] = resourcePaths.map { rp =>
      val sourceContent = SourceContent(Generator.main.sourceRoot, rp)
      val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
      jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    }
    typeDefinitions.foreach(td => println(td.typeName.name))

    val generatedSource = rewritePackage(Generator.generate(typeDefinitions))
    println(generatedSource)

    val outputPath = generatedOutputPath(typeName)
    val contentSink: ContentSink = ContentSink(Generator.test.sinkRoot, outputPath)
    contentSink.write(generatedSource)
    compileCheck(outputPath)
  }

  test("Generate Actor") {
    generateActorAndVerify("draco/Actor.json", "Actor")
  }

  test("Generate ActorDefinition") {
    generateAndVerify("draco/ActorDefinition.json", "ActorDefinition")
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

  test("Generate DomainDefinition") {
    generateAndVerify("draco/DomainDefinition.json", "DomainDefinition")
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

  test("Generate Main") {
    generateAndVerify("draco/Main.json", "Main")
  }

  test("Generate Primal") {
    generateAndVerify("draco/Primal.json", "Primal")
  }

  test("Generate Rule") {
    generateAndVerify("draco/Rule.json", "Rule")
  }

  test("Generate RuleDefinition") {
    generateAndVerify("draco/RuleDefinition.json", "RuleDefinition")
  }

  test("Generate RuleInstance") {
    generateAndVerify("draco/RuleInstance.json", "RuleInstance")
  }

  test("Generate RuleType") {
    generateAndVerify("draco/RuleType.json", "RuleType")
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
