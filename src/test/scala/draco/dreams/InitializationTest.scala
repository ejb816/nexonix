package draco.dreams

import draco._
import draco.dreams.user.{Create, Latent}
import io.circe.syntax._
import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration._

/** The endogenous initialization client (2026-10-12): draco acting as its own first User.
  * `Initialization` takes the canonical domains as Latents — supplied, not loaded: the
  * consuming project owns persistence, and these tests supply them from the resource
  * tree through DomainBuilder — and ORDERS them from the definitions themselves (Dev:
  * "the computed dependency order from an empty ontology"): a domain follows every
  * supplied domain its named parents reach, wave by wave, the supplied order kept within
  * a wave; a parent no supplied domain owns is not a constraint (the Editor will say so),
  * and a cycle cannot hang the computation. It mints the Editor over an EMPTY ontology
  * with its own two consumers, sends the ordered Latents, notes every Response and keeps
  * the last ontology delivered — so after the run it holds draco's innate ontology,
  * including the definition of itself. Nothing is hand-listed: adding a domain to the
  * corpus never means editing the client. */
class InitializationTest extends AnyFunSuite {
  private def read(path: String): String = new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  private val stem = "draco/dreams/Initialization"
  private def definition: TypeDefinition = io.circe.parser.parse(read(s"src/mods/resources/$stem.json"))
    .flatMap(_.as[TypeDefinition]).fold(error => fail(error.toString), identity)

  test("Initialization Drake and JSON are canonical mutual projections") {
    val source = read(s"src/mods/resources/$stem.drake")
    val parsed = Drake.parse(source)
    assert(parsed.asJson == definition.asJson)
    assert(Drake.emit(definition) == source)
    assert(Drake.emit(parsed) == source)
  }

  test("Initialization Scala is generated from its staged definition; it is the Dreams domain's first member") {
    assert(DracoGenerator.generate(definition) == read(s"src/mods/scala/$stem.scala"))
    assert(!Files.exists(Paths.get(s"src/main/scala/$stem.scala")), "duplicate main-tier scaffold")
    assert(Initialization.typeDefinition.typeName == definition.typeName)
    assert(Initialization.typeDefinition.domainAspect.typeName == Dreams.typeDefinition.typeName)
    assert(Dreams.elementTypeNames == Seq("Initialization"))
  }

  // --- the canonical domains, supplied in an order the computation must undo ---

  private val canonical: Seq[(String, Seq[String])] = Seq(
    ("Editor", Seq("draco", "dreams", "editor")),
    ("User", Seq("draco", "dreams", "user")),
    ("Dreams", Seq("draco", "dreams")),
    ("Service", Seq("draco", "service")),
    ("GenDrake", Seq("draco", "gendrake")),
    ("GenScala", Seq("draco", "genscala")),
    ("ScalaTarget", Seq("draco", "scalatarget")),
    ("DrakeTarget", Seq("draco", "draketarget")),
    ("Carrier", Seq("draco", "generator", "carrier")),
    ("Generator", Seq("draco", "generator")),
    ("Drake", Seq("draco", "drake")),
    ("Rete", Seq("draco", "rete")),
    ("XML", Seq("draco", "format", "xml")),
    ("Dracon", Seq("draco", "format", "dracon")),
    ("Format", Seq("draco", "format")),
    ("Primes", Seq("draco", "primes")),
    ("Base", Seq("draco", "base")),
    ("Draco", Seq("draco"))
  )
  private def supplied: Seq[Create] = canonical.map { case (name, pkg) =>
    val domain = DomainBuilder.define(name, pkg)
    Create(domain.typeDefinition, domain.typeDictionary.elementTypes)
  }
  private def name(latent: Latent): String = latent.definition.typeName.name

  test("the order is computed from the definitions: every domain follows the domains its parents reach, Draco first") {
    val client = Initialization(supplied)
    val ordered = client.ordered
    val names = ordered.map(name)
    assert(names.toSet == canonical.map(_._1).toSet && names.size == canonical.size, "nothing lost, nothing duplicated")
    assert(names.head == "Draco", s"every loaded definition is rooted at DracoType, so Draco alone forms the first wave; got $names")
    assert(names.indexOf("Base") > names.indexOf("Draco"), "Cardinal derives Primal")
    assert(names.indexOf("Editor") > names.indexOf("User"), "Actual derives Response")
    ordered.zipWithIndex.foreach { case (latent, position) =>
      client.parents(latent).filter(p => client.domains.exists(d => client.owns(d, p))).foreach { parent =>
        val owner = ordered.indexWhere(d => client.owns(d, parent))
        assert(owner < position, s"${name(latent)} at $position derives ${parent.namePath}, owned by ${name(ordered(owner))} at $owner")
      }
    }
  }

  test("a parent no supplied domain owns is not a constraint, and a cycle cannot hang: the remainder keeps its supplied order") {
    def domainOf(domain: TypeName, member: String, parent: TypeName): Create = Create(
      TypeDefinition(domain, _domainAspect = DomainAspect(domain, Seq(member))),
      Seq(TypeDefinition(TypeName(member, domain.namePackage),
        _dracoAspect = DracoAspect(_derivation = Seq(TypeName.encoder(parent))),
        _domainAspect = DomainAspect(domain))))
    val a = TypeName("A", Seq("example", "a"))
    val b = TypeName("B", Seq("example", "b"))
    val c = TypeName("C", Seq("example", "c"))
    val cycleA = domainOf(a, "X", TypeName("Y", b.namePackage))
    val cycleB = domainOf(b, "Y", TypeName("X", a.namePackage))
    val dangling = domainOf(c, "Z", TypeName("Nowhere", Seq("nowhere")))
    val client = Initialization(Seq(cycleA, cycleB, dangling))
    assert(client.ordered.map(name) == Seq("C", "A", "B"),
      "C is ready at once (its parent is nobody's); A and B never are, and follow in the order supplied")
    assert(client.parents(dangling).map(_.name) == Seq("Nowhere"), "the dangling parent is reported, not hidden; the Editor refuses it")
  }

  test("the endogenous run: from an empty ontology, every canonical domain is accepted and the client holds draco's innate ontology") {
    val client = Initialization(supplied)
    assert(client.initial.isEmpty && client.ontology.isEmpty && client.responses.isEmpty && !client.complete)
    val system = ActorSystem(client.editorType.asInstanceOf[Actor[draco.dreams.user.Request]], "initialization")
    try {
      client.run(system ! _)
      val deadline = System.nanoTime() + 120.seconds.toNanos
      while (!client.complete && System.nanoTime() < deadline) Thread.sleep(50)
    } finally {
      system.terminate()
      Await.result(system.whenTerminated, 10.seconds)
    }
    assert(client.complete, s"${client.responses.size} of ${client.domains.size} responses arrived")
    assert(client.accepted, s"refused: ${client.responses.filterNot(_.accepted).flatMap(_.problems.map(p => s"${p.subject.namePath}: ${p.message}"))}")
    val ontology = client.ontology
    assert(ontology.size == canonical.size)
    assert(client.domains.forall(l => ontology.defines(l.definition.typeName)))
    assert(ontology.defines(Initialization.typeDefinition.typeName), "the ontology the client built defines the client: the endogenous user")
    val base = ontology.keys.find(_.typeDefinition.typeName.name == "Base").get
    assert(ontology.dependencies(base).map(_.typeDefinition.typeName.name).toSet == Set("Draco"))
    assert(ontology.derivations.nonEmpty)
    assert(client.initial.isEmpty, "the empty starting ontology is untouched; the state moved through the consumer")
  }

  test("Initialization round-trips its supplied domains through JSON; its state is not carried") {
    val client = Initialization(supplied.take(2))
    val json = client.asJson
    assert(json.hcursor.downField("ontology").failed && json.hcursor.downField("responses").failed)
    val decoded = json.as[Initialization].fold(error => fail(error.toString), identity)
    assert(decoded.domains.map(name) == Seq("Editor", "User"))
    assert(decoded.ontology.isEmpty && decoded.responses.isEmpty)
  }
}
