package draco.drake

import org.scalatest.funsuite.AnyFunSuite

/** `fold` is the eliminator of draco's own option, and it is DISPATCH: declared abstract
  * on `Presence`, defined in `Present` as `present(value)` and in `Absent` as `absent`,
  * all three in drake with no host code and no conditional. This is the first member
  * override of a parent `dyn` in the corpus — `dyn` kept for polymorphism, exercised.
  * The call surface is a method on the presence, `p.fold(absent, f)`. */
class PresenceTest extends AnyFunSuite {

  test("fold dispatches: Present applies the function to its value, Absent yields the default") {
    assert(Present(3).fold(0, (n: Int) => n + 1) == 4)
    assert(Absent[Int]().fold(0, (n: Int) => n + 1) == 0)
  }

  test("fold dispatches through the parent type, and the branches may change type") {
    val present: Presence[Int] = Present(3)
    val absent:  Presence[Int] = Absent[Int]()
    assert(present.fold("none", n => s"some $n") == "some 3")
    assert(absent.fold("none", n => s"some $n") == "none")
  }
}
