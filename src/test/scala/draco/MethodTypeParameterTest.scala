package draco

import draco.scalatarget.DefinitionTrees._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

class MethodTypeParameterTest extends AnyFunSuite {
  private def definition(method: String): TypeDefinition =
    Drake.parse(s"type Probe\n  elements\n    $method\ndomain draco Draco\n")

  test("method formals carry plain names and structured bounds through the family codec") {
    val td = definition("dyn choose(T >: Bottom <: Box([Item]), R) R")
    val method = td.dracoAspect.elements.head.asInstanceOf[Dynamic]
    assert(method.name == "choose")
    assert(method.typeParameters.map(_.name) == Seq("T", "R"))
    assert(method.typeParameters.head.upperBound.json ==
      Json.obj("()" -> Json.arr(Json.fromString("Box"), Json.obj("[]" -> Json.arr(Json.fromString("Item"))))))
    val wire = TypeElement.encoder(method)
    assert(wire.hcursor.get[String]("name") == Right("choose"))
    assert(wire.hcursor.downField("typeParameters").downArray.get[String]("lowerBound") == Right("Bottom"))
    val decoded = TypeElement.decoder.decodeJson(wire).toOption.get
    assert(TypeElement.encoder(decoded) == wire)
    assert(TypeDefinition.encoder(Drake.parse(Drake.emit(td))) == TypeDefinition.encoder(td))
  }

  test("missing method formals and absent bounds retain empty defaults") {
    val method = TypeElement.decoder.decodeJson(Json.obj(
      "kind" -> Json.fromString("Dynamic"), "name" -> Json.fromString("size"),
      "valueType" -> Json.fromString("Int"))).toOption.get.asInstanceOf[Dynamic]
    assert(method.typeParameters.isEmpty)
    assert(!TypeElement.encoder(method).hcursor.downField("typeParameters").succeeded)
    val formal = TypeParameter.decoder.decodeJson(Json.obj("name" -> Json.fromString("T"))).toOption.get
    assert(formal.upperBound.isNull && formal.lowerBound.isNull)
    assert(TypeParameter.encoder(formal) == Json.obj("name" -> Json.fromString("T")))
  }

  test("Scala generation compiles generic methods with lower and compound upper bounds") {
    val td = Drake.parse("""type Generic
      |  elements
      |    dyn identity(T) T [
      |      parameters
      |        par input T
      |      body
      |        fix value T input
      |    ]
      |    dyn bounded(T <: Comparable(T)) Unit ()
      |    dyn widen(T >: String) Unit ()
      |  factory
      |  globals
      |    dyn pass(T) T [
      |      parameters
      |        par input T
      |      body
      |        fix value T input
      |    ]
      |domain formal fixture Fixture
      |""".stripMargin)
    val source = DracoGenerator.generate(td)
    assert(source.contains("def identity[T]"))
    assert(source.contains("def bounded[T <: Comparable[T]]"))
    assert(source.contains("def widen[T >: String]"))
    val probe = """
      |trait Fixture extends draco.DracoType
      |object Check { def verify(): Boolean = Generic().identity(12) == 12 && Generic.pass("ok") == "ok" }
      |""".stripMargin
    val compiled = DracoGenerator.compile(source + probe, "Generic.scala")
    assert(compiled.isRight, compiled.left.toOption.getOrElse(Seq.empty).mkString("\n"))
    val cls = DracoGenerator.loadClass(compiled.toOption.get, "formal.fixture.Check$")
    assert(cls.getMethod("verify").invoke(cls.getField("MODULE$").get(null)) == java.lang.Boolean.TRUE)
  }

  test("strict dynamic declarations retain their formals during target normalization") {
    val td = definition("""now dyn outer(T) T [
      |      parameters
      |        par input T
      |      body
      |        fix value T input
      |    ]""".stripMargin)
    val method = td.dracoAspect.elements.head.asInstanceOf[Dynamic]
    assert(method.now && method.typeParameters.head.name == "T")
    val source = DracoGenerator.generate(td)
    assert(source.contains("def outer[T]"))
    assert(TypeDefinition.encoder(Drake.parse(Drake.emit(td))) == TypeDefinition.encoder(td))
  }

  test("duplicate formals, malformed bounds and legacy name-embedded formals fail explicitly") {
    Seq("f(T, T)", "f(T: Decoder)", "f(T <: )", "f(T <: A <: B)", "f(T >: A >: B)").foreach { name =>
      intercept[IllegalArgumentException](definition(s"dyn $name Unit ()"))
    }
    val method = Dynamic("f[T]", Json.fromString("Unit"))
    intercept[IllegalArgumentException](MethodTypeParameters.validate(method))
    val duplicate = Dynamic("f", Json.fromString("Unit"),
      _typeParameters = Seq(TypeParameter("T"), TypeParameter("T")))
    intercept[IllegalArgumentException](MethodTypeParameters.validate(duplicate))
  }
}
