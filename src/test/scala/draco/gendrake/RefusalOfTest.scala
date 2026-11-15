package draco.gendrake

import draco._
import io.circe.parser
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** Drake's FAILURE FORM is data (Dev, 2026-11-14; GitHub #11): `gendrake.RefusalOf` maps a
 *  definition onto the `draco.Problem`s that refuse it a drake surface, and the emitter raises
 *  the first. Its first increment carries the three codec refusals the emitter wrote by hand —
 *  a discriminator (drake has no syntax for one), a duplicate payload field, a payload field
 *  that is not a name — with the messages they had; the element refusals stay in the emitter.
 */
class RefusalOfTest extends AnyFunSuite with PersistentTestLog {

  private def named(name: String, codec: CodecAspect): TypeDefinition =
    TypeDefinition(TypeName(name, Seq("example")), _codecAspect = codec)

  test("a definition with nothing to refuse has no problems") {
    assert(RefusalOf(TypeDefinition(TypeName("Plain", Seq("example")))).problems.isEmpty)
    assert(RefusalOf(named("Carrier", CodecAspect(_payload = Seq("value", "valueType", "_x9")))).problems.isEmpty)
  }

  test("each codec refusal is one Problem naming the definition, with the emitter's message") {
    val tagged = RefusalOf(named("Tagged", CodecAspect(_discriminator = "tag"))).problems
    assert(tagged.map(_.message) == Seq("Drake.emit: codec discriminator not yet emitted: Tagged"))
    assert(tagged.head.subject == TypeName("Tagged", Seq("example")))

    val twice = RefusalOf(named("Twice", CodecAspect(_payload = Seq("value", "value")))).problems
    assert(twice.map(_.message) == Seq("Drake.emit: duplicate payload field in Twice"))

    Seq("not a field", "a.b", "9lives", "").foreach { field =>
      val odd = RefusalOf(named("Odd", CodecAspect(_payload = Seq(field)))).problems
      assert(odd.map(_.message) == Seq("Drake.emit: invalid payload field in Odd"), s"for '$field'")
    }
  }

  test("every refusal is reported, in the order the emitter judged them") {
    val all = RefusalOf(named("Every", CodecAspect(_discriminator = "tag", _payload = Seq("a b", "a b")))).problems
    assert(all.map(_.message) == Seq(
      "Drake.emit: codec discriminator not yet emitted: Every",
      "Drake.emit: duplicate payload field in Every",
      "Drake.emit: invalid payload field in Every"))
    assert(all.forall(_.subject.name == "Every"))
  }

  test("the definition is read once, and only when the problems are read") {
    var reads = 0
    val refusal = RefusalOf({ reads += 1; named("Twice", CodecAspect(_payload = Seq("value", "value"))) })
    assert(reads == 0)
    assert(refusal.problems.size == 1 && refusal.problems.size == 1)
    assert(reads == 1)
  }

  test("the live emitter raises the first problem, before anything renders") {
    val twice = intercept[IllegalArgumentException](Drake.emit(named("Twice", CodecAspect(_payload = Seq("value", "value")))))
    assert(twice.getMessage == "Drake.emit: duplicate payload field in Twice")
    val every = intercept[IllegalArgumentException](Drake.emit(named("Every", CodecAspect(_discriminator = "tag", _payload = Seq("a b")))))
    assert(every.getMessage == "Drake.emit: codec discriminator not yet emitted: Every")
    assert(Drake.emit(named("Carrier", CodecAspect(_payload = Seq("value")))) == "type Carrier\ncodec\n  payload [value]\n")
  }

  test("no definition in the main and mods corpus is refused") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 202, s"the corpus walk found only ${definitions.size} definitions")

    var payloads = 0
    val refused: Seq[String] = definitions.flatMap { path =>
      val td = parser.parse(Files.readString(Paths.get(path))).flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      if (td.codecAspect.payload.nonEmpty) payloads += 1
      RefusalOf(td).problems.map(problem => s"$path: ${problem.message}")
    }
    refused.foreach(line => log.info(line))
    console.info(s"Refusal over the corpus: ${definitions.size} definitions ($payloads carrying a payload), ${refused.size} refused")
    assert(payloads >= 4, s"the walk found only $payloads payload lists")
    assert(refused.isEmpty, "a committed definition is refused a drake surface:\n  " + refused.mkString("\n  "))
  }
}
