package draco

import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

/** The domain ontology's first explicit relationship: DERIVATION (Dev, 2026-10-09).
 *
 *  An edge is a `Derivation` — child, parent, and its provenance `declared`. The
 *  ontology PROJECTS its edges from the composed domains at composition, from each
 *  domain's own definition and every member's named parents; nothing is re-authored,
 *  so the edges cannot drift from the aspects they come from. Declared edges are the
 *  only kind today; inferred derivation (a type carrying every element of another) is
 *  a later rule, and `declared` is where it will say so.
 *
 *  Over the edges the ontology answers `dependencies(domain)` — the other domains a
 *  domain's edges reach — and `including(domain)` composes one more domain without
 *  touching the ontology it was called on. The first inter-domain relationship the
 *  ontology can state is Base's dependence on Draco, through Cardinal → Primal.
 */
class DomainOntologyTest extends AnyFunSuite {

  private def draco: DomainType = DomainBuilder.define("Draco", Seq("draco"))
  private def base: DomainType = DomainBuilder.define("Base", Seq("draco", "base"))
  private val primal = TypeName("Primal", Seq("draco"))

  test("Derivation carries child, parent and provenance; declared by default; round-trips through JSON") {
    val edge = Derivation(TypeName("Cardinal", Seq("draco", "base")), primal)
    assert(edge.declared)
    assert(!Derivation(edge.child, edge.parent, _declared = false).declared)
    val decoded = edge.asJson.as[Derivation].fold(error => fail(error.toString), identity)
    assert(decoded.child == edge.child && decoded.parent == edge.parent && decoded.declared)
  }

  test("the ontology projects one declared edge per named parent of every composed definition") {
    val core = draco
    val ontology = DomainBuilder.ontology(core)
    val expected = (core.typeDefinition +: core.typeDictionary.elementTypes)
      .flatMap(td => DracoAspect.parents(td.dracoAspect).map(parent => (td.typeName, parent)))
    assert(ontology.derivations.map(e => (e.child, e.parent)) == expected)
    assert(ontology.derivations.nonEmpty && ontology.derivations.forall(_.declared))
    assert(ontology.derivations.forall(e => ontology.defines(e.child)), "every edge leaves a definition the ontology owns")
    assert(ontology.derivations.forall(e => ontology.defines(e.parent)), "Draco is closed under derivation")
  }

  test("Base depends on Draco through Cardinal -> Primal, the first inter-domain relationship") {
    val (core, b) = (draco, base)
    val ontology = DomainBuilder.ontology(core, b)
    val cardinal = TypeName("Cardinal", Seq("draco", "base"))
    // The edge keeps the parent AS AUTHORED — Cardinal derives `Primal(Int)`, parameters and
    // all — so the relationship is read by name and package, the way `owns` reads it.
    // Cardinal is itself `Cardinal(T)`, so both ends are read by name and package.
    val edge = ontology.derivations.find(e => e.child.namePath == cardinal.namePath && e.parent.namePath == primal.namePath)
    assert(edge.exists(_.declared), s"expected a declared Cardinal -> Primal edge; got ${ontology.derivations.filter(_.child.namePath == cardinal.namePath)}")
    assert(edge.exists(_.parent.typeParameters.nonEmpty), "the authored reference's parameters survive on the edge")
    assert(ontology.dependencies(b).map(_.typeDefinition.typeName) == Seq(core.typeDefinition.typeName))
    assert(ontology.dependencies(core).isEmpty, "Draco's members derive only Draco types")
  }

  test("including composes one more domain and leaves the ontology it was called on untouched") {
    val pkg = Seq("example", "fresh")
    val root = TypeDefinition(TypeName("Root", pkg),
      _dracoAspect = DracoAspect(_derivation = Seq(TypeName.encoder(primal))),
      _domainAspect = DomainAspect(TypeName("Fresh", pkg)))
    val leaf = TypeDefinition(TypeName("Leaf", pkg),
      _dracoAspect = DracoAspect(_derivation = Seq(TypeName.encoder(root.typeName))),
      _domainAspect = DomainAspect(TypeName("Fresh", pkg)))
    val fresh: DomainType = Domain[Any](
      TypeDefinition(TypeName("Fresh", pkg), _domainAspect = DomainAspect(TypeName("Fresh", pkg), Seq("Root", "Leaf"))),
      Seq(root, leaf))

    val core = draco
    val before = DomainBuilder.ontology(core)
    val after = before.including(fresh)
    assert(before.size == 1 && !before.defines(root.typeName) && before.dependencies(fresh).isEmpty)
    assert(after.size == 2 && after.defines(root.typeName) && after.defines(leaf.typeName))
    assert(after.derivations.size == before.derivations.size + 2)
    assert(after.derivations.exists(e => e.child == leaf.typeName && e.parent == root.typeName), "an intra-domain edge")
    assert(after.derivations.exists(e => e.child == root.typeName && e.parent == primal), "an inter-domain edge")
    assert(after.dependencies(fresh).map(_.typeDefinition.typeName) == Seq(core.typeDefinition.typeName))
    assert(after.dependencies(core).isEmpty)
  }

  test("owns and defines agree: a name is defined iff some composed domain owns it") {
    val ontology = DomainBuilder.ontology(draco, base)
    val names = Seq(primal, TypeName("Cardinal", Seq("draco", "base")), TypeName("Draco", Seq("draco")),
      TypeName("Base", Seq("draco", "base")), TypeName("Nonexistent", Seq("draco")))
    names.foreach { n =>
      assert(ontology.defines(n) == ontology.keys.exists(d => ontology.owns(d, n)), n.namePath)
    }
    assert(ontology.keys.count(d => ontology.owns(d, primal)) == 1, "Primal is owned by exactly one domain")
  }
}
