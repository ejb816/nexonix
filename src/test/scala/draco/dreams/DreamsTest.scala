package draco.dreams

import draco._
import draco.dreams.editor.Editor
import draco.dreams.user.User
import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

/** Staged definitions are outside the main-corpus discovery gates. Dreams and User
  * remain empty anchors; Editor gained members and an actor on 2026-10-09 and has
  * its own suite, `draco.dreams.editor.EditorTest`. */
class DreamsTest extends AnyFunSuite {
  private val skeletons = Seq(
    ("Dreams", Seq("draco", "dreams"), () => Dreams.typeDefinition, () => Dreams.domainType),
    ("User", Seq("draco", "dreams", "user"), () => User.typeDefinition, () => User.domainType)
  )
  private def read(path: String): String = new String(Files.readAllBytes(Paths.get(path)), UTF_8)

  skeletons.foreach { case (name, pkg, companion, domain) =>
    val stem = s"${pkg.mkString("/")}/$name"
    def definition: TypeDefinition = io.circe.parser.parse(read(s"src/mods/resources/$stem.json"))
      .flatMap(_.as[TypeDefinition]).fold(error => fail(error.toString), identity)

    test(s"$name Drake and JSON are canonical mutual projections") {
      val source = read(s"src/mods/resources/$stem.drake")
      val parsed = Drake.parse(source)
      assert(parsed.asJson == definition.asJson)
      assert(Drake.emit(definition) == source)
      assert(Drake.emit(parsed) == source)
    }

    test(s"$name Scala is generated from its staged definition") {
      assert(DracoGenerator.generate(definition) == read(s"src/mods/scala/$stem.scala"))
      assert(!Files.exists(Paths.get(s"src/main/scala/$stem.scala")), "duplicate main-tier scaffold")
    }

    test(s"$name companion loads an empty self-declaring domain") {
      val td = companion()
      assert(td.typeName == TypeName(name, pkg))
      assert(td.domainAspect.typeName == td.typeName)
      assert(td.domainAspect.elementTypeNames.isEmpty)
      assert(domain().typeDefinition.typeName == td.typeName)
      assert(domain().typeDictionary.isEmpty)
      assert(DracoAspect.isEmpty(definition.dracoAspect))
      assert(RuleAspect.isEmpty(td.ruleAspect))
      assert(ActorAspect.isEmpty(td.actorAspect))
      assert(CodecAspect.isEmpty(td.codecAspect))
    }

    test(s"DomainBuilder loads and validates $name without a stub") {
      val loaded = DomainBuilder.define(name, pkg)
      assert(loaded.typeDefinition.typeName == definition.typeName)
      assert(!DomainBuilder.isStub(loaded.typeDefinition))
      assert(loaded.typeDictionary.isEmpty)
      assert(DomainBuilder.validate(loaded).isEmpty)
    }

    test(s"DomainBuilder generates only the $name anchor") {
      val generated = DomainBuilder.generate(name, pkg)
      assert(generated.keySet == Set(definition.typeName))
      assert(generated(definition.typeName) == DracoGenerator.generate(definition))
    }
  }

  test("Dreams, Editor and User compose as peers with Draco and Service in one ontology") {
    val anchors = skeletons.map { case (name, pkg, _, _) => DomainBuilder.define(name, pkg) }
    val editor = DomainBuilder.define("Editor", Seq("draco", "dreams", "editor"))
    val core = DomainBuilder.define("Draco", Seq("draco"))
    val service = DomainBuilder.define("Service", Seq("draco", "service"))
    val all = anchors ++ Seq(editor, core, service)
    val ontology = DomainBuilder.ontology(all: _*)
    assert(ontology.size == 5)
    assert(ontology.keys.map(_.typeDefinition.typeName).toSet == all.map(_.typeDefinition.typeName).toSet)
    anchors.foreach { anchor =>
      assert(ontology.get(anchor).exists(_.isEmpty))
      assert(!core.typeDictionary.contains(anchor.typeDefinition.typeName))
      assert(!service.typeDictionary.contains(anchor.typeDefinition.typeName))
    }
    assert(ontology.get(editor).exists(_.size == 2))
    assert(ontology.get(core).exists(_.nonEmpty))
    assert(ontology.get(service).exists(_.size == 2))
    // Package nesting is not membership: Dreams does not define Editor's members.
    assert(!ontology.defines(TypeName("Latent", Seq("draco", "dreams"))))
    assert(ontology.defines(TypeName("Latent", Seq("draco", "dreams", "editor"))))
    assert(ontology.defines(Editor.typeDefinition.typeName))
  }
}
