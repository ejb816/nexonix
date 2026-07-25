package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Applies the foundation validation rules — Completeness, SelfDeclaration,
 *  DerivationResolvable — consistently to the foundation ITSELF: the draco domain
 *  dictionary, loaded from its existing JSON.
 *
 *  The three RulesTests each proved one rule fires in isolation. This is the
 *  culmination: all three run together, as one session, over the *whole* loaded
 *  DracoDomainDictionary — every first-party domain as a `DomainType` fact and
 *  every member as a `TypeDefinition` fact. It is the inferential mirror of
 *  `DomainBuilderTest`'s procedural `DomainBuilder.validate`: where that walks the
 *  dictionary and collects problem strings, this fires rules and collects `Problem`
 *  facts, and both must agree that the foundation is self-consistent (zero).
 *
 *  No source generation, no target compiling — this is a runtime self-validation of
 *  the stable foundation, the prerequisite for later using that foundation as the
 *  fixed reference when a user edits their own domain dictionary.
 */
class FoundationValidationTest extends AnyFunSuite {

  /** The first-party domains that make up the foundation — mirrors DomainBuilderTest. */
  private val foundationDomains: Seq[(String, Seq[String])] = Seq(
    ("Draco",  Seq("draco")),
    ("Base",   Seq("draco", "base")),
    ("Primes", Seq("draco", "primes"))
  )

  /** Run the full foundation battery over the given domain-role and member facts,
   *  returning every Problem the rules produce. */
  private def validate(domains: Seq[DomainType], members: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Foundation")
      Completeness.ruleType.pattern.accept(knowledge)
      SelfDeclaration.ruleType.pattern.accept(knowledge)
      DerivationResolvable.ruleType.pattern.accept(knowledge)
      // A Problem fact needs a working-memory node BEFORE the rules insert it, and
      // fire(Class, Consumer) does NOT allocate it (the insert happens mid-fire, before
      // the harvest argument applies), so a Problem-consuming rule both allocates the
      // node and gathers the findings. One-shot validation still uses a STATELESS
      // session, CONTINUOUS so every independent producing rule fires in one pass
      // (DEFAULT fires only the first per generation — see Evrete doAgendaDefault).
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatelessSession(ActivationMode.CONTINUOUS)
      session.insert(domains: _*)
      session.insert(members: _*)
      session.fire()
      collected.toList
    } finally service.shutdown()
  }

  private def loadFoundation(): (Seq[DomainType], Seq[TypeDefinition]) = {
    val built: Seq[DomainType] = foundationDomains.map { case (n, p) => DomainBuilder.define(n, p) }
    val members: Seq[TypeDefinition] = built.flatMap(_.typeDictionary.elementTypes)
    (built, members)
  }

  test("the draco foundation validates itself clean (all rules, full loaded dictionary)") {
    val (domains, members) = loadFoundation()
    assert(members.nonEmpty, "the foundation dictionary should be populated")

    val problems = validate(domains, members)
    assert(problems.isEmpty,
      s"the foundation should self-validate; got:\n  - " +
        problems.map(p => s"${p.subject.name}: ${p.message}").mkString("\n  - "))
  }

  test("the foundation rules catch a broken foundation — one Problem per rule") {
    val (domains, members) = loadFoundation()

    // One deliberately-broken fact per rule, inserted alongside the real foundation.
    val stubMember: TypeDefinition =                       // Completeness
      TypeDefinition(TypeName("Ghost", _namePackage = Seq("draco")))
    val danglerMember: TypeDefinition =                    // DerivationResolvable
      TypeDefinition(
        TypeName("Dangler", _namePackage = Seq("draco")),
        _dracoAspect = DracoAspect(
          _derivation = Seq(TypeName("Nonexistent", _namePackage = Seq("draco")))))
    val misdeclaredDomain: DomainType =                    // SelfDeclaration
      Domain[Any](
        TypeDefinition(
          TypeName("Wrong", _namePackage = Seq("draco")),
          _domainAspect = DomainAspect(TypeName("Different", _namePackage = Seq("draco")))))

    val problems = validate(domains :+ misdeclaredDomain, members ++ Seq(stubMember, danglerMember))
    val subjects = problems.map(_.subject.name).toSet
    assert(problems.size == 3, s"expected exactly three Problems; got ${problems.size}: $subjects")
    assert(subjects == Set("Ghost", "Dangler", "Wrong"),
      s"each rule should catch its broken fact; got $subjects")
  }
}
