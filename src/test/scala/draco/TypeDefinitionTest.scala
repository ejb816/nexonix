package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TypeDefinitionTest extends AnyFunSuite {

  // TypeDefinition describing itself
  val typeDefinitionTypeDefinition: TypeDefinition = TypeDefinition(
    _typeName = TypeName(
      _name = "TypeDefinition",
      _namePackage = Seq("draco")
    ),
    _modules = Seq.empty,
    _derivation = Seq.empty,
    _elements = Seq(
      Fixed("typeName", "TypeName"),
      Fixed("modules", "Seq[TypeName]"),
      Fixed("derivation", "Seq[TypeName]"),
      Fixed("elements", "Seq[TypeElement]"),
      Fixed("factory", "Factory"),
      Fixed("globalElements", "Seq[BodyElement]")
    ),
    _factory = Factory(
      _fullName = "draco.TypeDefinition",
      _parameters = Seq(
        Parameter("typeName", "TypeName", ""),
        Parameter("modules", "Seq[TypeName]", "Seq.empty"),
        Parameter("derivation", "Seq[TypeName]", "Seq.empty"),
        Parameter("elements", "Seq[TypeElement]", "Seq.empty"),
        Parameter("factory", "Factory", "Factory.Null"),
        Parameter("globalElements", "Seq[BodyElement]", "Seq.empty")
      ),
      _body = Seq(
        Fixed("typeName", "TypeName", "_typeName"),
        Fixed("modules", "Seq[TypeName]", "_modules"),
        Fixed("derivation", "Seq[TypeName]", "_derivation"),
        Fixed("elements", "Seq[TypeElement]", "_elements"),
        Fixed("factory", "Factory", "_factory"),
        Fixed("globalElements", "Seq[BodyElement]", "_globalElements")
      )
    ),
    _globalElements = Seq(
      Dynamic(
        "load",
        "TypeDefinition",
        Seq(Parameter("typeName", "TypeName", "")),
        Seq(
          Fixed("sourceContent", "SourceContent", "SourceContent(Generator.main.sourceRoot, typeName.resourcePath)"),
          Fixed("sourceJSON", "Json", "parser.parse(sourceContent.sourceString).getOrElse(TypeDefinition(typeName).asJson)"),
          Fixed("result", "TypeDefinition", "sourceJSON.as[TypeDefinition].getOrElse(Null)")
        )
      )
    )
  )

  test("TypeDefinition as JSON") {
    println("TypeDefinition as JSON:")
    println(typeDefinitionTypeDefinition.asJson.spaces2)
  }

  test("TypeDefinition generates Scala code") {
    println("Generated Scala code:")
    println(Generator.generate(typeDefinitionTypeDefinition))
  }

  test("Minimal TypeDefinition (no factory)") {
    val minimal = TypeDefinition(
      _typeName = TypeName(
        _name = "Baz",
        _namePackage = Seq("foo", "bar")
      )
    )
    println("Minimal TypeDefinition (no factory):")
    println(Generator.generate(minimal))
  }

  test("TypeDefinition with globalElements only") {
    val withGlobals = TypeDefinition(
      _typeName = TypeName(
        _name = "Constants",
        _namePackage = Seq("foo", "bar")
      ),
      _globalElements = Seq(
        Fixed("MAX_SIZE", "Int", "1024"),
        Fixed("DEFAULT_NAME", "String", "\"unnamed\"")
      )
    )
    println("TypeDefinition with globalElements only:")
    println(Generator.generate(withGlobals))
  }

  test("TypeElement hierarchy as JSON") {
    val types: Seq[TypeDefinition] = Seq(
      TypeElement.typeDefinition,
      BodyElement.typeDefinition,
      Fixed.typeDefinition,
      Mutable.typeDefinition,
      Dynamic.typeDefinition,
      Parameter.typeDefinition,
      Monadic.typeDefinition,
      Pattern.typeDefinition,
      Action.typeDefinition,
      Condition.typeDefinition,
      Variable.typeDefinition,
      Factory.typeDefinition
    )
    println("TypeElement hierarchy as JSON:")
    types.foreach(td => println(td.asJson.spaces2))
  }

  test("Multi-type generation: TypeElement hierarchy") {
    val types: Seq[TypeDefinition] = Seq(
      TypeElement.typeDefinition,
      BodyElement.typeDefinition,
      Fixed.typeDefinition,
      Mutable.typeDefinition,
      Dynamic.typeDefinition,
      Parameter.typeDefinition,
      Monadic.typeDefinition,
      Pattern.typeDefinition,
      Action.typeDefinition,
      Condition.typeDefinition,
      Variable.typeDefinition,
      Factory.typeDefinition
    )
    val output = Generator.generate(types)
    println("Multi-type generation (TypeElement hierarchy):")
    println(output)

    // TypeElement should appear before BodyElement (parent before child)
    assert(output.indexOf("sealed trait TypeElement") < output.indexOf("sealed trait BodyElement"),
      "TypeElement should appear before BodyElement")
    // BodyElement should appear before Fixed (parent before child)
    assert(output.indexOf("sealed trait BodyElement") < output.indexOf("trait Fixed"),
      "BodyElement should appear before Fixed")
    // sealed keywords present on types that have modules
    assert(output.contains("sealed trait TypeElement"), "TypeElement should be sealed")
    assert(output.contains("sealed trait BodyElement"), "BodyElement should be sealed")
  }

  test("Multi-type generation: simple parent-child (Animal/Dog)") {
    val animalTd = TypeDefinition(
      _typeName = TypeName("Animal", _namePackage = Seq("test", "zoo")),
      _modules = Seq(TypeName("Dog", _namePackage = Seq("test", "zoo"))),
      _elements = Seq(Fixed("name", "String"))
    )
    val dogTd = TypeDefinition(
      _typeName = TypeName("Dog", _namePackage = Seq("test", "zoo")),
      _derivation = Seq(TypeName("Animal", _namePackage = Seq("test", "zoo"))),
      _factory = Factory("Dog", _parameters = Seq(
        Parameter("name", "String", "\"Fido\"")
      ))
    )
    // Pass in reversed order to verify ordering
    val output = Generator.generate(Seq(dogTd, animalTd))
    println("Multi-type generation (Animal/Dog):")
    println(output)

    // Animal should appear before Dog despite reversed input
    assert(output.indexOf("sealed trait Animal") < output.indexOf("trait Dog"),
      "Animal should appear before Dog")
    // Dog companion should have apply method
    assert(output.contains("def apply"), "Dog companion should have apply method")
    // Single package declaration
    assert(output.contains("package test.zoo"), "Should have package declaration")
  }
}
