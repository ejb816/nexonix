package draco.draketarget

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** The resolution record and the codec section of the drake surface are definition-backed
 *  (Dev, 2026-11-10; GitHub #11): `draketarget.UsesText` / `gendrake.UsesOf` render `uses [ … ]`,
 *  `draketarget.CodecAspectText` / `gendrake.CodecAspectOf` the `codec` section's payload list,
 *  and `Drake.emit` delegates both — the fifth and sixth sections to cross, the last two. No
 *  section's layout is written in the emitter after them. The emitter keeps its guards: a
 *  duplicate or malformed payload field and a codec discriminator are refused before anything
 *  renders.
 */
class UsesAndCodecTest extends AnyFunSuite with PersistentTestLog {

  /** `uses [` through its closing bracket. */
  private def usesSection(source: String): String = {
    val lines = source.linesIterator.dropWhile(_ != "uses [").toList
    if (lines.isEmpty) "" else (lines.takeWhile(_ != "]") :+ "]").mkString("\n")
  }

  /** The `codec` line and everything indented under it. */
  private def codecSection(source: String): String = {
    val lines = source.linesIterator.dropWhile(_ != "codec").toList
    (lines.headOption.toSeq ++ lines.drop(1).takeWhile(_.startsWith(" "))).mkString("\n")
  }

  private val dracon = TypeName("Dracon", Seq("draco", "format", "dracon"))
  private val text   = TypeName("Text", Seq("draco", "drake"))
  private def carrier: TypeDefinition = TypeDefinition(TypeName("Carrier", Seq("example")),
    _uses = Seq(text, dracon), _codecAspect = CodecAspect(_payload = Seq("value", "valueType")))

  test("the target renders the record as a bracketed list, one identity per line, and nothing when there is none") {
    assert(UsesText(Seq("draco drake Text", "draco format dracon Dracon")).value == "uses [\n  draco drake Text\n  draco format dracon Dracon\n]")
    assert(UsesText(Seq("draco TypeName")).value == "uses [\n  draco TypeName\n]")
    assert(UsesText().value == "")
  }

  test("the target renders the codec section's payload list, and nothing when there is none") {
    assert(CodecAspectText(Seq("value", "valueType")).value == "codec\n  payload [value, valueType]")
    assert(CodecAspectText(Seq("derivation")).value == "codec\n  payload [derivation]")
    assert(CodecAspectText().value == "")
  }

  test("a definition's record and payload map onto their sections through the live emitter and read back; the guards stay") {
    val td = carrier
    assert(Drake.usesText(td) == "uses [\n  draco drake Text\n  draco format dracon Dracon\n]")
    assert(Drake.codecAspectText(td) == "codec\n  payload [value, valueType]")
    val surface = Drake.emit(td)
    assert(usesSection(surface) == Drake.usesText(td) && codecSection(surface) == Drake.codecAspectText(td))
    assert(surface == "type Carrier\nuses [\n  draco drake Text\n  draco format dracon Dracon\n]\ncodec\n  payload [value, valueType]\n")
    val back = Drake.parse(surface)
    assert(back.uses == Seq(text, dracon) && back.codecAspect.payload == Seq("value", "valueType"))

    val plain = TypeDefinition(TypeName("Plain", Seq("example")))
    assert(Drake.usesText(plain).isEmpty && Drake.codecAspectText(plain).isEmpty)
    assert(Drake.emit(plain) == "type Plain\n")

    intercept[IllegalArgumentException](Drake.emit(TypeDefinition(TypeName("Twice", Seq("example")), _codecAspect = CodecAspect(_payload = Seq("value", "value")))))
    intercept[IllegalArgumentException](Drake.emit(TypeDefinition(TypeName("Odd", Seq("example")), _codecAspect = CodecAspect(_payload = Seq("not a field")))))
    intercept[RuntimeException](Drake.emit(TypeDefinition(TypeName("Tagged", Seq("example")), _codecAspect = CodecAspect(_discriminator = "tag"))))
  }

  test("every record and every codec section in the main and mods corpus is reproduced") {
    val roots = Seq("src/main/resources", "src/mods/resources")
    val paths = roots.flatMap { root =>
      Using.resource(Files.walk(Paths.get(root))) { stream =>
        stream.iterator.asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json") && p.getFileName.toString != ".json").toList.sorted
      }
    }
    assert(paths.size >= 192, s"the corpus walk found only ${paths.size} definitions")
    var records = 0
    var codecs  = 0
    paths.foreach { path =>
      val td = io.circe.parser.parse(new String(Files.readAllBytes(path), UTF_8))
        .flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val authored = path.resolveSibling(path.getFileName.toString.stripSuffix(".json") + ".drake")
      assert(Files.isRegularFile(authored), s"missing $authored")
      val source = new String(Files.readAllBytes(authored), UTF_8)
      if (td.uses.nonEmpty) records += 1
      if (td.codecAspect.payload.nonEmpty) codecs += 1
      assert(Drake.usesText(td) == usesSection(source), s"uses section differs: $path")
      assert(Drake.codecAspectText(td) == codecSection(source), s"codec section differs: $path")
    }
    assert(records >= 85 && codecs >= 4, s"only $records records and $codecs codec sections found")
    console.info(s"Uses over the corpus: ${paths.size} definitions, $records records reproduced, 0 skipped")
    console.info(s"CodecAspect over the corpus: ${paths.size} definitions, $codecs codec sections reproduced, 0 skipped")
  }

  test("the running generator rule emits the definition-backed record and codec section") {
    val td = carrier
    val received = scala.collection.mutable.ArrayBuffer[draco.generator.Emission]()
    val knowledge = Rule.knowledgeService.newKnowledge("usesAndCodec")
    draco.gendrake.Emit.ruleType.pattern(knowledge)
    draco.generator.EmissionReceived.ruleType.pattern(knowledge)
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      session.insert(Seq(td): _*)
      session.fire()
    } finally session.close()
    assert(received.size == 1)
    assert(received.head.value == "type Carrier\nuses [\n  draco drake Text\n  draco format dracon Dracon\n]\ncodec\n  payload [value, valueType]\n")
  }
}
