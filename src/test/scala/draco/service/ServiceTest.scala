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
    assert(Service.elementTypeNames == Seq("ServiceConfiguration", "TextOutput"))
    assert(Service.domainType.typeDictionary.size == 2)
    assert(ActorAspect.isEmpty(td.actorAspect))
    assert(RuleAspect.isEmpty(td.ruleAspect))
    assert(CodecAspect.isEmpty(td.codecAspect))
  }

  test("DomainBuilder validates Service and composes it with an existing domain in one ontology") {
    val service = DomainBuilder.define("Service", Seq("draco", "service"))
    val base = DomainBuilder.define("Base", Seq("draco", "base"))
    assert(DomainBuilder.validate(service).isEmpty)
    assert(service.typeDefinition.domainAspect.typeName == service.typeDefinition.typeName)
    val ontology = DomainBuilder.ontology(service, base)
    assert(ontology.size == 2)
    assert(ontology.get(service).exists(_.size == 2))
    assert(ontology.get(base).exists(_.nonEmpty))
    assert(ontology.keys.exists(_.typeDefinition.typeName == Service.typeDefinition.typeName))
    assert(ontology.defines(ServiceConfiguration.typeDefinition.typeName))
  }

  test("DomainBuilder generates the real Service domain without a stub fallback") {
    val generated = DomainBuilder.generate("Service", Seq("draco", "service"))
    assert(generated.keySet == Set(definition.typeName, ServiceConfiguration.typeDefinition.typeName,
      TextOutput.typeDefinition.typeName))
    assert(generated(definition.typeName) == DracoGenerator.generate(definition))
    assert(!generated(definition.typeName).contains("stub skeleton"))
  }

  test("TextOutput has canonical Drake/JSON and generated Scala projections") {
    val base = Paths.get("src/mods/resources/draco/service/TextOutput")
    def content(suffix: String) = new String(Files.readAllBytes(Paths.get(base.toString + suffix)), UTF_8)
    val td = io.circe.parser.parse(content(".json")).flatMap(_.as[TypeDefinition]).toOption.get
    assert(Drake.parse(content(".drake")).asJson == td.asJson)
    assert(Drake.emit(td) == content(".drake"))
    assert(DracoGenerator.generate(td) == new String(Files.readAllBytes(
      Paths.get("src/mods/scala/draco/service/TextOutput.scala")), UTF_8))
    assert(TextOutput.typeDefinition.typeName == td.typeName)
  }

  test("TextOutput passes text and reports acceptance without changing the result") {
    val received = scala.collection.mutable.ArrayBuffer.empty[String]
    val sink = TextOutput(text => { received += text; text.nonEmpty })
    assert(sink.send("hello"))
    assert(!sink.send(""))
    assert(received.toSeq == Seq("hello", ""))
  }

  test("ServiceConfiguration has canonical Drake/JSON and generated Scala projections") {
    val base = Paths.get("src/mods/resources/draco/service/ServiceConfiguration")
    def content(suffix: String) = new String(Files.readAllBytes(Paths.get(base.toString + suffix)), UTF_8)
    val td = io.circe.parser.parse(content(".json")).flatMap(_.as[TypeDefinition]).toOption.get
    assert(Drake.parse(content(".drake")).asJson == td.asJson)
    assert(Drake.emit(td) == content(".drake"))
    assert(DracoGenerator.generate(td) == new String(Files.readAllBytes(
      Paths.get("src/mods/scala/draco/service/ServiceConfiguration.scala")), UTF_8))
    assert(ServiceConfiguration.typeDefinition.typeName == td.typeName)
    assert(td.domainAspect.typeName == Service.typeDefinition.typeName)
  }

  test("ServiceConfiguration contains an Assembly and round-trips it through JSON") {
    val entry = TypeName("Editor", Seq("draco", "dreams", "editor"))
    val assembly = Assembly(_members = Seq(entry), _entry = entry)
    val configuration = ServiceConfiguration(assembly)
    assert(configuration.assembly.entry == entry)
    assert(configuration.assembly.members == Seq(entry))
    assert(ServiceConfiguration().assembly.members.isEmpty, "the default configuration carries the Null assembly")
    val decoded = configuration.asJson.as[ServiceConfiguration].fold(error => fail(error.toString), identity)
    assert(decoded.assembly.entry == entry)
    assert(decoded.assembly.members == Seq(entry))
  }
}
