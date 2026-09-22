package draco.draketarget

import draco._
import draco.gendrake.DomainAspectOf
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

class DomainAspectTest extends AnyFunSuite with PersistentTestLog {
  private def name(value: String, parameters: Json*): TypeName =
    TypeName(value, Seq("example"), parameters)

  private def render(td: TypeDefinition): String =
    DomainAspectOf(td, Drake.typeParameterSurface(_)).value

  private def section(source: String): String = {
    val lines = source.linesIterator.dropWhile(l => l != "domain" && !l.startsWith("domain ")).toList
    (lines.headOption.toSeq ++ lines.drop(1).takeWhile(_.startsWith(" "))).mkString("\n")
  }

  test("the target renders the complete domain section in canonical order") {
    val text = DomainAspectText("example Domain", "example Parent", "example Source",
      "example Target", Seq("Second", "First")).value
    assert(text == "domain example Domain\n  super example Parent\n  source example Source\n  target example Target\n  types [\n    Second\n    First\n  ]")
    assert(DomainAspectText("example Domain").value == "domain example Domain")
    assert(DomainAspectText("example Domain", _sourceReference = "example Source").value ==
      "domain example Domain\n  source example Source")
    assert(DomainAspectText("example Domain", _targetReference = "example Target").value ==
      "domain example Domain\n  target example Target")
  }

  test("self and containing-domain references retain their meaning and super-domain ownership") {
    val domain = name("Domain")
    val parent = name("Parent")
    val self = TypeDefinition(domain,
      _dracoAspect = DracoAspect(_superDomain = parent),
      _domainAspect = DomainAspect(domain, Seq("Member")))
    val member = TypeDefinition(name("Member"), _domainAspect = DomainAspect(domain))
    assert(render(self) == "domain example Domain\n  super example Parent\n  types [\n    Member\n  ]")
    assert(render(member) == "domain example Domain")
    val decoded = Drake.parse(Drake.emit(self))
    assert(decoded.typeName == decoded.domainAspect.typeName)
    assert(decoded.dracoAspect.superDomain == parent)
    val decodedMember = Drake.parse(Drake.emit(member))
    assert(decodedMember.typeName != decodedMember.domainAspect.typeName)
    assert(decodedMember.domainAspect.typeName == domain)
  }

  test("all four reference positions retain nested type parameters through the live emitter") {
    val sequence = Json.obj("[]" -> Json.arr(Json.fromString("draco.drake.Text")))
    val function = Json.obj("->" -> Json.arr(Json.fromString("A"), Json.fromString("B")))
    val td = TypeDefinition(name("Projection"),
      _dracoAspect = DracoAspect(_superDomain = name("Parent", function)),
      _domainAspect = DomainAspect(name("Domain", sequence), Seq("Member"),
        name("Source", Json.fromString("S")), name("Target", sequence)))
    val expected = "domain example Domain([draco.drake.Text])\n  super example Parent(A -> B)\n  source example Source(S)\n  target example Target([draco.drake.Text])\n  types [\n    Member\n  ]"
    assert(render(td) == expected)
    assert(section(Drake.emit(td)) == expected)
    val decoded = Drake.parse(Drake.emit(td))
    assert(decoded.domainAspect.typeName == td.domainAspect.typeName)
    assert(decoded.domainAspect.source == td.domainAspect.source)
    assert(decoded.domainAspect.target == td.domainAspect.target)
    assert(decoded.domainAspect.elementTypeNames == td.domainAspect.elementTypeNames)
    assert(decoded.dracoAspect.superDomain == td.dracoAspect.superDomain)
  }

  test("nameless anchors, empty packages and missing domain references preserve existing behavior") {
    assert(render(TypeDefinition(TypeName.Null)) == "domain")
    assert(Drake.emit(TypeDefinition(TypeName.Null)) == "domain\n")
    val unqualified = TypeDefinition(name("Member"), _domainAspect = DomainAspect(TypeName("Container")))
    assert(render(unqualified) == "domain Container")
    val missing = TypeDefinition(name("Stub"), _dracoAspect = DracoAspect(_superDomain = name("Parent")))
    assert(render(missing).isEmpty)
    assert(section(Drake.emit(missing)).isEmpty)
    assert(DomainAspectText(throw new AssertionError("nameless reference evaluated"),
      _nameless = true).value == "domain")
  }

  test("complete domain sections reproduce the corpus without excluding parameterized references") {
    val root = Paths.get("src/main/resources/draco")
    val paths = Using.resource(Files.walk(root)) { stream =>
      stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json")).toList.sorted
    }
    assert(paths.nonEmpty)
    var parameterized = 0
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      if (td.domainAspect.typeName.typeParameters.nonEmpty) parameterized += 1
      assert(render(td) == section(new String(Files.readAllBytes(authored), UTF_8)), s"domain section differs: $path")
    }
    assert(parameterized > 0, "the parameterized-domain regression fixture disappeared")
    console.info(s"DomainAspect over the corpus: ${paths.size} complete sections reproduced, $parameterized parameterized included, 0 skipped")
  }

  test("the running generator rule emits the complete definition-backed domain section") {
    val td = TypeDefinition(name("Projection"),
      _dracoAspect = DracoAspect(_superDomain = name("Parent")),
      _domainAspect = DomainAspect(name("Domain", Json.fromString("T")), Seq("Member"), name("Source"), name("Target")))
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("domainAspect")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == "type Projection\ndomain example Domain(T)\n  super example Parent\n  source example Source\n  target example Target\n  types [\n    Member\n  ]\n")
  }
}
