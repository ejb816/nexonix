package draco

import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** Behavioral test — the generated `Draco` actor validating the foundation at
 *  runtime: the actor-shaped mirror of `FoundationValidationTest`.
 *
 *  Where that test builds a session by hand, here the *generated* actor owns the
 *  whole membrane: `start` stands up the exhaustive-evaluation session and seeds
 *  the findings collection (the `problems` construction parameter), `message`
 *  inserts each received fact and fires — stateful accumulation, one collapse per
 *  message — and `signal` closes the session at PostStop. The actor's rule set is
 *  DERIVED from its own domain dictionary: Completeness, SelfDeclaration,
 *  DerivationResolvable, and the findings consumer CollectProblems (now a real
 *  Draco member — a produced Problem needs a consuming rule to have a place in
 *  working memory) are picked up as members, nothing declared twice.
 *
 *  Facts arrive as messages: every first-party domain as a `DomainType`, every
 *  member as a `TypeDefinition` — both are `DracoType`, the actor's message type.
 */
class DracoActorTest extends AnyFunSuite {

  /** The first-party domains that make up the foundation — mirrors FoundationValidationTest. */
  private val foundationDomains: Seq[(String, Seq[String])] = Seq(
    ("Draco",  Seq("draco")),
    ("Base",   Seq("draco", "base")),
    ("Primes", Seq("draco", "primes"))
  )

  private def loadFoundation(): (Seq[DomainType], Seq[TypeDefinition]) = {
    val built: Seq[DomainType] = foundationDomains.map { case (n, p) => DomainBuilder.define(n, p) }
    val members: Seq[TypeDefinition] = built.flatMap(_.typeDictionary.elementTypes)
    (built, members)
  }

  /** Spawn a Draco actor, send it every fact as a message, let it collapse each
   *  in turn, stop it, and return the findings it accumulated. */
  private def validateViaActor(name: String, facts: Seq[DracoType]): Seq[Problem] = {
    val problems = new java.util.ArrayList[Problem]()
    val system = ActorSystem(Draco.actorType(problems).asInstanceOf[Actor[DracoType]], name)
    facts.foreach(system ! _)
    Thread.sleep(1000)
    system.terminate()
    Await.result(system.whenTerminated, 5.seconds)
    problems.asScala.toSeq
  }

  test("the Draco actor validates the loaded foundation clean") {
    val (domains, members) = loadFoundation()
    assert(members.nonEmpty, "the foundation dictionary should be populated")

    val problems = validateViaActor("dracoValidateClean", domains ++ members)
    assert(problems.isEmpty,
      s"the actor should find the foundation self-consistent; got:\n  - " +
        problems.map(p => s"${p.subject.name}: ${p.message}").mkString("\n  - "))
  }

  test("the Draco actor reports an injected stub member — one finding, naming it") {
    val (domains, members) = loadFoundation()
    // A declared-but-unauthored member: loading a name with no JSON behind it
    // yields the minimal stub definition, which Completeness must flag.
    val ghost = TypeLoader.loadType(TypeName("Nonexistent", _namePackage = Seq("draco")))

    val problems = validateViaActor("dracoValidateBroken", (domains ++ members) :+ ghost)
    assert(problems.size == 1,
      s"exactly one finding expected; got:\n  - " +
        problems.map(p => s"${p.subject.name}: ${p.message}").mkString("\n  - "))
    assert(problems.head.subject.name == "Nonexistent")
  }
}
