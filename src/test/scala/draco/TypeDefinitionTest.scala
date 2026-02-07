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
      Fixed("typeGlobals", "Seq[BodyElement]"),
      Fixed("rules", "Seq[TypeName]")
    ),
    _factory = Factory(
      _fullName = "draco.TypeDefinition",
      _parameters = Seq(
        Parameter("typeName", "TypeName", ""),
        Parameter("modules", "Seq[TypeName]", "Seq.empty"),
        Parameter("derivation", "Seq[TypeName]", "Seq.empty"),
        Parameter("elements", "Seq[TypeElement]", "Seq.empty"),
        Parameter("factory", "Factory", "Factory.Null"),
        Parameter("typeGlobals", "Seq[BodyElement]", "Seq.empty"),
        Parameter("rules", "Seq[TypeName]", "Seq.empty")
      ),
      _body = Seq(
        Fixed("typeName", "TypeName", "_typeName"),
        Fixed("modules", "Seq[TypeName]", "_modules"),
        Fixed("derivation", "Seq[TypeName]", "_derivation"),
        Fixed("elements", "Seq[TypeElement]", "_elements"),
        Fixed("factory", "Factory", "_factory"),
        Fixed("typeGlobals", "Seq[BodyElement]", "_typeGlobals"),
        Fixed("rules", "Seq[TypeName]", "_rules")
      )
    ),
    _typeGlobals = Seq(
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
    ),
    _rules = Seq.empty
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

  test("TypeDefinition with typeGlobals only") {
    val withGlobals = TypeDefinition(
      _typeName = TypeName(
        _name = "Constants",
        _namePackage = Seq("foo", "bar")
      ),
      _typeGlobals = Seq(
        Fixed("MAX_SIZE", "Int", "1024"),
        Fixed("DEFAULT_NAME", "String", "\"unnamed\"")
      )
    )
    println("TypeDefinition with typeGlobals only:")
    println(Generator.generate(withGlobals))
  }
}
