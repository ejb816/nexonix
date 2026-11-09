package draco

import draco.scalatarget.DefinitionTrees._

import io.circe.Json
import org.evrete.KnowledgeService
import org.evrete.api.{ActivationMode, Knowledge, RhsContext}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer

/** Firing proof for the `draco.FactDisjointness` validation rule (Dev, 2026-11-09; GitHub #63,
 *  constraint 4). The rules engine resolves a fact's type by walking its supertypes, and when
 *  more than one declared type fits it resolves NEITHER and skips the insert — a logged warning,
 *  nothing thrown (`SubtypeFactVisibilityTest`, "CO-DECLARATION DROPS THE FACT"). So no two
 *  rules of one domain may declare fact types related by derivation. The rule joins
 *  `var d DomainType` with `var o DomainOntology`: a rule member's pattern variables declare its
 *  fact types, read as identities through the member's resolution record (a dotted name is its
 *  path, a bare name is looked up in `uses`, else own-package); a Problem names the domain and
 *  every pair in which one declared type is an ancestor of another through the ontology's
 *  derivation edges. The canonical corpus is safe by shape — siblings per medium, a disjoint
 *  taxonomy in Draco — and this rule turns that shape into a guarantee.
 */
class FactDisjointnessRulesTest extends AnyFunSuite {

  private def problemsFrom(ontology: DomainOntology): Seq[Problem] = {
    val service: KnowledgeService = new KnowledgeService()
    val collected = ListBuffer.empty[Problem]
    try {
      val knowledge: Knowledge = service.newKnowledge("Validation")
      FactDisjointness.ruleType.pattern(knowledge)
      knowledge
        .builder()
        .newRule("draco.CollectProblems")
        .forEach("$p", classOf[Problem])
        .execute((ctx: RhsContext) => collected += ctx.get[Problem]("$p"))
        .build()
      val session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS)
      try {
        session.insert(ontology.keys.toSeq: _*)
        session.insert(Seq(ontology): _*)
        session.fire()
        collected.toList
      } finally session.close()
    } finally service.shutdown()
  }

  private val pkg = Seq("example", "lint")
  private def name(n: String): TypeName = TypeName(n, _namePackage = pkg)
  private def domainName: TypeName = name("Lint")

  /** A rule member declaring one fact type, spelled as given (bare or dotted). */
  private def rule(n: String, factType: String): TypeDefinition = TypeDefinition(
    name(n),
    _domainAspect = DomainAspect(domainName),
    _ruleAspect = draco.drake.Present(RuleAspect(
      _pattern = Pattern(_variables = Seq(Variable("x", Json.fromString(factType)))))))

  private def plain(n: String, parent: Option[String] = None): TypeDefinition = TypeDefinition(
    name(n),
    _dracoAspect = DracoAspect(
      _derivation = parent.toSeq.map(p => TypeName.write(name(p))),
      _elements = Seq(Fixed("v", Json.fromString("Int")))),
    _domainAspect = DomainAspect(domainName))

  private def lint(members: Seq[TypeDefinition]): DomainType = Domain[Any](
    TypeDefinition(domainName, _domainAspect = DomainAspect(domainName, members.map(_.typeName.name))),
    members)

  test("FactDisjointness fires zero Problems over the canonical ontology, which is safe by shape") {
    val canonical = DomainBuilder.ontology(DomainBuilder.define("Draco", Seq("draco")))
    assert(canonical.relatedFactTypes(canonical.keys.head).isEmpty)
    val problems = problemsFrom(canonical)
    assert(problems.isEmpty, s"expected no Problems; got:\n  - ${problems.map(_.message).mkString("\n  - ")}")
  }

  test("two rules declaring sibling fact types pass; a type and its parent are refused, naming both") {
    val siblings = lint(Seq(plain("A"), plain("B"), rule("RuleA", "A"), rule("RuleB", "example.lint.B")))
    assert(problemsFrom(DomainOntology(Seq(siblings))).isEmpty, "siblings are disjoint")

    // C derives A; RuleA declares A bare (own package), RuleC declares C in full.
    val related = lint(Seq(plain("A"), plain("C", Some("A")), rule("RuleA", "A"), rule("RuleC", "example.lint.C")))
    val ontology = DomainOntology(Seq(related))
    assert(ontology.ancestors(name("C")).map(_.namePath) == Seq("example.lint.A"), "the edge is projected")
    val problems = problemsFrom(ontology)
    assert(problems.size == 1, s"one Problem per domain; got ${problems.map(_.message)}")
    assert(problems.head.subject == domainName, "the Problem names the domain")
    assert(problems.head.message.contains("example.lint.C derives example.lint.A"), problems.head.message)
  }

  test("a bare fact type resolves through the member's resolution record before the own package") {
    // RuleD sits in example.lint but declares `D` recorded as example.other.D — not the own-package D.
    val other = TypeName("D", _namePackage = Seq("example", "other"))
    val ruleD = TypeDefinition(name("RuleD"), _uses = Seq(other), _domainAspect = DomainAspect(domainName),
      _ruleAspect = draco.drake.Present(RuleAspect(_pattern = Pattern(_variables = Seq(Variable("x", Json.fromString("D")))))))
    assert(TypeDefinition.factTypes(ruleD) == Seq(other), "the record wins over the own package")
    assert(TypeDefinition.factTypes(rule("RuleA", "A")) == Seq(name("A")), "an unrecorded bare name is own-package")
    assert(TypeDefinition.factTypes(rule("RuleB", "example.lint.B")) == Seq(name("B")), "a dotted name is its path")
    assert(TypeDefinition.factTypes(plain("A")).isEmpty, "a plain type declares no fact types")
  }
}
