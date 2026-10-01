package draco.service

import draco._
import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

/** The main-corpus gates do not discover draco definitions in src/mods. */
class ServiceTest extends AnyFunSuite {
  private val resource = Paths.get("src/mods/resources/draco/service/Service")
  private def read(suffix: String): String =
    new String(Files.readAllBytes(Paths.get(resource.toString + suffix)), UTF_8)
  private def definition: TypeDefinition =
    io.circe.parser.parse(read(".json")).flatMap(_.as[TypeDefinition])
      .fold(error => fail(error.toString), identity)

  test("Service Drake and JSON are canonical mutual projections") {
    val source = read(".drake")
    val parsed = Drake.parse(source)
    assert(parsed.asJson == definition.asJson)
    assert(Drake.emit(definition) == source)
    assert(Drake.emit(parsed) == source)
  }

  test("Service Scala is generated from its staged definition") {
    val scalaPath = Paths.get("src/mods/scala/draco/service/Service.scala")
    assert(DracoGenerator.generate(definition) == new String(Files.readAllBytes(scalaPath), UTF_8))
  }

  test("the compiled companion loads the self-declaring Service domain") {
    val expected = TypeName("Service", Seq("draco", "service"))
    val td = Service.typeDefinition
    assert(td.typeName == expected)
    assert(td.domainAspect.typeName == expected)
    assert(Service.domainType.typeDefinition.typeName == expected)
    assert(Service.elementTypeNames.isEmpty)
    assert(Service.domainType.typeDictionary.isEmpty)
    assert(ActorAspect.isEmpty(td.actorAspect))
    assert(RuleAspect.isEmpty(td.ruleAspect))
    assert(CodecAspect.isEmpty(td.codecAspect))
  }

  test("DomainBuilder validates Service and composes it with an existing domain") {
    val service = DomainBuilder.define("Service", Seq("draco", "service"))
    val base = DomainBuilder.define("Base", Seq("draco", "base"))
    assert(DomainBuilder.validate(service).isEmpty)
    assert(service.typeDefinition.domainAspect.typeName == service.typeDefinition.typeName)
    val dictionary = DomainBuilder.dictionary(service, base)
    assert(dictionary.size == 2)
    assert(dictionary.get(service).exists(_.isEmpty))
    assert(dictionary.get(base).exists(_.nonEmpty))
    assert(dictionary.keys.exists(_.typeDefinition.typeName == Service.typeDefinition.typeName))
  }

  test("DomainBuilder generates the real Service domain without a stub fallback") {
    val generated = DomainBuilder.generate("Service", Seq("draco", "service"))
    assert(generated.keySet == Set(definition.typeName))
    assert(generated(definition.typeName) == DracoGenerator.generate(definition))
    assert(!generated(definition.typeName).contains("stub skeleton"))
  }
}
