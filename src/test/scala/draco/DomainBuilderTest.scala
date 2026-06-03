package draco

import org.scalatest.funsuite.AnyFunSuite

/** Exercises [[DomainBuilder]] against the canonical first-party draco domains.
 *
 *  DomainBuilder is a `src/mods` staging stand-in for an under-development core
 *  capability: comprehensively define a domain dictionary from JSON, stand up a
 *  *populated* concrete instance, validate it, and generate code for the whole
 *  dictionary. It lives at `src/mods/scala/draco/DomainBuilder.scala` but compiles
 *  into `root` (see build.sbt's `unmanagedSourceDirectories`), which is why this
 *  test in the root project can reference it directly with no cross-project cycle.
 *
 *  These tests "build" each domain — Draco, Base, Primes, Language — and assert:
 *   - `define` returns a domain whose dictionary is *populated* (loaded member
 *     definitions, not the hollow name-only `TypeDictionary.apply` shells);
 *   - `validate` reports zero problems — the endogenous domains are held to the
 *     full structural rigor (self-declaration, completeness, derivation resolvability);
 *   - `generate` emits non-empty Scala for the domain and every member.
 *
 *  The `validate` assertions are intentionally strict: a failure here is a real
 *  defect in a shipped domain (a missing member file, a dangling derivation, a
 *  mis-declared domain), to be fixed sooner rather than later — not a DomainBuilder
 *  bug. DomainBuilder's skeleton tolerance is for *user* domains under construction;
 *  the first-party domains are expected to need none of it.
 */
class DomainBuilderTest extends AnyFunSuite {

  /** Canonical first-party domains — mirrors the probe set in
   *  `src/mods/scala/scripts/list-domains.scala`. */
  private val domains: Seq[(String, Seq[String])] = Seq(
    ("Draco",    Seq("draco")),
    ("Base",     Seq("draco", "base")),
    ("Primes",   Seq("draco", "primes")),
    ("Language", Seq("draco", "language"))
  )

  for ((name, pkg) <- domains) {

    test(s"$name — define yields a populated, correctly-keyed dictionary") {
      val domain = DomainBuilder.define(name, pkg)

      assert(domain.domainDefinition.typeName.name == name,
        s"expected domainDefinition named $name")

      val members = domain.typeDictionary.elementTypes
      assert(members.nonEmpty, s"$name should have at least one member")

      // Dictionary size matches the names declared in JSON.
      assert(members.size == domain.domainDefinition.domainAspect.elementTypeNames.size,
        s"$name dictionary should hold every declared member")

      // Each member is retrievable by its own TypeName.
      members.foreach { m =>
        assert(domain.typeDictionary.get(m.typeName).contains(m),
          s"${m.typeName.name} should be retrievable from $name's dictionary")
      }
    }

    test(s"$name — validate reports no problems") {
      val problems = DomainBuilder.validate(DomainBuilder.define(name, pkg))
      assert(problems.isEmpty,
        s"$name failed validation:\n  - ${problems.mkString("\n  - ")}")
    }

    test(s"$name — generate emits non-empty Scala for the domain and every member") {
      val code = DomainBuilder.generate(name, pkg)

      val domainDef = DomainBuilder.define(name, pkg)
      assert(code.size == domainDef.typeDictionary.elementTypes.size + 1,
        s"$name should generate code for the domain plus each member")

      code.foreach { case (tn, src) =>
        assert(src.trim.nonEmpty, s"generated source for ${tn.name} should be non-empty")
        assert(src.contains(s"package ${tn.namePackage.mkString(".")}"),
          s"generated source for ${tn.name} should declare its package")
      }
    }
  }

  test("dictionary assembles a populated cross-domain registry") {
    val built = domains.map { case (n, p) => DomainBuilder.define(n, p) }
    val registry = DomainBuilder.dictionary(built: _*)

    assert(registry.size == domains.size, "registry should hold every built domain")

    built.foreach { d =>
      assert(registry.get(d).exists(_.elementTypes.nonEmpty),
        s"${d.domainDefinition.typeName.name} should map to a populated dictionary")
    }
  }
}
