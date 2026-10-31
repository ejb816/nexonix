package domains

import draco._
import io.circe.Json
import io.circe.syntax.EncoderOps
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite

/** The four Intent types — aerial FlightIntent, marine VoyageIntent, ethereal LaunchIntent,
  * terrestrial MarchIntent — are each medium's entry message, the fact its OriginateReport joins.
  * They carry the same `json` factory the four reports took, so the tests that seed a medium
  * construct the intent as `X(payload)` instead of an anonymous host construction.
  * Each case iterates over the four: the trio, the by-name argument, the codec the Dracon shape derives.
  */
class IntentFactoryTest extends AnyFunSuite {
  private type Dracon = draco.format.dracon.Dracon

  private def read(path: String) = new String(Files.readAllBytes(Paths.get(path)), "UTF-8")

  /** medium, intent name, the companion's typeDefinition, its factory, its Null, the domain marker. */
  private case class Intent(
    medium: String,
    name: String,
    definition: TypeDefinition,
    make: (=> Json) => Dracon,
    nullValue: Dracon,
    isMedium: Dracon => Boolean)

  private val intents: Seq[Intent] = Seq(
    Intent("aerial", "FlightIntent", domains.aerial.FlightIntent.typeDefinition,
      json => domains.aerial.FlightIntent(json), domains.aerial.FlightIntent.Null, _.isInstanceOf[domains.aerial.Aerial]),
    Intent("marine", "VoyageIntent", domains.marine.VoyageIntent.typeDefinition,
      json => domains.marine.VoyageIntent(json), domains.marine.VoyageIntent.Null, _.isInstanceOf[domains.marine.Marine]),
    Intent("ethereal", "LaunchIntent", domains.ethereal.LaunchIntent.typeDefinition,
      json => domains.ethereal.LaunchIntent(json), domains.ethereal.LaunchIntent.Null, _.isInstanceOf[domains.ethereal.Ethereal]),
    Intent("terrestrial", "MarchIntent", domains.terrestrial.MarchIntent.typeDefinition,
      json => domains.terrestrial.MarchIntent(json), domains.terrestrial.MarchIntent.Null, _.isInstanceOf[domains.terrestrial.Terrestrial]))

  private val payload: Json = Json.obj(("tasking", Json.fromString("PROBE")), ("value", Json.fromInt(42)))

  test("each intent's Drake, JSON and generated Scala agree exactly and declare the json factory") {
    for (i <- intents) {
      val stem = s"src/mods/resources/domains/${i.medium}/${i.name}"
      val source = read(stem + ".drake")
      val td = io.circe.parser.parse(read(stem + ".json")).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
      assert(Drake.emit(td) == source, i.name)
      assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td), i.name)
      assert(DracoGenerator.generate(td) == read(s"src/mods/scala/domains/${i.medium}/${i.name}.scala"), i.name)
      assert(td.dracoAspect.factory.parameters.map(p => (p.name, TypeForm.text(p.valueType.json))) == Seq(("json", "Json")), i.name)
      assert(DracoAspect.parents(td.dracoAspect).map(_.name) == Seq(i.medium.capitalize, "Dracon"), i.name)
      assert(TypeDefinition.encoder(i.definition) == TypeDefinition.encoder(td), i.name)
    }
  }

  test("each factory argument is evaluated once, on first read, and never when unused") {
    for (i <- intents) {
      var evaluated = 0
      val intent = i.make({ evaluated += 1; payload })
      assert(evaluated == 0, i.name)
      assert(intent.typeDefinition eq i.definition, i.name)
      assert(i.isMedium(intent), i.name)
      assert(evaluated == 0, i.name)
      assert(intent.json == payload, i.name)
      assert(intent.json.hcursor.get[Int]("value").contains(42), i.name)
      assert(evaluated == 1, i.name)
      assert(i.nullValue.json == Json.Null, i.name)
    }
  }

  test("each derived codec writes the json field and reads the intent back") {
    val encoded = domains.aerial.FlightIntent(payload).asJson
    assert(encoded == Json.obj(("json", payload)))
    val flight = encoded.as[domains.aerial.FlightIntent].fold(throw _, identity)
    assert(flight.json == payload && (flight.typeDefinition eq domains.aerial.FlightIntent.typeDefinition))
    val voyage = domains.marine.VoyageIntent(payload).asJson.as[domains.marine.VoyageIntent].fold(throw _, identity)
    assert(voyage.json == payload && (voyage.typeDefinition eq domains.marine.VoyageIntent.typeDefinition))
    val launch = domains.ethereal.LaunchIntent(payload).asJson.as[domains.ethereal.LaunchIntent].fold(throw _, identity)
    assert(launch.json == payload && (launch.typeDefinition eq domains.ethereal.LaunchIntent.typeDefinition))
    val march = domains.terrestrial.MarchIntent(payload).asJson.as[domains.terrestrial.MarchIntent].fold(throw _, identity)
    assert(march.json == payload && (march.typeDefinition eq domains.terrestrial.MarchIntent.typeDefinition))
    assert(Json.obj().as[domains.terrestrial.MarchIntent].fold(throw _, identity).json == Json.Null)
  }
}
