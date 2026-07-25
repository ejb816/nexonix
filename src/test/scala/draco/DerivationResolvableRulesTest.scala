package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.DerivationResolvable` validation rule.
 *
 *  A member may name draco-internal ancestors in its `dracoAspect.derivation`;
 *  each must resolve to a real definition, not the empty (stub) TypeDefinition a
 *  missing JSON yields. This rule transcribes `DomainBuilder.validate` check 3.
 *
 *  The check is expressed as a load-in-condition: the rule binds `var m
 *  TypeDefinition` and its condition asks whether `m` derives any draco-internal
 *  ancestor that `TypeLoader.loadType`s to a stub. That condition is genuine
 *  host-glue — a classpath loader call plus Scala collection ops — so, unlike the
 *  native aspect-predicate conditions of Completeness/SelfDeclaration, it is
 *  authored as a host-opaque string value (the legitimate `isString` mode), the
 *  same category the `ctx.insert` actions occupy. External (non-`draco`)
 *  supertypes are out of scope and filtered out, exactly as check 3 does.
 *
 *  Only DerivationResolvable + CollectProblems are needed here: the rule's own
 *  `forEach(TypeDefinition)` supplies the working-memory node for member facts,
 *  and CollectProblems gives Problem its node and gathers the findings.
 */
class DerivationResolvableRulesTest extends AnyFunSuite {

  private def problemsFrom(members: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Validation")
      DerivationResolvable.ruleType.pattern.accept(knowledge)
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(members: _*)
        session.fire()
        collected.toList
      } finally session.close()
    } finally service.shutdown()
  }

  test("DerivationResolvable fires zero Problems on the real Draco dictionary") {
    val members: Seq[TypeDefinition] =
      DomainBuilder.define("Draco", Seq("draco")).typeDictionary.elementTypes
    assert(members.nonEmpty, "Draco dictionary should be populated")

    val problems = problemsFrom(members)
    assert(problems.isEmpty,
      s"expected no Problems on the real dictionary; got:\n  - " +
        problems.map(_.message).mkString("\n  - "))
  }

  test("DerivationResolvable fires exactly one Problem for a member with a dangling derivation") {
    // A member whose derivation names a draco-internal type with no JSON on disk —
    // the type resolves to a stub, so the derivation dangles. The member itself is
    // NOT a stub (it carries a dracoAspect), so it is a legitimate derivation-check
    // subject rather than a Completeness one.
    val dangler: TypeDefinition = TypeDefinition(
      TypeName("Dangler", _namePackage = Seq("draco")),
      _dracoAspect = DracoAspect(
        _derivation = Seq(TypeName("Nonexistent", _namePackage = Seq("draco")))))

    val problems = problemsFrom(Seq(dangler))
    assert(problems.size == 1, s"expected exactly one Problem; got ${problems.size}")
    assert(problems.head.subject.name == "Dangler",
      s"Problem should name the member with the dangling derivation; got ${problems.head.subject.name}")
  }
}
