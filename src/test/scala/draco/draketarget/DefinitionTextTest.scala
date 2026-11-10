package draco.draketarget

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** The ASSEMBLY of the drake surface is definition-backed (Dev, 2026-11-10; GitHub #11):
 *  `draketarget.DefinitionText` owns the order of the six sections — type, `uses`, domain, rule,
 *  actor, codec — and their joining, `gendrake.DefinitionOf` composes the six section transforms
 *  from a definition, and `Drake.emit` is its three guards and one call. After it the emitter
 *  holds no layout at all: what stays hand-written is the guards and the renderers the
 *  transforms receive as explicit dependencies.
 */
class DefinitionTextTest extends AnyFunSuite with PersistentTestLog {

  private val dracon = TypeName("Dracon", Seq("draco", "format", "dracon"))
  private val text   = TypeName("Text", Seq("draco", "drake"))
  private def carrier: TypeDefinition = TypeDefinition(TypeName("Carrier", Seq("example")),
    _uses = Seq(text, dracon), _codecAspect = CodecAspect(_payload = Seq("value", "valueType")))

  test("the target joins the sections in their order, drops the empty ones and ends with one line break") {
    assert(DefinitionText("type A", "uses [\n  x Y\n]", "domain x X", "rule", "actor", "codec\n  payload [a]").value ==
      "type A\nuses [\n  x Y\n]\ndomain x X\nrule\nactor\ncodec\n  payload [a]\n")
    assert(DefinitionText("type A", _domainSection = "domain x X").value == "type A\ndomain x X\n")
    assert(DefinitionText(_ruleSection = "rule", _typeSection = "type A").value == "type A\nrule\n")
    assert(DefinitionText().value == "\n")
  }

  test("a section argument is read once, and only when the surface is read") {
    var reads = 0
    val surface = DefinitionText({ reads += 1; "type A" }, _codecSection = { reads += 1; "codec" })
    assert(reads == 0)
    assert(surface.value == "type A\ncodec\n" && surface.value == "type A\ncodec\n")
    assert(reads == 2)
  }

  test("a definition maps onto the six section transforms in order through the live emitter and reads back; the guards stay") {
    val td = carrier
    val sections = Seq(Drake.dracoAspectText(td), Drake.usesText(td),
      draco.gendrake.DomainAspectOf(td, (_: draco.format.dracon.Dracon) => "").value,
      Drake.ruleAspectText(td), Drake.actorAspectText(td), Drake.codecAspectText(td))
    assert(Drake.definitionText(td) == sections.filter(_.nonEmpty).mkString("", "\n", "\n"))
    assert(Drake.emit(td) == Drake.definitionText(td))
    assert(Drake.emit(td) == "type Carrier\nuses [\n  draco drake Text\n  draco format dracon Dracon\n]\ncodec\n  payload [value, valueType]\n")
    val back = Drake.parse(Drake.emit(td))
    assert(back.typeName.name == td.typeName.name && back.uses == td.uses && back.codecAspect.payload == td.codecAspect.payload)
    assert(Drake.emit(TypeDefinition(TypeName("Plain", Seq("example")))) == "type Plain\n")

    intercept[IllegalArgumentException](Drake.emit(TypeDefinition(TypeName("Twice", Seq("example")), _codecAspect = CodecAspect(_payload = Seq("value", "value")))))
    intercept[IllegalArgumentException](Drake.emit(TypeDefinition(TypeName("Odd", Seq("example")), _codecAspect = CodecAspect(_payload = Seq("not a field")))))
    intercept[RuntimeException](Drake.emit(TypeDefinition(TypeName("Tagged", Seq("example")), _codecAspect = CodecAspect(_discriminator = "tag"))))
  }

  test("every surface in the main and mods corpus is reproduced whole") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val paths = roots.flatMap { root =>
      Using.resource(Files.walk(Paths.get(root))) { stream =>
        stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json") && p.getFileName.toString != ".json").toList.sorted
      }
    }
    assert(paths.size >= 194, s"the corpus walk found only ${paths.size} definitions")
    var sections = 0
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      val source = new String(Files.readAllBytes(authored), UTF_8)
      assert(Drake.definitionText(td) == source, s"surface differs: $path")
      sections += Seq(Drake.dracoAspectText(td), Drake.usesText(td), Drake.ruleAspectText(td),
        Drake.actorAspectText(td), Drake.codecAspectText(td)).count(_.nonEmpty) + (if (source.linesIterator.exists(l => l == "domain" || l.startsWith("domain "))) 1 else 0)
    }
    assert(sections >= paths.size, s"only $sections sections over ${paths.size} definitions")
    console.info(s"Definition over the corpus: ${paths.size} surfaces reproduced whole from $sections sections, 0 skipped")
  }

  test("the running generator rule emits the assembled surface") {
    val td = carrier
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("definitionText")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == Drake.definitionText(td))
  }
}
