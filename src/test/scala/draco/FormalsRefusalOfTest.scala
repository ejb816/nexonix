package draco

import draco.format.dracon.Dracon
import draco.gendrake.{ElementRefusalOf, RefusalOf}
import io.circe.{Json, parser}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

/** The check on a method's FORMALS is a definition (Dev, 2026-11-15; GitHub #11):
 *  `draco.FormalsRefusalOf` maps a method onto the reasons its formal declarations are
 *  malformed — a name carrying its formals, a formal declared twice, a formal that is not a
 *  name, a bound that is not a type form or that carries a bound marker — with the messages the
 *  hand-written `MethodTypeParameters.validate` raised. It is a statement about the definition,
 *  not about a target, so it lives in core beside TypeParameter, and its three readers — the
 *  drake emitter (through `gendrake.ElementRefusalOf`, as Problems), the drake parser and the
 *  Scala generator — raise the first reason. With it no refusal of the drake surface is
 *  hand-written.
 */
class FormalsRefusalOfTest extends AnyFunSuite {

  private val subject = TypeName("Probe", Seq("example"))
  private val unit = Dracon.string("Unit")
  private def leaf(text: String) = Dracon.string(text)
  private def tree(op: String, operands: Json*) = Dracon(Json.obj(op -> Json.arr(operands: _*)))
  private def method(name: String, formals: TypeParameter*) = Dynamic(name, unit, _typeParameters = formals)
  private def of(element: TypeElement): Seq[String] = FormalsRefusalOf(element).reasons
  private def upper(bound: Dracon) = of(method("f", TypeParameter("T", bound)))

  test("a method with well-formed formals has no reasons, and only a method is judged") {
    assert(of(method("size")).isEmpty)
    assert(of(method("choose", TypeParameter("T"), TypeParameter("R"))).isEmpty)
    val box = tree("()", Json.fromString("Box"), Json.obj("[]" -> Json.arr(Json.fromString("Item"))))
    assert(of(method("choose", TypeParameter("T", box, leaf("Bottom")), TypeParameter("_r9"))).isEmpty)
    assert(upper(tree("->", Json.fromString("A"), Json.fromString("B"))).isEmpty)
    assert(upper(tree("{}+", Json.fromString("K"), Json.fromString("V"))).isEmpty)
    // formals are declared on every element kind and are a method's alone
    assert(of(Fixed("f[T]", unit)).isEmpty && of(Action()).isEmpty)
  }

  test("each malformed declaration is one reason, with the message the hand-written check raised") {
    assert(of(method("f[T]")) == Seq("Method 'f[T]' must declare type parameters separately"))
    assert(of(method("f(T)")) == Seq("Method 'f(T)' must declare type parameters separately"))
    assert(of(method("f", TypeParameter("T"), TypeParameter("T"))) == Seq("Duplicate type parameters on 'f'"))
    Seq("T: Decoder", "9lives", "", "A.B").foreach { name =>
      assert(of(method("f", TypeParameter(name))) == Seq(s"Invalid type parameter '$name'"), s"for '$name'")
    }
    Seq(tree("[]"), tree("[]", Json.fromString("A"), Json.fromString("B")), tree("->", Json.fromString("A")),
      tree("(,)", Json.fromString("A")), tree("<:", Json.fromString("A"), Json.fromString("B")), leaf(" "),
      Dracon(Json.fromInt(1)), tree("[]", Json.fromInt(1)), Dracon(Json.obj("[]" -> Json.arr(), "{}" -> Json.arr()))).foreach { bound =>
      assert(upper(bound) == Seq("Invalid type-form bound for 'T'"), s"for ${bound.compact}")
    }
    Seq("A <: B", "A >: B", "T: Decoder").foreach { text =>
      assert(upper(leaf(text)) == Seq(s"Invalid bound for 'T': $text"), s"for '$text'")
    }
    // inside a tree the reason names the leaf that carries the marker
    assert(upper(tree("[]", Json.fromString("A <: B"))) == Seq("Invalid bound for 'T': A <: B"))
  }

  test("every reason is reported, in the order the hand-written check judged them") {
    val all = method("f[T]", TypeParameter("T", leaf("A <: B"), tree("[]")), TypeParameter("T"), TypeParameter("9"))
    assert(of(all) == Seq(
      "Method 'f[T]' must declare type parameters separately",
      "Duplicate type parameters on 'f[T]'",
      "Invalid type-form bound for 'T'", // the lower bound first
      "Invalid bound for 'T': A <: B",
      "Invalid type parameter '9'"))
  }

  test("a bound is well formed by its node's arity, and its leaves are read in order") {
    val a = Json.fromString("A"); val b = Json.fromString("B"); val c = Json.fromString("C")
    def ok(form: Dracon) = FormalsRefusalOf.wellFormed(form)
    assert(ok(leaf("A")) && ok(tree("[]", a)) && ok(tree("[]+", a)) && ok(tree("{}", a)) && ok(tree("{}", a, b)) && ok(tree("{}+", a, b)))
    assert(ok(tree("->", a, b)) && ok(tree("()", a, b)) && ok(tree("(,)", a, b, c)))
    assert(ok(Dracon(Json.obj("[]" -> a))), "a lone operand is read as one")
    assert(!ok(tree("{}", a, b, c)) && !ok(tree("->", a, b, c)) && !ok(tree("()", a)) && !ok(tree("(,)")) && !ok(tree("?", a)))
    assert(!ok(tree("[]", Json.obj("->" -> Json.arr(a)))), "at any depth")
    assert(!ok(Dracon.Null) && !ok(leaf("")))
    val nested = tree("->", Json.obj("(,)" -> Json.arr(a, Json.obj("[]" -> Json.arr(b)))), c)
    assert(FormalsRefusalOf.leaves(nested) == Seq("A", "B", "C") && FormalsRefusalOf.leaves(leaf("A")) == Seq("A"))
  }

  test("the element is read once, and only when the reasons are read") {
    var reads = 0
    val refusal = FormalsRefusalOf({ reads += 1; method("f", TypeParameter("T"), TypeParameter("T")) })
    assert(reads == 0)
    assert(refusal.reasons.size == 1 && refusal.reasons.size == 1)
    assert(reads == 1)
  }

  test("the emitter, the parser and the Scala generator raise the first reason") {
    val duplicate = method("f", TypeParameter("T"), TypeParameter("T"))
    val td = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(Fixed("a", unit), duplicate)))
    // the emitter, through the Problems of the definition's walk
    val problems = ElementRefusalOf(duplicate, subject).problems
    assert(problems.map(_.message) == Seq("Duplicate type parameters on 'f'") && problems.head.subject == subject)
    assert(RefusalOf(td).elementProblems.map(_.message) == Seq("Duplicate type parameters on 'f'"))
    assert(intercept[IllegalArgumentException](Drake.emit(td)).getMessage == "Duplicate type parameters on 'f'")
    // a kind with no keyword comes first on the same element, a method's value after its formals
    val valued = Dynamic("g(T)", unit, _value = Dracon(Json.fromInt(1)))
    assert(ElementRefusalOf(valued, subject).problems.map(_.message) ==
      Seq("Method 'g(T)' must declare type parameters separately", "Drake.expression: unrenderable value 1"))
    // the parser
    val read = intercept[IllegalArgumentException](Drake.parse("type Probe\n  elements\n    dyn f(T, T) Unit ()\ndomain draco Draco\n"))
    assert(read.getMessage == "Duplicate type parameters on 'f'")
    // the Scala generator
    assert(intercept[IllegalArgumentException](DracoGenerator.generate(td)).getMessage == "Duplicate type parameters on 'f'")
  }

  test("no method in the main and mods corpus is refused") {
    val definitions = Seq("src/main/resources", "src/mods/resources").flatMap { root =>
      Files.walk(Paths.get(root)).iterator().asScala
        .filter(p => p.toString.endsWith(".json") && p.getFileName.toString != ".json") // the nameless domain's anchor
        .map(_.toString).toSeq
    }.sorted
    assert(definitions.size >= 205, s"the corpus walk found only ${definitions.size} definitions")

    def all(element: TypeElement): Seq[TypeElement] = element +: (element.parameters ++ element.body).flatMap(all)
    var methods = 0
    var lists = 0
    val refused: Seq[String] = definitions.flatMap { path =>
      val td = parser.parse(Files.readString(Paths.get(path))).flatMap(_.as[TypeDefinition]).fold(error => fail(s"$path: $error"), identity)
      val aspect = td.dracoAspect
      val elements = (aspect.elements ++ aspect.factory.parameters ++ aspect.factory.body ++ aspect.globalElements ++
        td.ruleAspect.fold(Seq.empty[TypeElement], rule => rule.pattern.variables ++ rule.pattern.conditions ++ rule.action.body) ++
        td.actorAspect.fold(Seq.empty[TypeElement], actor => actor.start.body ++ actor.message.body ++ actor.signal.body)).flatMap(all)
      methods += elements.count(_.isInstanceOf[Dynamic])
      lists += elements.count(_.typeParameters.nonEmpty)
      elements.flatMap(element => FormalsRefusalOf(element).reasons.map(reason => s"$path: $reason"))
    }
    assert(methods >= 100 && lists >= 10, s"the walk found only $methods methods and $lists formal lists")
    assert(refused.isEmpty, "a committed method's formals are refused:\n  " + refused.mkString("\n  "))
  }
}
