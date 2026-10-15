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

  test("including a domain of the same name and package replaces it — an update, not a second owner") {
    val pkg = Seq("example", "fresh")
    def member(name: String) = TypeDefinition(TypeName(name, pkg), _domainAspect = DomainAspect(TypeName("Fresh", pkg)))
    def fresh(members: String*): DomainType = Domain[Any](
      TypeDefinition(TypeName("Fresh", pkg), _domainAspect = DomainAspect(TypeName("Fresh", pkg), members)),
      members.map(member))
    val before = DomainBuilder.ontology(draco).including(fresh("Root", "Leaf"))
    val after = before.including(fresh("Root"))
    assert(before.size == 2 && after.size == 2, "the same identity is replaced, not added")
    assert(before.defines(TypeName("Leaf", pkg)) && !after.defines(TypeName("Leaf", pkg)))
    assert(after.defines(TypeName("Root", pkg)))
    assert(after.keys.count(d => d.typeDefinition.typeName == TypeName("Fresh", pkg)) == 1)
    assert(after.keys.count(d => after.owns(d, TypeName("Root", pkg))) == 1, "one owner after the update")
  }

  test("excluding removes the domain of that name and package, leaves the ontology untouched, and ignores an unknown name") {
    // The inverse of including (2026-10-14, Delete's first point): a new ontology without
    // the named domain; the one it is called on keeps everything.
    val both = DomainBuilder.ontology(draco, base)
    val baseName = base.typeDefinition.typeName
    val without = both.excluding(baseName)
    assert(both.size == 2 && without.size == 1)
    assert(both.defines(TypeName("Cardinal", Seq("draco", "base"))) && !without.defines(TypeName("Cardinal", Seq("draco", "base"))))
    assert(without.derivations.forall(e => e.child.namePackage != Seq("draco", "base")), "the removed domain's edges go with it")
    assert(both.excluding(TypeName("Nowhere", Seq("nowhere"))).size == 2, "an unknown name removes nothing")
    assert(both.excluding(TypeName(baseName.name, baseName.namePackage, Seq(io.circe.Json.fromString("T")))).size == 1,
      "name and package, parameters free — as definitions are keyed")
  }

  test("holds and holding: a domain by its identity, name and package, parameters free — a Presence, never an option") {
    // 2026-10-15: the lookup the Editor's intent rules and the requests share, moved into
    // the ontology from the Editor's `holder` global.
    val both = DomainBuilder.ontology(draco, base)
    val baseName = base.typeDefinition.typeName
    assert(both.holds(baseName) && both.holds(TypeName("Draco", Seq("draco"))))
    assert(!both.holds(TypeName("Nowhere", Seq("nowhere"))) && !both.holds(primal), "a member is owned, not held: holds is about domains")
    assert(both.holds(TypeName(baseName.name, baseName.namePackage, Seq(io.circe.Json.fromString("T")))), "parameters free")
    assert(both.holding(baseName).fold(false, d => d.typeDefinition.typeName == baseName))
    assert(both.holding(TypeName("Nowhere", Seq("nowhere"))).fold(true, _ => false))
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
