package draco

import draco.scalatarget.DefinitionTrees._
import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

class NameResolutionTest extends AnyFunSuite {
  private def domain(name: String, pkg: String, members: Seq[String] = Seq.empty): DomainType = {
    val n = TypeName(name, pkg.split('.').toSeq)
    Domain(TypeDefinition(n, _domainAspect = DomainAspect(n, members)),
      members.map(m => TypeDefinition(TypeName(m, n.namePackage), _domainAspect = DomainAspect(n))))
  }
  private def vocabulary = Map("String" -> TypeName("String"), "Unit" -> TypeName("Unit"), "Int" -> TypeName("Int"))
  private def ontology = DomainOntology(Seq(
    DomainBuilder.define("Dracon", Seq("draco", "format", "dracon")),
    domain("Test", "sample")))
  private def scope = new NameResolution(ontology, vocabulary)
  private val source = """type Envelope
    |  elements
    |    fix content Dracon
    |  factory
    |    parameters
    |      par content Dracon Dracon.Null
    |domain sample Test
    |""".stripMargin

  test("Drake and compact JSON share explicit scope while resolved identities remain qualified") {
    val td = Drake.parse(source, scope)
    assert(td.dracoAspect.elements.head.valueType.json == Json.fromString("draco.format.dracon.Dracon"))
    val json = scope.compactJson(td)
    assert(json.hcursor.downField("dracoAspect").downField("elements").downArray.get[String]("valueType") == Right("Dracon"))
    assert(TypeDefinition.encoder(scope.read(json)) == TypeDefinition.encoder(td))
    assert(Drake.emit(td, scope).contains("fix content Dracon"))
    assert(TypeDefinition.encoder(Drake.parse(Drake.emit(td, scope), scope)) == TypeDefinition.encoder(td))
  }

  test("Scala imports the resolved Dracon identity and the resulting code compiles and runs") {
    val td = Drake.parse(source, scope)
    val generated = DracoGenerator.generate(td, scope)
    assert(generated.contains("import draco.format.dracon.Dracon"))
    assert(generated.contains("val content: Dracon"))
    assert(!generated.contains("val content: draco.format.dracon.Dracon"))
    val compiled = DracoGenerator.compile(generated + """
      |trait Test extends draco.DracoType
      |object Check { def verify(): Boolean = Envelope(Dracon.string("ok")).content == Dracon.string("ok") }
      |""".stripMargin, "Envelope.scala")
    assert(compiled.isRight, compiled.left.toOption.getOrElse(Seq.empty).mkString("\n"))
    val c = DracoGenerator.loadClass(compiled.toOption.get, "sample.Check$")
    assert(c.getMethod("verify").invoke(c.getField("MODULE$").get(null)) == java.lang.Boolean.TRUE)
  }

  test("unknown and ambiguous names fail independently of ontology order; qualification disambiguates") {
    val a = domain("A", "first", Seq("Clash"))
    val b = domain("B", "second", Seq("Clash"))
    Seq(Seq(a, b), Seq(b, a)).foreach { domains =>
      val resolver = new NameResolution(DomainOntology(domains), Map.empty)
      assert(intercept[IllegalArgumentException](resolver.resolveName("Clash")).getMessage.contains("Ambiguous"))
      assert(resolver.resolveName("first.Clash").namePackage == Seq("first"))
      assert(intercept[IllegalArgumentException](resolver.resolveName("Missing")).getMessage.contains("Unknown"))
      intercept[IllegalArgumentException](resolver.resolveName("java.lang.String"))
    }
  }

  test("self, type formals and method formals bind before ontology lookup") {
    val resolver = new NameResolution(DomainOntology(Seq(domain("Test", "sample", Seq("T", "R", "Envelope")))), vocabulary)
    val td = Drake.parse("""type Envelope(T)
      |  elements
      |    fix self Envelope(T)
      |    dyn choose(R) R [
      |      parameters
      |        par input T
      |      body
      |        fix value R input
      |    ]
      |domain sample Test
      |""".stripMargin, resolver)
    assert(td.typeName.typeParameters.head.json == Json.fromString("T"))
    val method = td.dracoAspect.elements(1).asInstanceOf[Dynamic]
    assert(method.valueType.json == Json.fromString("R"))
    assert(method.parameters.head.valueType.json == Json.fromString("T"))
    assert(TypeForm.text(td.dracoAspect.elements.head.valueType) == "sample.Envelope(T)")
  }

  test("bounds and nested type forms resolve structurally") {
    val resolver = new NameResolution(DomainOntology(Seq(domain("Test", "sample", Seq("Bound", "Box")))), vocabulary)
    val td = Drake.parse("""type Envelope(T <: Bound)
      |  elements
      |    dyn choose(R <: Box([T])) (R -> {String, T})
      |domain sample Test
      |""".stripMargin, resolver)
    assert(td.typeName.typeParameters.head.noSpaces.contains("sample.Bound"))
    val method = td.dracoAspect.elements.head.asInstanceOf[Dynamic]
    assert(TypeForm.text(method.typeParameters.head.upperBound) == "sample.Box([T])")
    assert(TypeForm.text(method.valueType) == "R -> {String, T}")
  }

  test("bindings are explicit and ontology names take precedence") {
    val o = DomainOntology(Seq(domain("Test", "sample")))
    intercept[IllegalArgumentException](Drake.parse("type A elements fix x String domain sample Test", new NameResolution(o, Map.empty)))
    val resolver = new NameResolution(o, Map("Text" -> TypeName("String", Seq("java", "lang"))))
    assert(resolver.resolveName("Text").namePath == "java.lang.String")
    assert(resolver.resolveName("java.lang.String").namePath == "java.lang.String")
    val shadow = new NameResolution(DomainOntology(Seq(domain("Test", "sample", Seq("Text")))), resolver.bindings)
    assert(shadow.resolveName("Text").namePath == "sample.Text")
  }

  test("nominal references resolve without same-package guessing") {
    val resolver = new NameResolution(DomainOntology(Seq(domain("Test", "sample"), domain("Other", "other", Seq("Parent")))), Map.empty)
    val td = Drake.parse("type Child from Parent domain sample Test", resolver)
    assert(DracoAspect.parents(td.dracoAspect).head.namePath == "other.Parent")
    assert(Drake.emit(td, resolver).contains("from Parent"))
    assert(TypeDefinition.encoder(Drake.parse(Drake.emit(td, resolver), resolver)) == TypeDefinition.encoder(td))
  }

  test("target shortening leaves strings and comments intact and avoids imported-name collisions") {
    val text = """package sample
      |// draco.format.dracon.Dracon
      |object Example {
      |  val x: draco.format.dracon.Dracon = draco.format.dracon.Dracon.Null
      |  val literal = "draco.format.dracon.Dracon"
      |}
      |""".stripMargin
    val rendered = scope.projectScala(text)
    assert(rendered.contains("val x: Dracon = Dracon.Null"))
    assert(rendered.contains("// draco.format.dracon.Dracon"))
    assert(rendered.contains("\"draco.format.dracon.Dracon\""))
    val collision = text.replace("package sample", "package sample\nimport unrelated.Dracon")
    assert(scope.projectScala(collision).contains("val x: draco.format.dracon.Dracon"))
  }
  test("expression binders and target method formals do not become imported type names") {
    val td = Drake.parse("""type Envelope
      |  elements
      |    dyn identity String [
      |      parameters
      |        par Dracon String
      |      body
      |        fix value String Dracon
      |    ]
      |domain sample Test
      |""".stripMargin, scope)
    val method = td.dracoAspect.elements.head.asInstanceOf[Dynamic]
    assert(method.body.head.value.json == Json.fromString("Dracon"))
    val target = "package sample\nobject X { def f[Dracon](x: draco.format.dracon.Dracon): Unit = () }\n"
    assert(scope.projectScala(target) == target)
    val shadowed = "package sample\nobject X { val Dracon = 1; val x = draco.format.dracon.Dracon.Null }\n"
    assert(scope.projectScala(shadowed) == shadowed)
  }

}
