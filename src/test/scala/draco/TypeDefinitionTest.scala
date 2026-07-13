package draco

import io.circe.Json
import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TypeDefinitionTest extends AnyFunSuite with PersistentTestLog {

  // TypeDefinition describing itself
  val typeDefinitionTypeDefinition: TypeDefinition = TypeDefinition(
    _typeName = TypeName(
      _name = "TypeDefinition",
      _namePackage = Seq("draco")
    ),
    _dracoAspect = DracoAspect(
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
        _valueType = "draco.TypeDefinition",
        _parameters = Seq(
          Parameter("typeName", "TypeName", Json.Null),
          Parameter("modules", "Seq[TypeName]", Json.fromString("Seq.empty")),
          Parameter("derivation", "Seq[TypeName]", Json.fromString("Seq.empty")),
          Parameter("elements", "Seq[TypeElement]", Json.fromString("Seq.empty")),
          Parameter("factory", "Factory", Json.fromString("Factory.Null")),
          Parameter("globalElements", "Seq[BodyElement]", Json.fromString("Seq.empty"))
        ),
        _body = Seq(
          Fixed("typeName", "TypeName", Json.fromString("_typeName")),
          Fixed("modules", "Seq[TypeName]", Json.fromString("_modules")),
          Fixed("derivation", "Seq[TypeName]", Json.fromString("_derivation")),
          Fixed("elements", "Seq[TypeElement]", Json.fromString("_elements")),
          Fixed("factory", "Factory", Json.fromString("_factory")),
          Fixed("globalElements", "Seq[BodyElement]", Json.fromString("_globalElements"))
        )
      ),
      _globalElements = Seq(
        Dynamic(
          "load",
          "TypeDefinition",
          Seq(Parameter("typeName", "TypeName", Json.Null)),
          Seq(
            Fixed("sourceContent", "SourceContent", Json.fromString("SourceContent(Generator.main.sourceRoot, typeName.resourcePath)")),
            Fixed("sourceJSON", "Json", Json.fromString("parser.parse(sourceContent.sourceString).getOrElse(TypeDefinition(typeName).asJson)")),
            Fixed("result", "TypeDefinition", Json.fromString("sourceJSON.as[TypeDefinition].getOrElse(Null)"))
          )
        )
      )
    )
  )

  test("TypeDefinition as JSON") {
    log.info("TypeDefinition as JSON:")
    log.info(typeDefinitionTypeDefinition.asJson.spaces2)
  }

  test("TypeDefinition generates Scala code") {
    log.info("Generated Scala code:")
    log.info(Generator.generate(typeDefinitionTypeDefinition))
  }

  test("Minimal TypeDefinition (no factory)") {
    val minimal = TypeDefinition(
      _typeName = TypeName(
        _name = "Baz",
        _namePackage = Seq("foo", "bar")
      )
    )
    log.info("Minimal TypeDefinition (no factory):")
    log.info(Generator.generate(minimal))
  }

  test("TypeDefinition with globalElements only") {
    val withGlobals = TypeDefinition(
      _typeName = TypeName(
        _name = "Constants",
        _namePackage = Seq("foo", "bar")
      ),
      _dracoAspect = DracoAspect(
        _globalElements = Seq(
          Fixed("MAX_SIZE", "Int", Json.fromString("1024")),
          Fixed("DEFAULT_NAME", "String", Json.fromString("\"unnamed\""))
        )
      )
    )
    log.info("TypeDefinition with globalElements only:")
    log.info(Generator.generate(withGlobals))
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
    log.info("TypeElement hierarchy as JSON:")
    types.foreach(td => log.info(td.asJson.spaces2))
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
    log.info("Multi-type generation (TypeElement hierarchy):")
    log.info(output)

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
      _dracoAspect = DracoAspect(
        _modules = Seq(TypeName("Dog", _namePackage = Seq("test", "zoo"))),
        _elements = Seq(Fixed("name", "String"))
      )
    )
    val dogTd = TypeDefinition(
      _typeName = TypeName("Dog", _namePackage = Seq("test", "zoo")),
      _dracoAspect = DracoAspect(
        _derivation = Seq(TypeName("Animal", _namePackage = Seq("test", "zoo"))),
        _factory = Factory("Dog", _parameters = Seq(
          Parameter("name", "String", Json.fromString("\"Fido\""))
        ))
      )
    )
    // Pass in reversed order to verify ordering
    val output = Generator.generate(Seq(dogTd, animalTd))
    log.info("Multi-type generation (Animal/Dog):")
    log.info(output)

    // Animal should appear before Dog despite reversed input
    assert(output.indexOf("sealed trait Animal") < output.indexOf("trait Dog"),
      "Animal should appear before Dog")
    // Dog companion should have apply method
    assert(output.contains("def apply"), "Dog companion should have apply method")
    // Single package declaration
    assert(output.contains("package test.zoo"), "Should have package declaration")
  }

  test("CodecAspect.discriminator overrides the default \"kind\" tag in the discriminated codec") {
    // Same Animal/Dog discriminated union as above, but the parent authors a
    // CodecAspect.discriminator. The generator must emit that tag as the wire key
    // in both encoder and decoder, and leave no default "kind" behind.
    val animalTd = TypeDefinition(
      _typeName = TypeName("Animal", _namePackage = Seq("test", "zoo")),
      _dracoAspect = DracoAspect(
        _modules = Seq(TypeName("Dog", _namePackage = Seq("test", "zoo"))),
        _elements = Seq(Fixed("name", "String"))
      ),
      _codecAspect = CodecAspect("species")
    )
    val dogTd = TypeDefinition(
      _typeName = TypeName("Dog", _namePackage = Seq("test", "zoo")),
      _dracoAspect = DracoAspect(
        _derivation = Seq(TypeName("Animal", _namePackage = Seq("test", "zoo"))),
        _factory = Factory("Dog", _parameters = Seq(
          Parameter("name", "String", Json.fromString("\"Fido\""))
        ))
      )
    )
    val output = Generator.generate(Seq(dogTd, animalTd))
    log.info("Animal/Dog with authored discriminator:\n" + output)

    assert(output.contains("\"species\" -> Json.fromString"),
      "encoder should emit the authored discriminator as the wire key")
    assert(output.contains("cursor.downField(\"species\").as[String]"),
      "decoder should read the authored discriminator field")
    assert(!output.contains("\"kind\""),
      "no default \"kind\" tag should remain once a discriminator is authored")
  }

  test("subtype-only fields round-trip through the TypeElement codec (Pattern, Action)") {
    // The discriminated TypeElement encoder must emit fields that live only on a
    // subtype (Pattern.variables/conditions, Action.variables/values), not just the
    // parent-trait fields. Before the fix these were dropped on encode.
    val pattern = Pattern(
      _variables = Seq(Variable("accumulator", "Accumulator"), Variable("i", "Integer"))
    )
    val patternJson = pattern.asJson
    assert(patternJson.spaces2.contains("\"variables\""),
      s"encoded Pattern must carry its variables, got:\n${patternJson.spaces2}")
    val patternBack = io.circe.Decoder[Pattern].decodeJson(patternJson).getOrElse(Pattern.Null)
    assert(patternBack.variables.map(_.name) == Seq("accumulator", "i"),
      s"Pattern variables lost on round-trip: ${patternBack.variables.map(_.name)}")

    val action = Action(
      _variables = Seq(Variable("i", "Integer")),
      _body = Seq(Monadic(Json.fromString("println(i)")))
    )
    val actionJson = action.asJson
    assert(actionJson.spaces2.contains("\"variables\""),
      s"encoded Action must carry its variables, got:\n${actionJson.spaces2}")
    val actionBack = io.circe.Decoder[Action].decodeJson(actionJson).getOrElse(Action.Null)
    assert(actionBack.variables.map(_.name) == Seq("i"),
      s"Action variables lost on round-trip: ${actionBack.variables.map(_.name)}")
    log.info(s"Pattern round-trip JSON:\n${patternJson.spaces2}")
  }
}
