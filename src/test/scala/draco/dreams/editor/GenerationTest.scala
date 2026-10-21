package draco.dreams.editor

import draco.scalatarget.DefinitionTrees._

import draco._
import draco.dreams.editor.{Generated => GenerationResponse}
import draco.dreams.user.{Create, Generate, Read, Request, Response, Update}
import draco.gendrake.Emit
import draco.generator.EmissionReceived
import io.circe.syntax._
import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.CopyOnWriteArrayList
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class GenerationTest extends AnyFunSuite {
  private def load(name: String, pkg: String): DomainType = DomainBuilder.define(name, pkg.split('.').toSeq)
  private def core = load("Draco", "draco")
  private def transform = load("GenDrake", "draco.gendrake")
  private def generator = load("Generator", "draco.generator")
  private def binding = TransformBinding(transform, Seq(generator), Seq((Emit.typeDefinition, Emit.ruleType), (EmissionReceived.typeDefinition, EmissionReceived.ruleType)))
  private val name = TypeName("Sample", Seq("example", "generation"))
  private val memberName = TypeName("Item", name.namePackage)
  private def definition = TypeDefinition(name, _domainAspect = DomainAspect(name, Seq("Item")))
  private def member = TypeDefinition(memberName, _domainAspect = DomainAspect(name))
  private def sample = Domain(definition, Seq(member))
  private def ontology = DomainBuilder.ontology(core, transform, generator, load("DrakeTarget", "draco.draketarget"), sample)
  private val transformName = TypeName("GenDrake", Seq("draco", "gendrake"))

  private def deliver(initial: DomainOntology, requests: Seq[Request], bindings: Seq[TransformBinding]): (Seq[Response], Seq[DomainOntology]) = {
    val responses = new CopyOnWriteArrayList[Response]()
    val ontologies = new CopyOnWriteArrayList[DomainOntology]()
    val system = ActorSystem(Editor.actorType(initial,
      r => { responses.add(r); () }, o => { ontologies.add(o); () }, bindings).asInstanceOf[Actor[Request]], "generationTest")
    try {
      requests.foreach(system ! _)
      val deadline = System.nanoTime() + 15.seconds.toNanos
      while (responses.size < requests.size && System.nanoTime() < deadline) Thread.sleep(20)
      assert(responses.size == requests.size, s"received ${responses.size} of ${requests.size} responses")
    } finally {
      system.terminate()
      Await.result(system.whenTerminated, 5.seconds)
    }
    (responses.asScala.toSeq, ontologies.asScala.toSeq)
  }

  test("Generate names two domains, preserves the ontology, and round-trips through its codec") {
    val request = Generate(name, transformName)
    val held = ontology
    assert(request.proposal(held) eq held)
    assert(request.subject(held).typeName == name)
    assert(request.contents(held).map(_.typeName) == Seq(memberName))
    val decoded = request.asJson.as[Generate].toOption.get
    assert(decoded.domain == name && decoded.transform == transformName)
  }

  test("GenDrake generates the held domain and every member through the Response consumer, in fresh sessions") {
    val held = ontology
    val request = Generate(name, transformName)
    val (responses, deliveries) = deliver(held, Seq(request, request, Read(name)), Seq(binding))
    val expected = Set(Drake.emit(definition), Drake.emit(member))
    responses.take(2).foreach { response =>
      val generated = response.asInstanceOf[GenerationResponse]
      assert(generated.accepted, generated.problems.map(_.message).mkString(", "))
      assert(generated.domain == name && generated.transform == transformName)
      assert(generated.emissions.size == 2)
      assert(generated.emissions.map(_.value).toSet == expected)
      val decoded = generated.asJson.as[GenerationResponse].toOption.get
      assert(decoded.emissions.map(_.value) == generated.emissions.map(_.value))
    }
    assert(responses.last.asInstanceOf[Actual].members.map(_.typeName) == Seq(memberName))
    assert(deliveries.isEmpty && held.holds(name))
  }

  test("unheld domain, unheld transform, missing binding, and duplicate bindings return Problems without emissions") {
    val unknown = TypeName("Unknown", Seq("example"))
    val cases = Seq(
      (Generate(unknown, transformName), Seq(binding)),
      (Generate(name, unknown), Seq(binding)),
      (Generate(name, transformName), Seq.empty[TransformBinding]),
      (Generate(name, transformName), Seq(binding, binding)))
    cases.foreach { case (request, bindings) =>
      val (responses, deliveries) = deliver(ontology, Seq(request), bindings)
      val response = responses.head.asInstanceOf[GenerationResponse]
      assert(!response.accepted && response.emissions.isEmpty)
      assert(response.problems.nonEmpty && deliveries.isEmpty)
    }
  }

  test("a binding must match the held transform, its super-domain, and every executable rule definition") {
    val b = binding
    val held = ontology
    assert(b.matches(held))
    def changed(d: DomainType): DomainType = Domain(TypeDefinition(d.typeDefinition.typeName,
      _dracoAspect = d.typeDefinition.dracoAspect,
      _domainAspect = DomainAspect(d.typeDefinition.domainAspect.typeName, d.typeDefinition.domainAspect.elementTypeNames :+ "Extra",
        d.typeDefinition.domainAspect.source, d.typeDefinition.domainAspect.target)), d.typeDictionary.elementTypes)
    Seq(held.including(changed(transform)), held.including(changed(generator)), held.excluding(generator.typeDefinition.typeName)).foreach { stale =>
      assert(!b.matches(stale))
      val (responses, deliveries) = deliver(stale, Seq(Generate(name, transformName)), Seq(b))
      assert(!responses.head.accepted && responses.head.asInstanceOf[GenerationResponse].emissions.isEmpty && deliveries.isEmpty)
    }
    assert(!TransformBinding(transform, Seq.empty, Seq((Emit.typeDefinition, Emit.ruleType))).matches(held))
    assert(!TransformBinding(transform, Seq(generator), Seq((Emit.typeDefinition, Emit.ruleType))).matches(held))
    assert(!TransformBinding(sample, Seq.empty, Seq.empty).matches(held), "a plain domain is not a transform")
  }

  test("acceptance never runs a binding; a later Generate uses the currently held definitions") {
    val held = ontology.excluding(name)
    val changed = TypeDefinition(memberName, _dracoAspect = DracoAspect(_elements = Seq(Fixed("label", io.circe.Json.fromString("String"), io.circe.Json.fromString("\"new\"")))),
      _domainAspect = DomainAspect(name))
    val runs = new java.util.concurrent.atomic.AtomicInteger()
    val counted = Rule[Emit](k => { runs.incrementAndGet(); Emit.ruleType.pattern(k) }, _ => ())
    val b = TransformBinding(transform, Seq(generator), Seq((Emit.typeDefinition, counted), (EmissionReceived.typeDefinition, EmissionReceived.ruleType)),
      Seq("String" -> TypeName("String")))
    val (responses, deliveries) = deliver(held, Seq(Create(definition, Seq(member)), Update(definition, Seq(changed)), Generate(name, transformName)), Seq(b))
    assert(runs.get() == 1, "only the explicit Generate executes a binding")
    assert(responses.take(2).forall(r => r.accepted && r.isInstanceOf[Actual]))
    val generated = responses.last.asInstanceOf[GenerationResponse]
    assert(generated.accepted && generated.emissions.map(_.value).toSet == Set(Drake.emit(definition), Drake.emit(changed)))
    assert(deliveries.size == 2 && !held.holds(name))
  }
  test("execution failures and incomplete emissions return Problems and leave the Editor able to answer") {
    val broken = Seq(
      Rule[Emit](_ => throw new IllegalStateException("broken binding"), _ => ()),
      Rule[Emit](_ => (), _ => ()))
    broken.foreach { rule =>
      val b = TransformBinding(transform, Seq(generator), Seq((Emit.typeDefinition, rule), (EmissionReceived.typeDefinition, EmissionReceived.ruleType)))
      assert(b.matches(ontology))
      val (responses, deliveries) = deliver(ontology, Seq(Generate(name, transformName), Read(name)), Seq(b))
      val generated = responses.head.asInstanceOf[GenerationResponse]
      assert(!generated.accepted && generated.emissions.isEmpty)
      assert(generated.problems.exists(_.message.startsWith("generation failed:")))
      assert(responses.last.accepted && deliveries.isEmpty)
    }
  }
  test("Generate resolves short names against the current held ontology and returns resolution failures as Problems") {
    val shortened = TypeDefinition(memberName,
      _dracoAspect = DracoAspect(_elements = Seq(Fixed("content", io.circe.Json.fromString("Dracon"), io.circe.Json.Null))),
      _domainAspect = DomainAspect(name))
    val withMember = ontology.including(Domain(definition, Seq(shortened)))
    val withDracon = withMember.including(load("Format", "draco.format")).including(load("Dracon", "draco.format.dracon"))
    val (good, _) = deliver(withDracon, Seq(Generate(name, transformName)), Seq(binding))
    val generated = good.head.asInstanceOf[GenerationResponse]
    assert(generated.accepted, generated.problems.map(_.message).mkString(", "))
    assert(generated.emissions.exists(_.value.contains("fix content Dracon")))
    val (bad, deliveries) = deliver(withMember, Seq(Generate(name, transformName), Read(name)), Seq(binding))
    val rejected = bad.head.asInstanceOf[GenerationResponse]
    assert(!rejected.accepted && rejected.emissions.isEmpty)
    assert(rejected.problems.exists(_.message.contains("Unknown type 'Dracon'")))
    assert(bad.last.accepted && deliveries.isEmpty)
  }

}
