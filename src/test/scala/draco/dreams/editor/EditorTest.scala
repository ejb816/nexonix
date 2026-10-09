package draco.dreams.editor

import draco._
import io.circe.Json
import io.circe.syntax._
import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import java.util.concurrent.CopyOnWriteArrayList
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** The authoring increment's first slice (2026-10-09): a `Latent` — one domain in
  * candidate form, its definition plus its members' definitions — goes to the
  * `Editor` actor and comes back as an `Actual`, the same definitions with the
  * problems found, accepted iff there are none. The Editor validates against the
  * ontology that WOULD result — the accepted ontology it was given plus the
  * candidate — using core's own rules (Completeness, SelfDeclaration,
  * DerivationResolvable, CollectProblems) in a fresh session per message; it
  * never consults the classpath and never mutates the ontology it was given.
  * Composing accepted domains and their relationships into a `DomainOntology`
  * is the next increment; this one stops at the Actual.
  *
  * Staged definitions are outside the main-corpus discovery gates, so the trio
  * checks live here. */
class EditorTest extends AnyFunSuite {
  private def read(path: String): String = new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  private val trios = Seq(
    ("Latent", () => Latent.typeDefinition),
    ("Actual", () => Actual.typeDefinition),
    ("Editor", () => Editor.typeDefinition)
  )

  trios.foreach { case (name, companion) =>
    val stem = s"draco/dreams/editor/$name"
    def definition: TypeDefinition = io.circe.parser.parse(read(s"src/mods/resources/$stem.json"))
      .flatMap(_.as[TypeDefinition]).fold(error => fail(error.toString), identity)

    test(s"$name Drake and JSON are canonical mutual projections") {
      val source = read(s"src/mods/resources/$stem.drake")
      val parsed = Drake.parse(source)
      assert(parsed.asJson == definition.asJson)
      assert(Drake.emit(definition) == source)
      assert(Drake.emit(parsed) == source)
    }

    test(s"$name Scala is generated from its staged definition") {
      assert(DracoGenerator.generate(definition) == read(s"src/mods/scala/$stem.scala"))
      assert(!Files.exists(Paths.get(s"src/main/scala/$stem.scala")), "duplicate main-tier scaffold")
      assert(companion().typeName == definition.typeName)
      assert(companion().domainAspect.typeName == Editor.typeDefinition.typeName)
    }
  }

  test("Editor is a domain of two members and an actor whose message is a Latent") {
    val td = Editor.typeDefinition
    assert(td.domainAspect.typeName == td.typeName)
    assert(Editor.elementTypeNames == Seq("Actual", "Latent"))
    assert(Editor.domainType.typeDictionary.size == 2)
    assert(!ActorAspect.isEmpty(td.actorAspect))
    assert(td.actorAspect.messageType == TypeName("Latent", Seq("draco", "dreams", "editor")))
    assert(RuleAspect.isEmpty(td.ruleAspect))
    val loaded = DomainBuilder.define("Editor", Seq("draco", "dreams", "editor"))
    assert(DomainBuilder.validate(loaded).isEmpty)
    assert(loaded.typeDictionary.elementTypes.forall(m => !DomainBuilder.isStub(m)))
  }

  // --- the candidate domain ---

  private val pkg = Seq("example", "fresh")
  private val fresh = TypeName("Fresh", pkg)
  private def definition(members: Seq[String], self: TypeName = fresh, identity: TypeName = fresh): TypeDefinition =
    TypeDefinition(identity, _domainAspect = DomainAspect(self, members))
  private def member(name: String, parents: TypeName*): TypeDefinition =
    TypeDefinition(TypeName(name, pkg),
      _dracoAspect = DracoAspect(_derivation = parents.map(TypeName.encoder(_))),
      _domainAspect = DomainAspect(fresh))
  private val primal = TypeName("Primal", Seq("draco"))
  private def reference: DomainOntology = DomainBuilder.ontology(DomainBuilder.define("Draco", Seq("draco")))

  /** Mint one Editor over `ontology`, send every Latent, and collect the Actuals
    * it delivers to its consumer — in order, one per Latent. */
  private def edit(ontology: DomainOntology, latents: Seq[Latent], name: String): Seq[Actual] = {
    val actuals = new CopyOnWriteArrayList[Actual]()
    val system = ActorSystem(Editor.actorType(ontology, a => { actuals.add(a); () }).asInstanceOf[Actor[Latent]], name)
    try {
      latents.foreach(system ! _)
      val deadline = System.nanoTime() + 10.seconds.toNanos
      while (actuals.size < latents.size && System.nanoTime() < deadline) Thread.sleep(20)
    } finally {
      system.terminate()
      Await.result(system.whenTerminated, 5.seconds)
    }
    actuals.asScala.toSeq
  }

  private def subjects(actual: Actual): Set[String] = actual.problems.map(_.subject.name).toSet

  test("a well-formed Latent is accepted and its Actual domain is populated from the supplied members") {
    val latent = Latent(definition(Seq("Root", "Leaf")), Seq(member("Root", primal), member("Leaf", TypeName("Root", pkg))))
    val Seq(actual) = edit(reference, Seq(latent), "editorAccepts")
    assert(actual.accepted, s"expected acceptance; got ${actual.problems.map(_.message)}")
    assert(actual.definition.typeName == fresh)
    val domain = actual.domain
    assert(domain.typeDefinition.typeName == fresh)
    assert(domain.typeDictionary.size == 2)
    assert(domain.typeDictionary.elementTypes.forall(m => !DomainBuilder.isStub(m)), "members are the supplied definitions, not placeholders")
    assert(DracoAspect.parents(domain.typeDictionary(TypeName("Leaf", pkg)).dracoAspect).map(_.name) == Seq("Root"))
  }

  test("a member named but not supplied is a placeholder in the candidate, and a Completeness problem — not accepted") {
    val latent = Latent(definition(Seq("Root", "Ghost")), Seq(member("Root", primal)))
    val Seq(actual) = edit(reference, Seq(latent), "editorIncomplete")
    assert(!actual.accepted)
    assert(subjects(actual) == Set("Ghost"), s"got ${actual.problems.map(_.message)}")
    assert(actual.members.size == 1, "the Actual echoes exactly the members that were examined")
  }

  test("a member deriving from a type the ontology does not define is a DerivationResolvable problem") {
    val latent = Latent(definition(Seq("Leaf")), Seq(member("Leaf", TypeName("Nonexistent", Seq("draco")))))
    val Seq(actual) = edit(reference, Seq(latent), "editorDangling")
    assert(!actual.accepted)
    assert(subjects(actual) == Set("Leaf"), s"got ${actual.problems.map(_.message)}")
  }

  test("a domain whose self-declaration omits its type parameter is a SelfDeclaration problem") {
    val generic = TypeName("Fresh", pkg, Seq(Json.fromString("T")))
    val latent = Latent(definition(Seq("Root"), self = fresh, identity = generic), Seq(member("Root", primal)))
    val Seq(actual) = edit(reference, Seq(latent), "editorMisdeclared")
    assert(!actual.accepted)
    assert(subjects(actual) == Set("Fresh"), s"got ${actual.problems.map(_.message)}")
  }

  test("the Editor judges each Latent on its own, in order, and leaves the given ontology untouched") {
    val ontology = reference
    val before = ontology.size
    val good = Latent(definition(Seq("Root")), Seq(member("Root", primal)))
    val bad = Latent(definition(Seq("Leaf")), Seq(member("Leaf", TypeName("Root", pkg))))
    val actuals = edit(ontology, Seq(good, bad, good), "editorSequence")
    assert(actuals.map(_.accepted) == Seq(true, false, true),
      s"a sibling accepted earlier is not part of the given ontology; got ${actuals.map(_.problems.map(_.message))}")
    assert(ontology.size == before)
    assert(!ontology.defines(TypeName("Root", pkg)), "acceptance does not compose; that is the next increment")
  }

  test("Latent and Actual round-trip through JSON; Actual's domain is derived, not carried") {
    val latent = Latent(definition(Seq("Root")), Seq(member("Root", primal)))
    val decodedLatent = latent.asJson.as[Latent].fold(error => fail(error.toString), identity)
    assert(decodedLatent.definition.typeName == fresh)
    assert(decodedLatent.members.map(_.typeName) == Seq(TypeName("Root", pkg)))
    val problem = Problem(TypeName("Root", pkg), "example")
    val actual = Actual(latent.definition, latent.members, Seq(problem))
    assert(!actual.accepted)
    assert(Actual(latent.definition, latent.members).accepted)
    val json = actual.asJson
    assert(json.hcursor.downField("domain").failed, "the populated domain is a dyn on the Actual, not a JSON field")
    val decoded = json.as[Actual].fold(error => fail(error.toString), identity)
    assert(decoded.problems.map(_.message) == Seq("example"))
    assert(decoded.domain.typeDictionary.size == 1)
  }
}
