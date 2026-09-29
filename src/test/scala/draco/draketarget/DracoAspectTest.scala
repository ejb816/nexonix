package draco.draketarget

import draco._
import io.circe.Json
import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

class DracoAspectTest extends AnyFunSuite with PersistentTestLog {
  private def section(source: String): String =
    source.linesIterator.takeWhile(l => l.startsWith("type ") || l.startsWith(" ")).mkString("\n")

  private def render(td: TypeDefinition): String = Drake.dracoAspectText(td)

  test("the target orders all type sections and preserves supplied element indentation") {
    val text = DracoAspectText("Example(T)", Seq("Parent(T)"), Seq("Second", "First"),
      "other Host", Seq("    fix x T"), true, "ActorType(M)", Seq("      par m M"),
      Seq("      fix value T x"), Seq("    fix zero T z")).value
    assert(text == "type Example(T) from Parent(T)\n  modules [\n    Second\n    First\n  ]\n  extensible other Host\n  elements\n    fix x T\n  factory ActorType(M)\n    parameters\n      par m M\n    body\n      fix value T x\n  globals\n    fix zero T z")
    assert(DracoAspectText("Empty").value == "type Empty")
    assert(DracoAspectText("Empty", _factoryPresent = true).value == "type Empty\n  factory")
  }

  test("root elision preserves foreign parents and explicit roots beside named parents") {
    val root = TypeName("DracoType", Seq("draco")).asJson
    val local = TypeName("Parent", Seq("example")).asJson
    val foreign = Json.obj("{}" -> Json.arr(Json.fromString("K"), Json.fromString("V")))
    def td(parents: Json*): TypeDefinition = TypeDefinition(TypeName("Child", Seq("example")),
      _dracoAspect = DracoAspect(_derivation = parents))
    assert(render(td()) == "type Child")
    assert(render(td(root)) == "type Child")
    assert(render(td(root, foreign)) == "type Child from {K, V}")
    assert(render(td(local, root)) == "type Child from Parent draco DracoType")
    assert(render(td(TypeName("DracoType", Seq("other")).asJson)) == "type Child from other DracoType")
    assert(render(td(TypeName("Parent").asJson)) == "type Child from Parent")
  }

  test("header, derivation, modules and extensible references retain nested parameters") {
    val parameter = Json.obj("[]" -> Json.arr(Json.fromString("draco.drake.Text")))
    val local = TypeName("Parent", Seq("example"), Seq(parameter))
    val remote = TypeName("Peer", Seq("other"), Seq(Json.fromString("T")))
    val td = TypeDefinition(TypeName("Child", Seq("example"), Seq(parameter)),
      _dracoAspect = DracoAspect(_derivation = Seq(local.asJson),
        _modules = Seq(remote, local), _extensible = local),
      _domainAspect = DomainAspect(TypeName("Domain", Seq("example"))))
    val expected = "type Child([draco.drake.Text]) from Parent([draco.drake.Text])\n  modules [\n    other Peer(T)\n    Parent([draco.drake.Text])\n  ]\n  extensible example Parent([draco.drake.Text])"
    assert(render(td) == expected)
    assert(section(Drake.emit(td)) == expected)
    val parsed = Drake.parse(Drake.emit(td))
    assert(parsed.typeName == td.typeName)
    assert(parsed.dracoAspect.modules == td.dracoAspect.modules)
    assert(parsed.dracoAspect.extensible == td.dracoAspect.extensible)
  }

  test("factory result elision and nested bodies round-trip without losing sections") {
    val source = """type Example(T)
                   |  elements
                   |    fix x T
                   |  factory
                   |    parameters
                   |      par x T
                   |    body
                   |      fix x T _x
                   |      dyn identity T [
                   |        parameters
                   |          now par y T
                   |        body
                   |          fix value T y
                   |      ]
                   |  globals
                   |    fix label draco.drake.Text "example"
                   |domain example Domain
                   |""".stripMargin
    val td = Drake.parse(source)
    assert(render(td) == section(source))
    assert(Drake.emit(td) == source)
    assert(Drake.emit(Drake.parse(Drake.emit(td))) == source)
    val explicit = source.replace("  factory\n", "  factory ActorType(T)\n")
    assert(Drake.emit(Drake.parse(explicit)) == explicit)
    val legacy = TypeDefinition(td.typeName, _dracoAspect = DracoAspect(
      _factory = Factory(_valueType = Json.fromString("Example[T]"))))
    assert(render(legacy) == "type Example(T)\n  factory")
  }

  test("nameless anchors and absent factories do not force unused factory rendering") {
    assert(render(TypeDefinition(TypeName.Null)).isEmpty)
    assert(Drake.emit(TypeDefinition(TypeName.Null)) == "domain\n")
    assert(render(TypeDefinition(TypeName("Stub"))) == "type Stub")
    assert(DracoAspectText("Stub", _factoryResult = throw new AssertionError("result evaluated"),
      _factoryParameters = throw new AssertionError("parameters evaluated"),
      _factoryBody = throw new AssertionError("body evaluated")).value == "type Stub")
    val noFactory = TypeDefinition(TypeName("Stub"), _dracoAspect = DracoAspect(
      _factory = Factory(_valueType = Json.Null, _body = Seq(Fixed(_name = "unused", _valueType = Json.fromString("T"))))))
    val rendered = draco.gendrake.DracoAspectOf(noFactory, Drake.typeParameterSurface(_),
      _ => throw new AssertionError("form evaluated"), Drake.typeSurface(_),
      (_, _) => throw new AssertionError("element evaluated")).value
    assert(rendered == "type Stub")
  }

  test("complete Draco sections reproduce the corpus including authored-ahead baselines") {
    val paths = Using.resource(Files.walk(Paths.get("src/main/resources/draco"))) { stream =>
      stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json")).toList.sorted
    }
    assert(paths.nonEmpty)
    // These surfaces predate the migration and deliberately differ from their JSON.
    // Freeze the old emitter's type section instead of excluding either definition.
    val authoredAhead = Set("ActorAspect.json", "BodyElement.json")
      .map(name => Paths.get("src/main/resources/draco").resolve(name))
    assert(authoredAhead.subsetOf(paths.toSet))
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      val expected = if (authoredAhead(path))
        Paths.get("src/test/resources/draco/draketarget/draco-aspect-baseline").resolve(authored.getFileName)
      else authored
      assert(render(td) == section(new String(Files.readAllBytes(expected), UTF_8)), s"Draco section differs: $path")
    }
    console.info(s"DracoAspect over the corpus: ${paths.size} complete sections reproduced, 2 frozen authored-ahead baselines, 0 skipped")
  }

  test("the running generator rule uses the definition-backed Draco section") {
    val td = Drake.parse("type Example\n  factory\n  globals\n    fix label draco.drake.Text \"example\"\ndomain example Domain\n")
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("dracoAspect")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == "type Example\n  factory\n  globals\n    fix label draco.drake.Text \"example\"\ndomain example Domain\n")
  }
}
