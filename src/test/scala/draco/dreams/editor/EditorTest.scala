package draco.dreams.editor

import draco._
import draco.dreams.user.{Create, Delete, Latent, Read, Request, Response, Update}
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
  *
  * Since 2026-10-12 the Editor HOLDS the accepted ontology (Dev: the innate, endogenous
  * ontology of draco itself, so acceptance must not compromise the framework). Each
  * Latent is judged against the ontology that would result — every accepted domain
  * plus the candidate, `including` replacing a domain of the same name and package —
  * with the whole of it in working memory, so an update cannot orphan a dependent;
  * the state is swapped only when every rule is quiet, and `Uniqueness` refuses a
  * name owned by more than one domain. Acceptance generates and activates nothing.
  * The grown ontology reaches the caller through the factory's second consumer,
  * `ontology`, invoked after the Actual on every acceptance and never on a rejection
  * (Dev, 2026-10-12, the initialization client's first design point): the ontology
  * has no codec, so it cannot ride a message or the Actual, and a caller keeping the
  * last delivery holds the current state.
  *
  * The CRUD reading (Dev, 2026-10-12): User defines the requests, Editor the responses.
  * `Latent` is User's Create/Update request (the ontology decides which, by identity);
  * `Response` in User is the abstract reply — `problems`, `accepted` — that Editor's
  * `Actual` derives, so a user consumes `User.Response`, never `Editor.Actual`, and
  * the dependency runs one way: Editor depends on User, never the reverse. The Editor's
  * consumer is typed `Response -> Unit` for that reason; these tests narrow to Actual
  * because they inspect the definitions, which only the Editor's side knows.
  *
  * Staged definitions are outside the main-corpus discovery gates, so the trio
  * checks live here. */
class EditorTest extends AnyFunSuite {
  private def read(path: String): String = new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  private val trios = Seq(
    ("Accepted", () => Accepted.typeDefinition),
    ("Actual", () => Actual.typeDefinition),
    ("CreateHeld", () => CreateHeld.typeDefinition),
    ("UpdateUnheld", () => UpdateUnheld.typeDefinition),
    ("ReadUnheld", () => ReadUnheld.typeDefinition),
    ("DeleteUnheld", () => DeleteUnheld.typeDefinition),
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

  test("Editor is a domain of six members — Accepted, Actual and four intent rules — and a thin actor over User's Request family") {
    val td = Editor.typeDefinition
    assert(td.domainAspect.typeName == td.typeName)
    assert(Editor.elementTypeNames == Seq("Accepted", "Actual", "CreateHeld", "DeleteUnheld", "ReadUnheld", "UpdateUnheld"))
    assert(Editor.domainType.typeDictionary.size == 6)
    assert(!ActorAspect.isEmpty(td.actorAspect))
    assert(td.actorAspect.messageType == TypeName("Request", Seq("draco", "dreams", "user")))
    // Since 2026-10-15 the actor is the membrane and the knowledge is RULES: no case branches,
    // no globals; the message body inserts the request, the Accepted fact and the proposed
    // ontology with its definitions, fires, and reads the Actual back out of the problems.
    assert(!td.actorAspect.message.body.exists(_.isInstanceOf[Case]), "no dispatch in the actor: which rule matches is the dispatch")
    assert(td.dracoAspect.globalElements.isEmpty, "no judgment in the actor: the rules judge")
    val fired = td.actorAspect.start.body.map(e => TypeForm.text(e.valueType) + " " + e.value.noSpaces).count(_.contains("ruleType.pattern"))
    assert(fired == 9, s"five core rules and four Editor rules build the knowledge; got $fired")
    Seq(CreateHeld, UpdateUnheld, ReadUnheld, DeleteUnheld).foreach(r => assert(!RuleAspect.isEmpty(r.typeDefinition.ruleAspect)))
    assert(RuleAspect.isEmpty(Accepted.typeDefinition.ruleAspect) && Accepted.typeDefinition.dracoAspect.elements.map(_.name) == Seq("ontology"))
    val request = TypeName("Request", Seq("draco", "dreams", "user"))
    val latent = TypeName("Latent", Seq("draco", "dreams", "user"))
    Seq(Latent, Read, Delete).foreach(m => assert(DracoAspect.parents(m.typeDefinition.dracoAspect) == Seq(request)))
    // Latent is the candidate SHAPE two verbs share (Dev, 2026-10-14): abstract, no factory.
    Seq(Create, Update).foreach(m => assert(DracoAspect.parents(m.typeDefinition.dracoAspect) == Seq(latent)))
    assert(Latent.typeDefinition.dracoAspect.factory.parameters.isEmpty)
    val shape: Latent = Create(definition(Seq.empty))
    assert(shape.isInstanceOf[Request] && Update(definition(Seq.empty)).isInstanceOf[Latent] && Read(fresh).isInstanceOf[Request] && Delete(fresh).isInstanceOf[Request])
    // A request knows what it proposes and what it reports on (User dyns, 2026-10-15).
    val held = reference
    assert(Read(fresh).proposal(held) eq held, "a Read proposes the ontology as it is — the same one, so the actor sees nothing changing")
    assert(Delete(fresh).proposal(held).size == held.size, "deleting what is not held proposes the same contents")
    assert(Read(fresh).subject(held).typeName == fresh && Read(fresh).contents(held).isEmpty)
    assert(RuleAspect.isEmpty(td.ruleAspect))
    val loaded = DomainBuilder.define("Editor", Seq("draco", "dreams", "editor"))
    assert(DomainBuilder.validate(loaded).isEmpty)
    assert(loaded.typeDictionary.elementTypes.forall(m => !DomainBuilder.isStub(m)))
  }

  test("Actual derives User's Response, so Editor depends on User and User never on Editor") {
    val response = TypeName("Response", Seq("draco", "dreams", "user"))
    assert(DracoAspect.parents(Actual.typeDefinition.dracoAspect) == Seq(response))
    assert(Actual(definition(Seq.empty)).isInstanceOf[Response])
    val editor = DomainBuilder.define("Editor", Seq("draco", "dreams", "editor"))
    val user = DomainBuilder.define("User", Seq("draco", "dreams", "user"))
    val ontology = DomainBuilder.ontology(editor, user, DomainBuilder.define("Draco", Seq("draco")))
    assert(ontology.derivations.exists(e => e.child.namePath == Actual.typeDefinition.typeName.namePath && e.parent.namePath == response.namePath))
    assert(ontology.dependencies(editor).map(_.typeDefinition.typeName).contains(user.typeDefinition.typeName),
      "the ontology states the edge with the relationship it already carries: derivation")
    assert(!ontology.dependencies(user).map(_.typeDefinition.typeName).contains(editor.typeDefinition.typeName),
      "no cycle: a Haskell module pair could not import each other")
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
  private def definitionOf(domain: TypeName, members: String*): TypeDefinition =
    TypeDefinition(domain, _domainAspect = DomainAspect(domain, members))
  private def memberOf(domain: TypeName, name: String, parents: TypeName*): TypeDefinition =
    TypeDefinition(TypeName(name, domain.namePackage),
      _dracoAspect = DracoAspect(_derivation = parents.map(TypeName.encoder(_))),
      _domainAspect = DomainAspect(domain))
  private val other = TypeName("Other", Seq("example", "other"))
  private val root = TypeName("Root", pkg)
  private val leaf = TypeName("Leaf", pkg)

  /** What one Editor delivered to its two consumers: the Actuals in order, one per
    * Latent; the ontologies in order, one per ACCEPTANCE (2026-10-12: the grown
    * ontology reaches the caller through the factory's second consumer, never
    * through the Actual); and the interleaving as it happened. */
  private case class Delivered(actuals: Seq[Actual], ontologies: Seq[DomainOntology], order: Seq[String])

  /** Mint one Editor over `initial`, send every Latent, and collect everything it
    * delivers. Every Latent yields an Actual, so the Actual count is the signal
    * that the Editor is done. */
  private def deliveries(initial: DomainOntology, latents: Seq[Request], name: String): Delivered = {
    val responses = new CopyOnWriteArrayList[Response]()
    val ontologies = new CopyOnWriteArrayList[DomainOntology]()
    val order = new CopyOnWriteArrayList[String]()
    val system = ActorSystem(Editor.actorType(initial,
      r => { responses.add(r); order.add("actual"); () },
      o => { ontologies.add(o); order.add("ontology"); () }).asInstanceOf[Actor[Request]], name)
    try {
      latents.foreach(system ! _)
      val deadline = System.nanoTime() + 10.seconds.toNanos
      while (responses.size < latents.size && System.nanoTime() < deadline) Thread.sleep(20)
    } finally {
      system.terminate()
      Await.result(system.whenTerminated, 5.seconds)
    }
    val actuals = responses.asScala.toSeq.map {
      case a: Actual => a
      case other => fail(s"the Editor delivered a Response that is not an Actual: $other")
    }
    Delivered(actuals, ontologies.asScala.toSeq, order.asScala.toSeq)
  }

  private def edit(initial: DomainOntology, latents: Seq[Latent], name: String): Seq[Actual] =
    deliveries(initial, latents, name).actuals

  private def subjects(actual: Actual): Set[String] = actual.problems.map(_.subject.name).toSet

  test("a well-formed Latent is accepted and its Actual domain is populated from the supplied members") {
    val latent = Create(definition(Seq("Root", "Leaf")), Seq(member("Root", primal), member("Leaf", TypeName("Root", pkg))))
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
    val latent = Create(definition(Seq("Root", "Ghost")), Seq(member("Root", primal)))
    val Seq(actual) = edit(reference, Seq(latent), "editorIncomplete")
    assert(!actual.accepted)
    assert(subjects(actual) == Set("Ghost"), s"got ${actual.problems.map(_.message)}")
    assert(actual.members.size == 1, "the Actual echoes exactly the members that were examined")
  }

  test("a member deriving from a type the ontology does not define is a DerivationResolvable problem") {
    val latent = Create(definition(Seq("Leaf")), Seq(member("Leaf", TypeName("Nonexistent", Seq("draco")))))
    val Seq(actual) = edit(reference, Seq(latent), "editorDangling")
    assert(!actual.accepted)
    assert(subjects(actual) == Set("Leaf"), s"got ${actual.problems.map(_.message)}")
  }

  test("a domain whose self-declaration names another definition is a SelfDeclaration problem; omitting its type parameter is not") {
    val elsewhere = Create(definition(Seq("Root"), self = TypeName("Elsewhere", pkg)), Seq(member("Root", primal)))
    val generic = TypeName("Fresh", pkg, Seq(Json.fromString("T")))
    val parameterized = Create(definition(Seq("Root"), self = fresh, identity = generic), Seq(member("Root", primal)))
    val Seq(misdeclared, bare) = edit(reference, Seq(elsewhere, parameterized), "editorMisdeclared")
    assert(!misdeclared.accepted)
    assert(subjects(misdeclared) == Set("Fresh"), s"got ${misdeclared.problems.map(_.message)}")
    // Parameters compose references, not definitions (Dev, 2026-10-12): a self-declaration
    // points at the definition, keyed bare — Format(F) self-declares as Format.
    assert(bare.accepted, s"got ${bare.problems.map(_.message)}")
  }

  test("an accepted domain joins the Editor's ontology: a later Latent resolves against it; the given ontology is untouched") {
    val ontology = reference
    val before = ontology.size
    val first = Create(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal)))
    val second = Create(definitionOf(other, "Twig"), Seq(memberOf(other, "Twig", root)))
    val actuals = edit(ontology, Seq(second, first, second), "editorComposes")
    assert(actuals.map(_.accepted) == Seq(false, true, true),
      s"Twig resolves only once Fresh is accepted; got ${actuals.map(_.problems.map(_.message))}")
    assert(subjects(actuals.head) == Set("Twig"))
    assert(ontology.size == before && !ontology.defines(root), "the ontology the Editor was given is never mutated")
  }

  test("a Latent owning a name an accepted domain already owns is refused by Uniqueness") {
    val shadow = TypeName("Shadow", Seq("draco"))
    val claim = Create(definitionOf(shadow, "Primal"), Seq(memberOf(shadow, "Primal")))
    val asDomain = Create(definitionOf(primal))              // a DOMAIN named like Draco's member
    val elsewhere = TypeName("Shadow", Seq("example", "shadow"))
    val distinct = Create(definitionOf(elsewhere, "Primal"), Seq(memberOf(elsewhere, "Primal")))
    val actuals = edit(reference, Seq(claim, asDomain, distinct), "editorUniqueness")
    assert(actuals.map(_.accepted) == Seq(false, false, true), s"got ${actuals.map(_.problems.map(_.message))}")
    assert(subjects(actuals(0)) == Set("Primal") && subjects(actuals(1)) == Set("Primal"))
  }

  test("an update replaces the domain of the same identity in place, and cannot orphan a dependent") {
    val steps = Seq(
      Create(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal))),
      Update(definitionOf(fresh, "Root", "Leaf"), Seq(memberOf(fresh, "Root", primal), memberOf(fresh, "Leaf", root))),
      Create(definitionOf(other, "Twig"), Seq(memberOf(other, "Twig", leaf))),
      Update(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal))),   // drops Leaf: Twig would dangle
      Update(definitionOf(fresh, "Root", "Leaf", "Branch"),
        Seq(memberOf(fresh, "Root", primal), memberOf(fresh, "Leaf", root), memberOf(fresh, "Branch", leaf))))
    val actuals = edit(reference, steps, "editorUpdates")
    assert(actuals.map(_.accepted) == Seq(true, true, true, false, true),
      s"got ${actuals.map(_.problems.map(p => s"${p.subject.name}: ${p.message}"))}")
    assert(subjects(actuals(3)) == Set("Twig"), "the refused update names the dependent it would orphan, not itself")
    assert(actuals(4).domain.typeDictionary.size == 3)
  }

  test("the grown ontology reaches the caller through the Editor's ontology consumer: once per acceptance, after the Actual, never on a rejection") {
    val first = Create(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal)))
    val second = Create(definitionOf(other, "Twig"), Seq(memberOf(other, "Twig", root)))
    val delivered = deliveries(reference, Seq(second, first, second), "editorDelivers")
    assert(delivered.actuals.map(_.accepted) == Seq(false, true, true))
    assert(delivered.ontologies.size == 2, "one ontology per acceptance; the rejection delivers none")
    assert(delivered.order == Seq("actual", "actual", "ontology", "actual", "ontology"),
      "the Actual is delivered first, then the ontology, and only when the state changed")
    assert(delivered.ontologies.head.defines(root) && !delivered.ontologies.head.defines(TypeName("Twig", other.namePackage)),
      "the first delivery is the state after Fresh alone")
    assert(delivered.ontologies.last.defines(root) && delivered.ontologies.last.defines(TypeName("Twig", other.namePackage)),
      "the last delivery is the whole accepted state: the caller keeping its last ontology holds the current one")
    assert(delivered.ontologies.last.keys.size == reference.size + 2)
  }

  test("a delivered ontology is the composed state, not a copy of the initial one: an update's delivery carries the replacement") {
    val steps = Seq(
      Create(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal))),
      Update(definitionOf(fresh, "Root", "Leaf"), Seq(memberOf(fresh, "Root", primal), memberOf(fresh, "Leaf", root))))
    val delivered = deliveries(reference, steps, "editorDeliversUpdate")
    assert(delivered.actuals.forall(_.accepted))
    val Seq(before, after) = delivered.ontologies
    assert(!before.defines(leaf) && after.defines(leaf))
    assert(before.keys.size == after.keys.size, "an update replaces in place; the delivered ontology has no second Fresh")
    assert(after.keys.find(_.typeDefinition.typeName == fresh).map(_.typeDictionary.size).contains(2))
    assert(!reference.defines(root), "the initial ontology is never mutated or delivered")
  }

  test("a Read names one domain: an Actual of its definition and members from the accepted ontology, or a Problem when it owns none; no ontology is delivered") {
    val create = Create(definitionOf(fresh, "Root", "Leaf"), Seq(memberOf(fresh, "Root", primal), memberOf(fresh, "Leaf", root)))
    val draco = TypeName("Draco", Seq("draco"))
    val nowhere = TypeName("Nowhere", Seq("example", "nowhere"))
    val delivered = deliveries(reference, Seq(Read(fresh), create, Read(fresh), Read(draco), Read(nowhere)), "editorReads")
    val Seq(before, created, after, core, missing) = delivered.actuals
    assert(!before.accepted && subjects(before) == Set("Fresh"), "before acceptance Fresh is not owned: one Problem naming it, a stub definition, no members")
    assert(before.definition.typeName == fresh && before.members.isEmpty)
    assert(created.accepted)
    assert(after.accepted && after.definition.typeName == fresh && after.members.map(_.typeName.name) == Seq("Root", "Leaf"),
      "after acceptance the Read answers with the domain as the ontology holds it")
    assert(core.accepted && core.definition.typeName == draco && core.members.size == Draco.elementTypeNames.size,
      "a domain of the initial ontology reads too: draco's own definition and every member")
    assert(!missing.accepted && subjects(missing) == Set("Nowhere"))
    assert(delivered.ontologies.size == 1 && delivered.order == Seq("actual", "actual", "ontology", "actual", "actual", "actual"),
      "a Read never changes the state, so only the one acceptance delivered an ontology")
  }

  test("a Delete names one domain: removed when nothing depends on it, refused naming the dependent when something does, refused when not owned") {
    val draco = TypeName("Draco", Seq("draco"))
    val nowhere = TypeName("Nowhere", Seq("example", "nowhere"))
    val steps = Seq(
      Delete(nowhere),                                                                   // not owned
      Create(definitionOf(fresh, "Root"), Seq(memberOf(fresh, "Root", primal))),       // accepted
      Create(definitionOf(other, "Twig"), Seq(memberOf(other, "Twig", root))),         // accepted; Twig derives Root
      Delete(draco),                                                                     // Root derives Primal: refused naming Root
      Delete(fresh),                                                                     // Twig derives Root: refused naming Twig
      Delete(other),                                                                     // nothing depends on Other: removed
      Delete(fresh))                                                                     // now nothing depends on Fresh: removed
    val delivered = deliveries(reference, steps, "editorDeletes")
    val actuals = delivered.actuals
    assert(actuals.map(_.accepted) == Seq(false, true, true, false, false, true, true),
      s"got ${actuals.map(a => a.problems.map(p => s"${p.subject.name}: ${p.message}"))}")
    assert(subjects(actuals(0)) == Set("Nowhere") && actuals(0).members.isEmpty)
    assert(subjects(actuals(3)) == Set("Root"), "the framework's own core cannot be deleted while anything derives from it")
    assert(subjects(actuals(4)) == Set("Twig"), "a refused deletion names the dependent it would orphan")
    assert(actuals(5).definition.typeName == other && actuals(5).members.map(_.typeName.name) == Seq("Twig"),
      "the Actual of an accepted deletion carries the domain as the ontology held it")
    assert(delivered.ontologies.size == 4, "two acceptances and two deletions delivered an ontology; the refusals did not")
    val last = delivered.ontologies.last
    assert(last.size == reference.size && !last.defines(root) && last.defines(primal), "the shrunk ontology is Draco alone again")
    assert(reference.defines(primal) && reference.size == last.size, "the initial ontology was never mutated")
  }

  test("Create and Update are distinct intents: a Create of an owned identity is refused, an Update of an unowned one is refused") {
    val root = Seq(memberOf(fresh, "Root", primal))
    val steps = Seq(
      Update(definitionOf(fresh, "Root"), root),    // nothing to update yet
      Create(definitionOf(fresh, "Root"), root),    // created
      Create(definitionOf(fresh, "Root"), root),    // already owned: refused, not silently updated
      Update(definitionOf(fresh, "Root"), root))    // now an update
    val delivered = deliveries(reference, steps, "editorIntents")
    assert(delivered.actuals.map(_.accepted) == Seq(false, true, false, true),
      s"got ${delivered.actuals.map(_.problems.map(_.message))}")
    assert(delivered.actuals(0).problems.map(_.message) == Seq("domain Fresh is not in the accepted ontology"))
    assert(delivered.actuals(2).problems.map(_.message) == Seq("domain Fresh is already in the accepted ontology"))
    assert(delivered.ontologies.size == 2, "the two refusals delivered nothing")
  }

  test("the intent rules are Editor-domain rules: they fire over the request and the Accepted fact in a bare session, no actor needed") {
    val knowledge = Rule.knowledgeService.newKnowledge("EditorRulesProbe")
    Seq(CreateHeld.ruleType, UpdateUnheld.ruleType, ReadUnheld.ruleType, DeleteUnheld.ruleType, CollectProblems.ruleType).foreach(_.pattern(knowledge))
    def judged(request: Request): Seq[String] = {
      val problems = scala.collection.mutable.ArrayBuffer.empty[Problem]
      val session = knowledge.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)
      session.set("problems", problems)
      session.insert(Seq(request): _*)
      session.insert(Seq(Accepted(reference)): _*)
      session.fire()
      session.close()
      problems.map(_.message).toSeq
    }
    val draco = TypeName("Draco", Seq("draco"))
    assert(judged(Create(definitionOf(draco))) == Seq("domain Draco is already in the accepted ontology"), "CreateHeld")
    assert(judged(Create(definitionOf(fresh))).isEmpty)
    assert(judged(Update(definitionOf(fresh))) == Seq("domain Fresh is not in the accepted ontology"), "UpdateUnheld")
    assert(judged(Update(definitionOf(draco))).isEmpty)
    assert(judged(Read(fresh)) == Seq("domain Fresh is not in the accepted ontology"), "ReadUnheld")
    assert(judged(Read(draco)).isEmpty)
    assert(judged(Delete(fresh)) == Seq("domain Fresh is not in the accepted ontology"), "DeleteUnheld")
    assert(judged(Delete(draco)).isEmpty, "DeleteUnheld is silent on a held domain; whether it may go is the core rules' judgment")
  }

  test("Latent and Actual round-trip through JSON; Actual's domain is derived, not carried") {
    val latent = Create(definition(Seq("Root")), Seq(member("Root", primal)))
    val decodedLatent = latent.asJson.as[Create].fold(error => fail(error.toString), identity)
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
