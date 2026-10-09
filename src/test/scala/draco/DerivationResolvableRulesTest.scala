package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.DerivationResolvable` validation rule.
 *
 *  A member may name ancestors in its `dracoAspect.derivation` (a FOREIGN parent is a
 *  type form there, never a name — `DracoAspect.parents` returns only the named ones);
 *  each named ancestor must be DEFINED by the ontology the member is being validated into. The
 *  rule joins two facts — `var m TypeDefinition` and `var o DomainOntology` —
 *  and asks whether `m` derives any named ancestor that `o.defines`
 *  does not know, by name and package (a reference may spell parameters; a
 *  definition is keyed bare). Until 2026-10-09 the condition loaded each ancestor
 *  through `TypeLoader` and looked only at `draco`-package parents, so a candidate
 *  domain was judged by whatever the classpath happened to carry; the ontology fact makes the reference set
 *  explicit and lets a member resolve against a sibling supplied in the same
 *  candidate, which no resource holds.
 *
 *  DerivationResolvable + CollectProblems are enough here: the rule's own
 *  `forEach` supplies the working-memory nodes for members and the ontology,
 *  and CollectProblems gives Problem its node and gathers the findings.
 */
class DerivationResolvableRulesTest extends AnyFunSuite {

  private def problemsFrom(ontology: DomainOntology, members: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Validation")
      DerivationResolvable.ruleType.pattern(knowledge)
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

  private def dangler(parent: TypeName): TypeDefinition = TypeDefinition(
    TypeName("Dangler", _namePackage = Seq("draco")),
    _dracoAspect = DracoAspect(_derivation = Seq(TypeName.encoder(parent))))

  test("DerivationResolvable fires zero Problems on the real Draco dictionary against its own ontology") {
    val domain = draco
    val members: Seq[TypeDefinition] = domain.typeDictionary.elementTypes
    assert(members.nonEmpty, "Draco dictionary should be populated")

    val problems = problemsFrom(DomainBuilder.ontology(domain), members)
    assert(problems.isEmpty,
      s"expected no Problems on the real dictionary; got:\n  - " +
        problems.map(_.message).mkString("\n  - "))
  }

  test("DerivationResolvable fires exactly one Problem for a member whose derivation the ontology does not define") {
    // The member itself is NOT a stub (it carries a dracoAspect), so it is a
    // legitimate derivation-check subject rather than a Completeness one.
    val problems = problemsFrom(DomainBuilder.ontology(draco),
      Seq(dangler(TypeName("Nonexistent", _namePackage = Seq("draco")))))
    assert(problems.size == 1, s"expected exactly one Problem; got ${problems.size}")
    assert(problems.head.subject.name == "Dangler",
      s"Problem should name the member with the dangling derivation; got ${problems.head.subject.name}")
  }

  test("DerivationResolvable resolves against the ontology, not the classpath — a parent no resource holds") {
    // `Root` exists only in memory: no JSON anywhere names it. A member deriving
    // from it resolves iff the ontology defines it — true when the candidate
    // domain carrying Root is composed in, false against Draco alone.
    val pkg = Seq("draco", "candidate")
    val root = TypeDefinition(TypeName("Root", _namePackage = pkg),
      _domainAspect = DomainAspect(TypeName("Candidate", _namePackage = pkg)))
    val leaf = TypeDefinition(TypeName("Leaf", _namePackage = pkg),
      _dracoAspect = DracoAspect(_derivation = Seq(TypeName.encoder(root.typeName))),
      _domainAspect = DomainAspect(TypeName("Candidate", _namePackage = pkg)))
    val candidate: DomainType = Domain[Any](
      TypeDefinition(TypeName("Candidate", _namePackage = pkg),
        _domainAspect = DomainAspect(TypeName("Candidate", _namePackage = pkg), Seq("Root", "Leaf"))),
      Seq(root, leaf))
    assert(!DomainBuilder.isStub(candidate.typeDictionary(root.typeName)), "core populates from supplied members")

    val resolved = problemsFrom(DomainBuilder.ontology(draco, candidate), Seq(leaf))
    assert(resolved.isEmpty, s"Leaf should resolve through the candidate's own ontology; got ${resolved.map(_.message)}")

    val unresolved = problemsFrom(DomainBuilder.ontology(draco), Seq(leaf))
    assert(unresolved.size == 1 && unresolved.head.subject.name == "Leaf",
      s"without the candidate in the ontology, Leaf's parent is undefined; got ${unresolved.map(_.message)}")
  }
}
