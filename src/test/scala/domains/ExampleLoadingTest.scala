package domains

import draco._
import org.scalatest.funsuite.AnyFunSuite

class ExampleLoadingTest extends AnyFunSuite {
  private def examples(ontology: DomainOntology): Seq[TypeDefinition] =
    ontology.kvMap.keys.toSeq.filter(_.typeDefinition.typeName.namePackage.headOption.contains("domains"))
      .flatMap(d => d.typeDefinition +: d.typeDictionary.elementTypes).sortBy(_.typeName.namePath)
  private def encoded(definitions: Seq[TypeDefinition]) = definitions.map(TypeDefinition.encoder(_))

  test("explicit two-stage loading resolves all 48 example definitions independently of declaration order") {
    val identities = DomainBuilder.identities(ExampleScope.declarations)
    val scope = new NameResolution(identities, ExampleScope.bindings)
    val loaded = DomainBuilder.load(ExampleScope.declarations, ExampleScope.bindings)
    val reversed = DomainBuilder.load(ExampleScope.declarations.reverse, ExampleScope.bindings)
    val actual = examples(loaded)
    val authored = ExampleScope.domains.flatMap { name =>
      val domain = DomainBuilder.define(name.name, name.namePackage)
      domain.typeDefinition +: domain.typeDictionary.elementTypes
    }.sortBy(_.typeName.namePath)
    assert(actual.size == 48 && actual.map(_.typeName.namePath).distinct.size == 48)
    assert(actual.forall(td => !DomainBuilder.isStub(td)))
    assert(encoded(actual) == encoded(authored.map(scope.resolve)))
    assert(encoded(actual) == encoded(examples(reversed)))
    assert(!scope.ontological(ExampleScope.sink.namePath), "the caller's sink is a binding, not a domain member")
    assert(!draco.scalatarget.HostBindings.vocabulary.values.exists(_ == ExampleScope.sink))
    assert(!scope.ontological(ExampleScope.geodesy.namePath), "the geodesy helper is a caller binding")
    assert(!draco.scalatarget.HostBindings.vocabulary.values.exists(_ == ExampleScope.geodesy))
    val input = actual.find(_.typeName.namePath == "domains.aerial.Input").get
    val cursor = input.actorAspect.fold(fail("Input has no actor role"), _.message.body.find(_.name == "cursor").get)
    assert(TypeForm.text(cursor.valueType.json) == "io.circe.HCursor")
  }

  test("example loading requires the caller sink, geodesy helper and target cursor bindings explicitly") {
    val noSink = intercept[IllegalArgumentException] {
      DomainBuilder.load(ExampleScope.declarations, ExampleScope.bindings - ExampleScope.sink.namePath)
    }
    assert(noSink.getMessage.contains("domains.aerial.AerialSink"), noSink.getMessage)
    val noGeodesy = intercept[IllegalArgumentException] {
      DomainBuilder.load(ExampleScope.declarations, ExampleScope.bindings - ExampleScope.geodesy.name)
    }
    assert(noGeodesy.getMessage.contains("Geodesy"), noGeodesy.getMessage)
    val noCursor = intercept[IllegalArgumentException] {
      DomainBuilder.load(ExampleScope.declarations, ExampleScope.bindings - "io.circe.HCursor")
    }
    assert(noCursor.getMessage.contains("io.circe.HCursor"), noCursor.getMessage)
  }
}
