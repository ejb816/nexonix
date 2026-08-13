package draco

import org.scalatest.funsuite.AnyFunSuite

/** TypeName's identity is STRUCTURAL (GitHub #37).
 *
 *  Until now `TypeName` was compared by reference, so two names with identical
 *  content were unequal and every downstream check that compared them had to be
 *  written as `.namePath == .namePath` instead. That workaround is only available
 *  where someone noticed; where nobody did, the comparison silently answered
 *  "different" and the caller took the wrong branch.
 *
 *  It stops being cleanup the moment a definition can come from more than one
 *  place. A definition path that resolves a name across several roots has to be
 *  able to say "these two roots define the SAME type" — and the instances it
 *  compares come from separate parses, so nothing makes them the same object.
 *  Reference equality would report no collision, always. */
class TypeNameTest extends AnyFunSuite with PersistentTestLog {

  test("two names with the same content are equal") {
    assert(TypeName("Primes", Seq("draco", "primes")) == TypeName("Primes", Seq("draco", "primes")))
  }

  test("equal names agree on hashCode, so they work as a map key") {
    val a = TypeName("Primes", Seq("draco", "primes"))
    val b = TypeName("Primes", Seq("draco", "primes"))
    assert(a.hashCode == b.hashCode)
    // The property DefinitionPath actually needs: a name parsed from one root
    // finds what a DISTINCT instance of the same name stored from another.
    assert(Map(a -> "one").contains(b))
    assert(Map(a -> "one").updated(b, "two").size == 1)
  }

  test("each field participates") {
    val base = TypeName("Primes", Seq("draco", "primes"))
    assert(base != TypeName("Numbers", Seq("draco", "primes")))
    assert(base != TypeName("Primes", Seq("draco")))
    assert(base != TypeName("Primes", Seq()))
  }

  /** Answers false rather than throwing. `equals` takes `Any`, so the non-name
   *  case is reachable at runtime however unrelated the argument's static type —
   *  which is why the value is bound as `Any` here. Comparing against a `String`
   *  literal directly is a comparison the compiler can settle on its own, and it
   *  says so. */
  test("a name is not equal to a non-name") {
    val notAName: Any = "draco.primes.Primes"
    assert(TypeName("Primes", Seq("draco", "primes")) != notAName)
  }

  /** The case from #37's report: `domainAspect.typeName == typeName` decides
   *  whether a definition IS a domain, and answered false for every domain. */
  test("a domain recognizes itself") {
    val draco = TypeLoader.loadType(TypeName("Draco", _namePackage = Seq("draco")))
    assert(draco.domainAspect.typeName == draco.typeName)
  }

  /** DELIBERATE, and the sharp edge worth keeping visible: type parameters are
   *  part of the identity. They are FORMALS on a declaring type and ACTUALS on a
   *  reference to it, so a reference spelled `Dictionary(K, V)` is NOT equal to
   *  the declaration `Dictionary` it refers to.
   *
   *  Resolution is unaffected — `namePath` and `resourcePath` both exclude type
   *  parameters, so loading by either name reaches the same definition. What it
   *  affects is `Map[TypeName, _]` lookup, and `TypeDictionary.kvMap` is exactly
   *  that, keyed by declarations. Looking one up by a parameterized reference
   *  misses. Asserted here so the behaviour is a decision on record rather than
   *  something rediscovered later. */
  test("type parameters are part of the identity") {
    val declaration = TypeName("Dictionary", Seq("draco"))
    val reference   = TypeName("Dictionary", Seq("draco"), Seq("K", "V"))
    assert(declaration != reference)
    assert(declaration.namePath == reference.namePath)
    assert(declaration.resourcePath == reference.resourcePath)
  }
}
