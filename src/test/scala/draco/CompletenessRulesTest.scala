package draco

import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.Completeness` validation rule.
 *
 *  `Completeness` compiles and gates green as a Draco domain member, but
 *  compiling a rule is not the same as firing it: the four `where` conditions
 *  (`draco.Completeness.w0($td)` … `w3`) are strings compiled by Evrete's
 *  runtime Java compiler and, until now, had never been exercised. This test
 *  stands up a bare Evrete `Knowledge`/`Session` — no Pekko actor — carrying
 *  only the Completeness rule, feeds it real facts, and proves both directions
 *  of the mechanism:
 *
 *   - inserting the real, populated Draco dictionary members yields ZERO
 *     `Problem`s (every member resolves to authored content);
 *   - inserting a single deliberately-stubbed member (a `TypeName` with no JSON,
 *     which loads as an empty `TypeDefinition`) yields exactly ONE `Problem`.
 *
 *  The structural pattern — an `object` with a companion `trait` whose public
 *  methods back the `where` clauses — is the same one `RemoveCompositeNumbers`
 *  uses and `AddAndRemoveRulesTest` already fires green, so the object-method
 *  condition resolution is known to work; this test pins it for the validation
 *  rule specifically and for the aspect-`isEmpty` predicate it evaluates.
 */
class CompletenessRulesTest extends AnyFunSuite {

  /** Build a Knowledge carrying the Completeness rule plus a minimal
   *  Problem-consuming rule, insert `facts`, fire once, and return every
   *  `Problem` the validation rule produced.
   *
   *  The consuming rule is not incidental scaffolding: Completeness's RHS
   *  `ctx.insert(Problem(...))` produces `Problem` as a first-class working-memory
   *  fact, and Evrete only allocates working memory for a type that some rule
   *  references in its LHS. A `Problem` that no rule consumes has no home in the
   *  session — the insert NPEs in `getTypeMemory`. So a session that produces
   *  Problems must also host something that consumes them; here that consumer both
   *  gives `Problem` its memory node and collects the findings, standing in for the
   *  reporting/aggregation path the Draco actor will eventually own. */
  private def problemsFrom(facts: Seq[TypeDefinition]): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Completeness")
      Completeness.ruleType.pattern.accept(knowledge)
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(facts: _*)
        session.fire()
        collected.toList
      } finally session.close()
    } finally service.shutdown()
  }

  test("Completeness fires zero Problems on the real Draco dictionary") {
    val members: Seq[TypeDefinition] =
      DomainBuilder.define("Draco", Seq("draco")).typeDictionary.elementTypes
    assert(members.nonEmpty, "Draco dictionary should be populated")

    val problems = problemsFrom(members)
    assert(problems.isEmpty,
      s"expected no Problems on the real dictionary; got:\n  - " +
        problems.map(_.message).mkString("\n  - "))
  }

  test("Completeness fires exactly one Problem for a deliberately-stubbed member") {
    val stub: TypeDefinition =
      TypeLoader.loadType(TypeName("Nonexistent", _namePackage = Seq("draco")))
    assert(DomainBuilder.isStub(stub),
      "a TypeName with no JSON on disk should load as an empty (stub) TypeDefinition")

    val problems = problemsFrom(Seq(stub))
    assert(problems.size == 1, s"expected exactly one Problem; got ${problems.size}")
    assert(problems.head.subject.name == "Nonexistent",
      s"Problem should name the stubbed member; got ${problems.head.subject.name}")
  }
}
