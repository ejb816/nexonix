package draco

import org.scalatest.funsuite.AnyFunSuite

/** Locks the "identity element" and "generation is total" invariants that the
 *  aspect-composer stands on.
 *
 *  Two distinct notions of minimality:
 *   - `TypeName.Null` is the algebraic identity — `loadType(Null)` returns the
 *     empty (aspect-less) TypeDefinition, the base contribution with not even a
 *     name. It is the identity element for aspect-composition, never emitted as a
 *     standalone file (a nameless, package-less type has no legal Scala surface).
 *   - a NAMED, packaged but aspect-less type — what a declared-but-unauthored
 *     member (`draco.Foo` with no JSON) loads as — must still generate source that
 *     COMPILES: the minimal named type, a bare trait + object carrying only the
 *     base contribution (`typeDefinition` + `dracoType`). Generation is a total
 *     function; the validators, not the Generator, judge that the absence of
 *     aspects is a problem.
 *
 *  Also pins the `resourcePath` fix that makes the no-name domain (empty package)
 *  reachable at the resource root rather than behind a `//` double slash.
 */
class MinimalTypeTest extends AnyFunSuite with PersistentTestLog {

  test("resourcePath omits the empty package (no-name domain reaches the resource root)") {
    assert(TypeName("Bar").resourcePath == "/Bar.json",
      s"bare name should resolve at the root; got ${TypeName("Bar").resourcePath}")
    assert(TypeName("Bar", _namePackage = Seq("foo")).resourcePath == "/foo/Bar.json")
    assert(TypeName("Bar", _namePackage = Seq("foo", "bar")).resourcePath == "/foo/bar/Bar.json")
  }

  test("loadType(TypeName.Null) returns the identity TypeDefinition") {
    val identity = TypeLoader.loadType(TypeName.Null)
    assert(DomainBuilder.isStub(identity), "the identity type carries no aspects")
    assert(identity.typeName.name.isEmpty, "the identity type is nameless")
  }

  test("a fileless named member generates source that compiles (generation is total)") {
    val minimal = TypeDefinition(TypeName("Foo", _namePackage = Seq("draco")))
    val source = Generator.generate(minimal)
    assert(source.contains("trait Foo"), s"expected a bare trait; got:\n$source")
    assert(source.contains("object Foo"), s"expected a base object; got:\n$source")

    Generator.compile(source, "Foo.scala") match {
      case Right(_) => // the minimal named type compiles
      case Left(errs) =>
        log.info(source)
        fail(s"minimal named type failed to compile: ${errs.mkString("; ")}")
    }
  }
}
