package draco.gendrake

import draco._
import draco.drake.Present
import draco.format.dracon.Dracon
import org.scalatest.funsuite.AnyFunSuite

/** The first ELEMENT refusal is data (Dev, 2026-11-14; GitHub #11): `gendrake.ElementRefusalOf`
 *  maps an element onto the `draco.Problem`s that refuse it a member line — a kind with no drake
 *  keyword (Action, Factory, Pattern: sections, never member lines), with the message the emitter
 *  raised by hand — and recurses into its parameters and body. `gendrake.RefusalOf` walks the
 *  elements a surface renders (the type section's, a present factory's, the globals, a present
 *  rule's, a present actor's) and joins those problems after the codec refusals. The second
 *  element refusal followed the same day: a block — a dyn with parameters or a body, a
 *  case-branch — carrying a value outside its body; and the third, a value outside the tree
 *  language, each reason of `gendrake.ValueRefusalOf` as a Problem. Invalid formals, the check
 *  shared with the parser and the Scala generator, stays in the emitter.
 */
class ElementRefusalOfTest extends AnyFunSuite {

  private val subject = TypeName("Probe", Seq("example"))
  private val int = Dracon.string("Int")
  private def noKeyword(name: String, kind: String) = s"Drake.emit: no drake keyword for element '$name' ($kind)"
  private val outside = " carries a value outside its body — the result is the body element named `value`"
  private val unknown = Dracon(io.circe.Json.obj("<:" -> io.circe.Json.arr(io.circe.Json.fromString("a"), io.circe.Json.fromString("b"))))
  private val unknownOperator = "Drake.expression: unknown operator '<:' in {\"<:\":[\"a\",\"b\"]}"
  private def of(element: TypeElement): Seq[String] = ElementRefusalOf(element, subject).problems.map(_.message)

  private val probe: String =
    """type Probe
      |  elements
      |    fix a Int 1
      |    dyn d Int [
      |      parameters
      |        par x Int
      |      body
      |        loc y Int x
      |        fix value Int y
      |    ]
      |domain draco Draco
      |rule
      |  pattern
      |    variables
      |      var v Integer
      |    conditions
      |      con v > 0
      |  action
      |    mon println(v)
      |actor
      |  messageType String
      |  message
      |    case s String [
      |      mon println(s)
      |    ]
      |""".stripMargin

  test("an element of a kind with a keyword has no problems, at any depth") {
    assert(of(Fixed("a", int)).isEmpty)
    assert(of(Dynamic("d", int, _parameters = Seq(Parameter("x", int, Dracon.Null)), _body = Seq(Monadic(Dracon.string("println(x)"))))).isEmpty)
    val read = Drake.parse(probe)
    assert(RefusalOf(read).elementProblems.isEmpty && RefusalOf(read).problems.isEmpty)
  }

  test("a kind with no keyword is one Problem naming the definition, with the emitter's message") {
    val action = ElementRefusalOf(Action(), subject).problems
    assert(action.map(_.message) == Seq(noKeyword("", "Action")))
    assert(action.head.subject == subject)
    assert(of(Pattern()) == Seq(noKeyword("", "Pattern")))
    assert(of(Factory(int)) == Seq(noKeyword("", "Factory")))
  }

  test("the refusal reaches into parameters and bodies, in order") {
    val nested = Dynamic("d", int, _body = Seq(Fixed("a", int), Action(_body = Seq(Pattern())), Pattern()))
    assert(of(nested) == Seq(noKeyword("", "Action"), noKeyword("", "Pattern"), noKeyword("", "Pattern")))
  }

  test("a block carrying a value outside its body is one Problem, with the emitter's message") {
    val one = Dracon.string("1")
    val method = Dynamic("d", int, _body = Seq(Fixed("value", int, one)), _value = Dracon.string("2"))
    assert(of(method) == Seq("Drake.emit: block dyn 'd'" + outside))
    assert(of(Dynamic("d", int, _parameters = Seq(Parameter("x", int, Dracon.Null)), _value = one)) == Seq("Drake.emit: block dyn 'd'" + outside))
    // a leaf dyn's value is its own line; a block with no value of its own is as authored
    assert(of(Dynamic("d", int, _value = one)).isEmpty)
    assert(of(Dynamic("d", int, _body = Seq(Fixed("value", int, one)))).isEmpty)
    // a case-branch is always a block
    val message = Drake.parse(probe).actorAspect.fold(Seq.empty[TypeElement], _.message.body)
    assert(message.flatMap(of).isEmpty)
    val valued = TypeDefinition.decoder.decodeJson(TypeDefinition.encoder(Drake.parse(probe)).hcursor
      .downField("actorAspect").downField("message").downField("body").downN(0).withFocus(_.mapObject(_.add("value", io.circe.Json.fromString("1")))).top.get)
      .fold(error => fail(error.toString), identity)
    assert(RefusalOf(valued).elementProblems.map(_.message) == Seq("Drake.emit: case-branch" + outside))
    // nested, after its container's own problems
    assert(of(Dynamic("outer", int, _body = Seq(method, Action()))) == Seq("Drake.emit: block dyn 'd'" + outside, noKeyword("", "Action")))
  }

  test("a value outside the tree language is one Problem per reason, ahead of the block refusal") {
    assert(of(Fixed("a", int, unknown)) == Seq(unknownOperator))
    assert(ElementRefusalOf(Fixed("a", int, unknown), subject).problems.head.subject == subject)
    assert(of(Fixed("a", int, Dracon(io.circe.Json.fromInt(1)))) == Seq("Drake.expression: unrenderable value 1"))
    // inside a block, and on a block's own misplaced value: refused for the value, as the emitter judged it first
    assert(of(Dynamic("d", int, _body = Seq(Fixed("value", int, unknown)))) == Seq(unknownOperator))
    assert(of(Dynamic("d", int, _body = Seq(Fixed("value", int, Dracon.string("1"))), _value = unknown)) == Seq(unknownOperator))
    assert(of(Fixed("a", int, Dracon.string("x + 1"))).isEmpty && of(Fixed("a", int)).isEmpty)
  }

  test("RefusalOf walks the elements the surface renders, and no others") {
    def typed(aspect: DracoAspect) = TypeDefinition(subject, _dracoAspect = aspect)
    def count(td: TypeDefinition) = RefusalOf(td).elementProblems.size
    assert(count(typed(DracoAspect(_elements = Seq(Fixed("a", int), Action())))) == 1)
    assert(count(typed(DracoAspect(_globalElements = Seq(Pattern(), Action())))) == 2)
    // a present factory's body is rendered; an absent factory's is not judged
    assert(count(typed(DracoAspect(_factory = Factory(Dracon.string("Probe"), _body = Seq(Action()))))) == 1)
    assert(count(typed(DracoAspect(_factory = Factory(Dracon.Null, _body = Seq(Action()))))) == 0)
    // a present rule's action and a present actor's three blocks
    val unkeyed = Action(_body = Seq(Pattern()))
    assert(count(TypeDefinition(subject, _ruleAspect = Present(RuleAspect(_action = unkeyed)))) == 1)
    assert(count(TypeDefinition(subject, _actorAspect = Present(ActorAspect(_message = unkeyed, _signal = unkeyed, _start = unkeyed)))) == 3)
    // the codec refusals come first in the joined list
    val both = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(Action())), _codecAspect = CodecAspect(_discriminator = "tag"))
    assert(RefusalOf(both).problems.map(_.message) == Seq("Drake.emit: codec discriminator not yet emitted: Probe", noKeyword("", "Action")))
    assert(RefusalOf(both).codecProblems.size == 1 && RefusalOf(both).elementProblems.size == 1)
  }

  test("the element is read once, and only when the problems are read") {
    var reads = 0
    val refusal = ElementRefusalOf({ reads += 1; Action() }, subject)
    assert(reads == 0)
    assert(refusal.problems.size == 1 && refusal.problems.size == 1)
    assert(reads == 1)
  }

  test("the live emitter and its section entry points raise the first problem, before anything renders") {
    val unkeyed = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(Fixed("a", int), Action())))
    assert(intercept[IllegalArgumentException](Drake.emit(unkeyed)).getMessage == noKeyword("", "Action"))
    assert(intercept[IllegalArgumentException](Drake.dracoAspectText(unkeyed)).getMessage == noKeyword("", "Action"))
    assert(intercept[IllegalArgumentException](Drake.ruleAspectText(unkeyed)).getMessage == noKeyword("", "Action"))
    assert(intercept[IllegalArgumentException](Drake.actorAspectText(unkeyed)).getMessage == noKeyword("", "Action"))
    // the codec refusal is still the emitter's first; a section entry point judges elements alone
    val both = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(Action())), _codecAspect = CodecAspect(_discriminator = "tag"))
    assert(intercept[IllegalArgumentException](Drake.emit(both)).getMessage == "Drake.emit: codec discriminator not yet emitted: Probe")
    assert(intercept[IllegalArgumentException](Drake.dracoAspectText(both)).getMessage == noKeyword("", "Action"))
    val valued = Dynamic("d", int, _body = Seq(Fixed("value", int, Dracon.string("1"))), _value = Dracon.string("2"))
    val block = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(valued)))
    assert(intercept[IllegalArgumentException](Drake.emit(block)).getMessage == "Drake.emit: block dyn 'd'" + outside)
    val unrenderable = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(Fixed("a", int, unknown))))
    assert(intercept[IllegalArgumentException](Drake.emit(unrenderable)).getMessage == unknownOperator)
    // the one refusal still judged by hand, a method's formals, follows the definition's, over the whole definition
    val duplicate = Dynamic("f", int, _typeParameters = Seq(TypeParameter("T"), TypeParameter("T")))
    assert(intercept[IllegalArgumentException](Drake.emit(TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(duplicate))))).getMessage.contains("Duplicate type parameters"))
    val twoWays = TypeDefinition(subject, _dracoAspect = DracoAspect(_elements = Seq(duplicate, Action())))
    assert(intercept[IllegalArgumentException](Drake.emit(twoWays)).getMessage == noKeyword("", "Action"), "the definition's refusals are judged over the whole definition first")
    assert(Drake.emit(Drake.parse(probe)) == probe)
  }
}
