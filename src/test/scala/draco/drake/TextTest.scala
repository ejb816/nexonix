package draco.drake

import draco.{DracoAspect, DracoGenerator, TypeLoader, TypeName}
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

class TextTest extends AnyFunSuite {
  private def leaf(s: String): Json = Json.fromString(s)
  private def node(op: String, args: Json*): Json = Json.obj(op -> Json.arr(args: _*))
  private val text = leaf("draco.drake.Text")

  test("Text is a definition-backed member of the Drake runtime domain") {
    val td = TypeLoader.loadType(TypeName("Text", Seq("draco", "drake")))
    assert(td.domainAspect.typeName == TypeName("Drake", Seq("draco", "drake")))
    assert(Drake.elementTypeNames.contains("Text"))
    assert(Text.typeDefinition.typeName == td.typeName)
  }

  test("Scala realizes the qualified Text identity recursively without capturing other names") {
    assert(DracoGenerator.targetType(text) == leaf("String"))
    assert(DracoGenerator.targetType(node("[]", text)) == leaf("Seq[String]"))
    assert(DracoGenerator.targetType(node("->", text, text)) == leaf("String => String"))
    assert(DracoGenerator.targetType(node("()", leaf("Primal"), text)) == leaf("Primal[String]"))
    assert(DracoGenerator.targetType(leaf("Text")) == leaf("Text"))
    assert(DracoGenerator.targetType(leaf("example.Text")) == leaf("example.Text"))
    assert(text == leaf("draco.drake.Text"))
  }

  test("Nominal retains its Scala representation but names Text in its definition") {
    val td = TypeLoader.loadType(TypeName("Nominal", Seq("draco", "base")))
    val primal = DracoAspect.parents(td.dracoAspect).find(_.name == "Primal").get
    assert(primal.typeParameters == Seq(text))
    assert(DracoGenerator.generate(td).contains("trait Nominal extends Unit with Primal[String]"))
    assert(draco.Drake.emit(td).contains("Primal(draco.drake.Text)"))
  }

  test("projected text operations preserve Unicode, exact equality and empty joins") {
    val scalaText = DracoGenerator.targetType(text).asString.get
    val concat = DracoGenerator.expression(node("++", leaf("a"), leaf("b")))
    val join = DracoGenerator.expression(node("()", leaf("join"), leaf("sep"), leaf("xs")))
    val equal = DracoGenerator.expression(node("==", leaf("a"), leaf("b")))
    val source = s"""package draco.generated.test
      |class TextProbe {
      |  def concat(a: $scalaText, b: $scalaText): $scalaText = $concat
      |  def join(sep: $scalaText, xs: Seq[$scalaText]): $scalaText = $join
      |  def equal(a: $scalaText, b: $scalaText): Boolean = $equal
      |}
      |""".stripMargin
    val directory = DracoGenerator.compile(source, "TextProbe.scala").fold(
      errors => fail(errors.mkString("\n")), identity)
    val clazz = DracoGenerator.loadClass(directory, "draco.generated.test.TextProbe")
    val instance = clazz.getDeclaredConstructor().newInstance()
    val concatMethod = clazz.getMethod("concat", classOf[String], classOf[String])
    val equalMethod = clazz.getMethod("equal", classOf[String], classOf[String])
    val joinMethod = clazz.getMethod("join", classOf[String], classOf[Seq[_]])
    val composed = "\u00e9"
    val decomposed = "e\u0301"
    val supplementary = "\ud83d\ude00"
    assert(concatMethod.invoke(instance, composed, supplementary) == composed + supplementary)
    assert(concatMethod.invoke(instance, "", composed) == composed)
    assert(equalMethod.invoke(instance, composed, composed) == java.lang.Boolean.TRUE)
    assert(equalMethod.invoke(instance, composed, decomposed) == java.lang.Boolean.FALSE)
    assert(joinMethod.invoke(instance, "|", Seq(composed, supplementary)) == composed + "|" + supplementary)
    assert(joinMethod.invoke(instance, "|", Seq.empty[String]) == "")
    assert(joinMethod.invoke(instance, "|", Seq(composed)) == composed)
  }
}
