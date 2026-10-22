package draco

import draco.scalatarget.DefinitionTrees._
import io.circe.parser
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}

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

  test("an unscoped reader resolves a recorded definition from itself: unscoped generation equals scoped generation") {
    swept.foreach { stem =>
      val td = load(stem)
      assert(DracoGenerator.generate(td) == DracoGenerator.generate(td, scope), stem)
    }
    // The six swept definitions with a file of their own (the eleven family kinds share TypeElement.scala).
    swept.filter(stem => Files.exists(Paths.get(s"src/main/scala/$stem.scala")) && stem != "draco/TypeElement").foreach { stem =>
      assert(DracoGenerator.generate(load(stem)) == read(s"src/main/scala/$stem.scala"), stem)
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

  test("a record entry the ontology does not define fails every scoped read") {
    val td = load("draco/TypeName")
    val stale = TypeDefinition(td.typeName, _uses = td.uses :+ TypeName("Vanished", Seq("draco", "elsewhere")), _dracoAspect = td.dracoAspect,
      _domainAspect = td.domainAspect)
    val error = intercept[IllegalArgumentException](scope.resolve(stale))
    assert(error.getMessage.contains("draco.elsewhere.Vanished"), error.getMessage)
    intercept[IllegalArgumentException](DracoGenerator.generate(stale, scope))
  }
}
