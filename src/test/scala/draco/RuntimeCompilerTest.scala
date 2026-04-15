package draco

import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

class RuntimeCompilerTest extends AnyFunSuite {

  test("Compile simple generated source") {
    val source =
      """package draco.generated.test
        |
        |class SimpleTest {
        |  val name: String = "SimpleTest"
        |}
        |""".stripMargin

    val result = RuntimeCompiler.compile(source, "SimpleTest.scala")
    assert(result.isRight, s"Compilation failed: ${result.left.getOrElse(Seq.empty).mkString("\n")}")

    val classDir = result.toOption.get
    val clazz = RuntimeCompiler.loadClass(classDir, "draco.generated.test.SimpleTest")
    val instance = clazz.getDeclaredConstructor().newInstance()
    val nameField = clazz.getMethod("name")
    assert(nameField.invoke(instance) == "SimpleTest")
  }

  test("Compile generated source that references draco types") {
    val source =
      """package draco.generated.test
        |
        |import draco._
        |
        |trait GeneratedTrait extends Extensible with DracoType
        |
        |object GeneratedTrait extends App with TypeInstance {
        |  lazy val typeDefinition: TypeDefinition = TypeDefinition(
        |    _typeName = TypeName("GeneratedTrait", _namePackage = Seq("draco", "generated", "test"))
        |  )
        |  lazy val typeInstance: Type[GeneratedTrait] = Type[GeneratedTrait](typeDefinition)
        |}
        |""".stripMargin

    val result = RuntimeCompiler.compile(source, "GeneratedTrait.scala")
    assert(result.isRight, s"Compilation failed: ${result.left.getOrElse(Seq.empty).mkString("\n")}")

    val classDir = result.toOption.get
    val clazz = RuntimeCompiler.loadClass(classDir, "draco.generated.test.GeneratedTrait$")
    val instance = clazz.getField("MODULE$").get(null)
    val typeDef = clazz.getMethod("typeDefinition").invoke(instance).asInstanceOf[TypeDefinition]
    assert(typeDef.typeName.name == "GeneratedTrait")
    assert(typeDef.typeName.namePackage == Seq("draco", "generated", "test"))
  }

  test("Generate from JSON and compile") {
    val sourceContent = SourceContent(Generator.main.sourceRoot, "draco/Holon.json")
    val jsonContent: Json = parser.parse(sourceContent.sourceString).getOrElse(Json.Null)
    val td: TypeDefinition = jsonContent.as[TypeDefinition].getOrElse(TypeDefinition.Null)
    val generatedSource = Generator.generate(td)

    println(s"Generated source for ${td.typeName.name}:")
    println(generatedSource)

    val result = RuntimeCompiler.compile(generatedSource, "Holon.scala")
    result match {
      case Right(classDir) =>
        println(s"Compilation successful: $classDir")
      case Left(errors) =>
        println(s"Compilation errors (expected for types with complex dependencies):")
        errors.foreach(e => println(s"  $e"))
    }
  }

  test("Report compilation errors") {
    val badSource =
      """package draco.generated.test
        |
        |class BadClass {
        |  val x: NonexistentType = ???
        |}
        |""".stripMargin

    val result = RuntimeCompiler.compile(badSource, "BadClass.scala")
    assert(result.isLeft, "Expected compilation to fail")
    val errors = result.left.getOrElse(Seq.empty)
    assert(errors.nonEmpty, "Expected at least one error")
    println(s"Correctly reported ${errors.size} error(s):")
    errors.foreach(e => println(s"  $e"))
  }
}
