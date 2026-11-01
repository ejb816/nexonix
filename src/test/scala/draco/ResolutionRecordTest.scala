package draco

import draco.scalatarget.DefinitionTrees._
import io.circe.parser
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The RESOLUTION RECORD (Dev accepted 2026-10-21): a definition carries `uses`, the identities
 *  its bodies name by their short form, written by the compact form and validated against the
 *  ontology on every scoped read, so an unscoped reader resolves the file from itself.
 */
class ResolutionRecordTest extends AnyFunSuite {
  private def scope = CanonicalScope.scope
  private def load(stem: String): TypeDefinition =
    parser.parse(Files.readString(Paths.get(s"src/main/resources/$stem.json"))).toOption.get.as[TypeDefinition].toOption.get
  private def read(path: String): String = Files.readString(Paths.get(path))

  /** The seventeen swept definitions of 2026-10-21: bodies short, anchors qualified, records written. */
  private val swept = Seq("draco/Case", "draco/Condition", "draco/DracoAspect", "draco/Dynamic", "draco/Factory", "draco/Fixed",
    "draco/Local", "draco/Monadic", "draco/Mutable", "draco/Parameter", "draco/TypeElement", "draco/TypeName", "draco/TypeParameter",
    "draco/Variable", "draco/gendrake/DomainAspectOf", "draco/gendrake/DracoAspectOf", "draco/scalatarget/CirceCodecs")

  test("compact writes the record: the cross-package identities the bodies name short, sorted, and nothing of the own package") {
    val td = load("draco/TypeName")
    assert(td.uses.map(_.namePath) == Seq("draco.drake.Absent", "draco.drake.Presence", "draco.drake.Present", "draco.format.dracon.Dracon"))
    val again = scope.compact(td)
    assert(again.uses.map(_.namePath) == td.uses.map(_.namePath), "idempotent")
    assert(TypeDefinition.encoder(again) == TypeDefinition.encoder(td))
    assert(scope.compact(load("draco/Problem")).uses.isEmpty, "a definition spelling everything in full or own-package carries no record")
    val surface = read("src/main/resources/draco/TypeName.drake")
    assert(surface.contains("\nuses [\n  draco drake Absent\n  draco drake Presence\n  draco drake Present\n  draco format dracon Dracon\n]\ndomain draco Draco\n"))
    assert(TypeDefinition.encoder(Drake.parse(surface)) == TypeDefinition.encoder(td), "the record round-trips through the surface")
  }

  test("an unscoped reader resolves a recorded definition from itself: unscoped generation equals scoped generation over the whole corpus") {
    // Since the corpus sweep (2026-10-21) every main definition is recorded where it needs to be,
    // so the ontology is needed only to author: unscoped and scoped generation agree everywhere.
    val families = Set("draco/TypeElement", "draco/BodyElement", "draco/Fixed", "draco/Mutable", "draco/Dynamic", "draco/Parameter",
      "draco/Monadic", "draco/Pattern", "draco/Action", "draco/Condition", "draco/Variable", "draco/Factory", "draco/Local", "draco/Case",
      "draco/drake/Presence", "draco/drake/Present", "draco/drake/Absent")
    val all = java.nio.file.Files.walk(Paths.get("src/main/resources/draco")).iterator().asScala
      .filter(_.toString.endsWith(".json")).map(p => Paths.get("src/main/resources").relativize(p).toString.stripSuffix(".json")).toSeq.sorted
    assert(all.size >= 110)
    all.foreach { stem =>
      val td = load(stem)
      assert(DracoGenerator.generate(td) == DracoGenerator.generate(td, scope), stem)
      if (!families(stem) && Files.exists(Paths.get(s"src/main/scala/$stem.scala")))
        assert(DracoGenerator.generate(td) == read(s"src/main/scala/$stem.scala"), stem)
    }
    // A referrer of a swept definition: the engine loads TypeName through its record when judging the codec.
    assert(DracoGenerator.generate(load("draco/TypeDefinition")) == read("src/main/scala/draco/TypeDefinition.scala"))
    assert(DracoGenerator.generate(load("draco/Problem")) == read("src/main/scala/draco/Problem.scala"))
  }

  test("the recorded scope knows only the record: a short name it does not list passes as written") {
    val recorded = NameResolution.recorded(load("draco/TypeName"))
    assert(recorded.resolveName("Dracon").namePath == "draco.format.dracon.Dracon")
    assert(recorded.resolveName("Json").namePath == "Json", "a host leaf is the author's business")
    assert(recorded.resolveName("Unknown").namePath == "Unknown")
  }

  test("an own-package anchor parameter is bare only when its simple name is unique: Percept and Waypoint keep Distance in full and their compact form reads back") {
    // Until 2026-11-01 the anchor-parameter rule wrote an own-package name bare unconditionally, where a body
    // leaf is shortened only when unique across the ontology and the vocabulary; `resolve` then turned the
    // full `domains.sentient.Distance` (5f0e023) back into the bare name the loader had refused as ambiguous
    // against `draco.base.Distance`, and the compact form of either definition could not read back.
    val example = new NameResolution(DomainBuilder.identities(domains.ExampleScope.declarations), domains.ExampleScope.bindings)
    def mods(stem: String): TypeDefinition =
      parser.parse(Files.readString(Paths.get(s"src/mods/resources/$stem.json"))).toOption.get.as[TypeDefinition].toOption.get
    Seq("Percept", "Waypoint").foreach { name =>
      val td = mods(s"domains/sentient/$name")
      val tuple = DracoAspect.parents(td.dracoAspect).find(_.name == "Primal").get.typeParameters.head.json
      assert(tuple == parser.parse("{\"(,)\": [\"Gaze\", \"domains.sentient.Distance\"]}").toOption.get, name)
      assert(TypeDefinition.encoder(example.resolve(td)) == TypeDefinition.encoder(td), s"$name resolves to itself: Gaze bare, Distance in full")
      val compact = example.compact(td)
      assert(TypeDefinition.encoder(compact) == TypeDefinition.encoder(td), s"$name's compact form is the committed JSON")
      assert(TypeDefinition.encoder(example.resolve(compact)) == TypeDefinition.encoder(td), s"$name's compact form reads back")
      assert(DracoGenerator.generate(td) == DracoGenerator.generate(td, example), s"$name: unscoped and scoped generation agree")
      assert(DracoGenerator.generate(td) == read(s"src/mods/scala/domains/sentient/$name.scala"), s"$name: the committed companion")
    }
    // A unique own-package anchor parameter is still written bare, in the example tree and in the canonical corpus.
    val ego = mods("domains/sentient/Ego")
    assert(TypeDefinition.encoder(example.compact(ego)) == TypeDefinition.encoder(ego), "Ego's Holon(Percept, Effect) stays bare")
    val dictionary = load("draco/TypeDictionary")
    assert(TypeDefinition.encoder(scope.compact(dictionary)) == TypeDefinition.encoder(dictionary), "TypeDictionary's Dictionary(TypeName, TypeDefinition) stays bare")
  }

  test("the whole corpus is a fixpoint of the compact form: every definition under main and mods compacts to itself and reads back") {
    // The sweeps of 2026-10-21 (canonical and staged) and 2026-10-31 (example) left every definition in the compact
    // form — a claim their records measured and nothing asserted as a whole until 2026-11-01. Asserted here, the day
    // Percept and Waypoint spelled `domains.sentient.Distance` in full (5f0e023) this test would have failed on both,
    // the anchor-parameter inconsistency ed57aa9 closed. The scope is the one the definition's caller supplies: the
    // canonical identity ontology for draco, the example caller's for domains.
    val example = new NameResolution(DomainBuilder.identities(domains.ExampleScope.declarations), domains.ExampleScope.bindings)
    val roots = Seq("src/main/resources", "src/mods/resources")
    val definitions = roots.flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor is no definition
        .map(p => root -> Paths.get(root).relativize(p).toString.stripSuffix(".json")).toSeq
    }.sorted
    assert(definitions.size >= 183, s"the corpus holds ${definitions.size} definitions; 183 on 2026-11-01")
    definitions.foreach { case (root, stem) =>
      val td = parser.parse(Files.readString(Paths.get(s"$root/$stem.json"))).toOption.get.as[TypeDefinition].toOption.get
      val scope = if (stem.startsWith("domains")) example else CanonicalScope.scope
      val compact = scope.compact(td)
      assert(TypeDefinition.encoder(compact) == TypeDefinition.encoder(td), s"$stem is not its own compact form")
      assert(TypeDefinition.encoder(scope.resolve(compact)) == TypeDefinition.encoder(scope.resolve(td)), s"$stem's compact form does not read back")
    }
  }

  test("a record entry the ontology does not define fails every scoped read") {
    val td = load("draco/TypeName")
    val stale = TypeDefinition(td.typeName, _uses = td.uses :+ TypeName("Vanished", Seq("draco", "elsewhere")), _dracoAspect = td.dracoAspect,
      _domainAspect = td.domainAspect)
    val error = intercept[IllegalArgumentException](scope.resolve(stale))
    assert(error.getMessage.contains("draco.elsewhere.Vanished"), error.getMessage)
    intercept[IllegalArgumentException](DracoGenerator.generate(stale, scope))
  }
}
