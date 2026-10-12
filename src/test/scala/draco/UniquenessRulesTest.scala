package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.Uniqueness` validation rule (Dev, 2026-10-12): the
 *  integrity safeguard for accepting a domain into the ontology. A definition's
 *  key is its atomic name and its package; a name owned by more than one composed
 *  domain is a Problem, named once per definition that carries it. Type parameters
 *  are free — they compose references, not definitions — so `Dictionary(K, V)` and
 *  `Dictionary(TypeName, TypeDefinition)` both resolve to the one `draco.Dictionary`
 *  and collide with nothing. The rule joins `var m TypeDefinition` with
 *  `var o DomainOntology` and counts the domains that own `m`'s name.
 */
class UniquenessRulesTest extends AnyFunSuite {

  private def problemsFrom(ontology: DomainOntology, members: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Validation")
      Uniqueness.ruleType.pattern(knowledge)
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(members: _*)
        session.insert(Seq(ontology): _*)
        session.fire()
        collected.toList
      } finally session.close()
    } finally service.shutdown()
  }

  private def draco: DomainType = DomainBuilder.define("Draco", Seq("draco"))

  test("Uniqueness fires zero Problems on the real Draco dictionary against its own ontology") {
    val domain = draco
    val problems = problemsFrom(DomainBuilder.ontology(domain), domain.typeDictionary.elementTypes)
    assert(problems.isEmpty, s"expected no Problems; got:\n  - ${problems.map(_.message).mkString("\n  - ")}")
  }

  test("Uniqueness names a member that a second composed domain also owns, parameters notwithstanding") {
    // `draco.Shadow` claims `draco.Primal` — the same atomic name in the same package that
    // Draco already owns — and spells it with a parameter Draco's own `Primal(T)` does not
    // share. Parameters are free; the key collides.
    val shadowed = TypeDefinition(
      TypeName("Primal", _namePackage = Seq("draco"), _typeParameters = Seq(io.circe.Json.fromString("Int"))),
      _domainAspect = DomainAspect(TypeName("Shadow", _namePackage = Seq("draco"))))
    val shadow: DomainType = Domain[Any](
      TypeDefinition(TypeName("Shadow", _namePackage = Seq("draco")),
        _domainAspect = DomainAspect(TypeName("Shadow", _namePackage = Seq("draco")), Seq("Primal"))),
      Seq(shadowed))
    val core = draco
    val ontology = DomainBuilder.ontology(core).including(shadow)

    val problems = problemsFrom(ontology, core.typeDictionary.elementTypes :+ shadowed)
    assert(problems.nonEmpty && problems.forall(_.subject.name == "Primal"),
      s"every Problem should name Primal; got ${problems.map(p => (p.subject.namePath, p.message))}")
    assert(problems.size == 2, "each definition carrying the collided name is reported once")

    val distinct = DomainBuilder.ontology(core).including(Domain[Any](
      TypeDefinition(TypeName("Shadow", _namePackage = Seq("example")),
        _domainAspect = DomainAspect(TypeName("Shadow", _namePackage = Seq("example")), Seq("Primal"))),
      Seq(TypeDefinition(TypeName("Primal", _namePackage = Seq("example")),
        _domainAspect = DomainAspect(TypeName("Shadow", _namePackage = Seq("example")))))))
    assert(problemsFrom(distinct, distinct.keys.toSeq.flatMap(_.typeDictionary.elementTypes)).isEmpty,
      "the same atomic name in another package is another definition")
  }
}
