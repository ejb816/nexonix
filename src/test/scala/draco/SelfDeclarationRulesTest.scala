package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.SelfDeclaration` validation rule — and, with it, a
 *  proof of the fact taxonomy that makes domain-vs-member validation possible.
 *
 *  A domain is well-formed only if it self-declares: its `domainAspect.typeName`
 *  points back at its own `typeName` (the self-loop that discriminates a domain
 *  from a leaf — see the DomainAspect notes). A *member* also carries a
 *  `domainAspect.typeName`, but it points at *its domain*, not itself. So a naive
 *  self-declaration rule bound to a bare `TypeDefinition` would false-positive on
 *  every member.
 *
 *  The fix is a fact taxonomy, not a cleverer condition: domains are inserted as
 *  `DomainType` facts and members as `TypeDefinition` facts. Because `DomainType`
 *  and `TypeDefinition` are disjoint in the class hierarchy (both extend
 *  `DracoType`, neither derives the other), `SelfDeclaration`'s
 *  `forEach("$d", classOf[DomainType])` structurally cannot match a member — so the
 *  false-positive is dissolved at the type level, before any condition runs.
 *
 *  Both validation rules share one session here — the nucleus of the Draco actor's
 *  eventual session — and their taxonomy keeps them from treading on each other:
 *  `SelfDeclaration` binds `DomainType` (domains only); `Completeness` binds
 *  `TypeDefinition` (members only). Co-residency is not incidental: Completeness's
 *  `forEach(TypeDefinition)` is what supplies the working-memory node that lets
 *  member facts be inserted at all (a produced/consumed type needs an LHS
 *  reference — see CompletenessRulesTest). `CollectProblems` gives `Problem` its
 *  node and gathers the findings.
 */
class SelfDeclarationRulesTest extends AnyFunSuite {

  /** Run the Draco validation battery (Completeness + SelfDeclaration) over a set
   *  of domain-role facts and member facts, and return every Problem produced. */
  private def validate(domains: Seq[DomainType], members: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Validation")
      Completeness.ruleType.pattern.accept(knowledge)
      SelfDeclaration.ruleType.pattern.accept(knowledge)
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(domains: _*)
        session.insert(members: _*)
        session.fire()
        collected.toList
      } finally session.close()
    } finally service.shutdown()
  }

  test("SelfDeclaration passes the real Draco domain and does not false-positive on its members") {
    val draco: DomainType = DomainBuilder.define("Draco", Seq("draco"))
    val members: Seq[TypeDefinition] = draco.typeDictionary.elementTypes
    assert(members.nonEmpty, "Draco dictionary should be populated")

    // The domain (DomainType) plus every member (TypeDefinition) go into one
    // session; a correct taxonomy means SelfDeclaration sees only the domain and
    // finds it well-formed, while the members — which each carry
    // domainAspect.typeName -> Draco — never trigger it.
    val problems = validate(Seq(draco), members)
    assert(problems.isEmpty,
      s"expected no Problems on the real Draco domain + members; got:\n  - " +
        problems.map(_.message).mkString("\n  - "))
  }

  test("SelfDeclaration fires exactly one Problem for a domain that misdeclares itself") {
    // A DomainType whose domainAspect.typeName (Different) does not match its own
    // typeName (Wrong) — the shape DomainBuilder.validate check 1 rejects.
    val misdeclared: DomainType = Domain[Any](
      TypeDefinition(
        TypeName("Wrong", _namePackage = Seq("draco")),
        _domainAspect = DomainAspect(TypeName("Different", _namePackage = Seq("draco")))))

    val problems = validate(Seq(misdeclared), Seq.empty)
    assert(problems.size == 1, s"expected exactly one Problem; got ${problems.size}")
    assert(problems.head.subject.name == "Wrong",
      s"Problem should name the misdeclaring domain; got ${problems.head.subject.name}")
  }
}
