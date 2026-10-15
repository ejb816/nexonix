package draco.dreams

import draco._
import draco.dreams.editor.{Actual, Editor}
import draco.dreams.user.{Create, Delete, Latent, Read, Request, Response, Update, User}
import io.circe.syntax._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

/** Staged definitions are outside the main-corpus discovery gates. Dreams remains an
  * empty anchor; Editor gained members and an actor on 2026-10-09 and has its own
  * suite, `draco.dreams.editor.EditorTest`. Since 2026-10-12 User holds the editing
  * REQUEST vocabulary (Dev's CRUD reading: User defines the requests, Editor the
  * responses): `Latent`, the Create/Update request, moved here from Editor, and
  * `Response` is the abstract reply — `problems`, `accepted` — that Editor's `Actual`
  * derives, so the dependency between the two domains runs one way. */
class DreamsTest extends AnyFunSuite {
  private def read(path: String): String = new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  private def definitionAt(stem: String): TypeDefinition = io.circe.parser.parse(read(s"src/mods/resources/$stem.json"))
    .flatMap(_.as[TypeDefinition]).fold(error => fail(error.toString), identity)

  private val userPkg = Seq("draco", "dreams", "user")
  private val trios = Seq(
    ("Dreams", Seq("draco", "dreams"), () => Dreams.typeDefinition),
    ("User", userPkg, () => User.typeDefinition),
    ("Create", userPkg, () => Create.typeDefinition),
    ("Delete", userPkg, () => Delete.typeDefinition),
    ("Latent", userPkg, () => Latent.typeDefinition),
    ("Read", userPkg, () => Read.typeDefinition),
    ("Request", userPkg, () => Request.typeDefinition),
    ("Response", userPkg, () => Response.typeDefinition),
    ("Update", userPkg, () => Update.typeDefinition)
  )

  trios.foreach { case (name, pkg, companion) =>
    val stem = s"${pkg.mkString("/")}/$name"

    test(s"$name Drake and JSON are canonical mutual projections") {
      val definition = definitionAt(stem)
      val source = read(s"src/mods/resources/$stem.drake")
      val parsed = Drake.parse(source)
      assert(parsed.asJson == definition.asJson)
      assert(Drake.emit(definition) == source)
      assert(Drake.emit(parsed) == source)
    }

    test(s"$name Scala is generated from its staged definition") {
      val definition = definitionAt(stem)
      assert(DracoGenerator.generate(definition) == read(s"src/mods/scala/$stem.scala"))
      assert(!Files.exists(Paths.get(s"src/main/scala/$stem.scala")), "duplicate main-tier scaffold")
      assert(companion().typeName == definition.typeName)
    }
  }

  test("Dreams is the constructing layer: its one member is the Initialization client, no rule, actor or codec of its own") {
    val td = Dreams.typeDefinition
    assert(td.typeName == TypeName("Dreams", Seq("draco", "dreams")))
    assert(td.domainAspect.typeName == td.typeName)
    assert(Dreams.elementTypeNames == Seq("Initialization"))
    assert(Dreams.domainType.typeDictionary.size == 1)
    assert(DracoAspect.isEmpty(definitionAt("draco/dreams/Dreams").dracoAspect))
    assert(RuleAspect.isEmpty(td.ruleAspect) && ActorAspect.isEmpty(td.actorAspect) && CodecAspect.isEmpty(td.codecAspect))
    val loaded = DomainBuilder.define("Dreams", Seq("draco", "dreams"))
    assert(!DomainBuilder.isStub(loaded.typeDefinition))
    assert(loaded.typeDictionary.elementTypes.forall(m => !DomainBuilder.isStub(m)))
    assert(DomainBuilder.validate(loaded).isEmpty)
    assert(DomainBuilder.generate("Dreams", Seq("draco", "dreams")).keySet == Set(td.typeName, Initialization.typeDefinition.typeName))
  }

  test("User is the request domain: the Request family as verbs (Create, Update, Read, Delete) over the Latent shape, and Response") {
    val td = User.typeDefinition
    assert(td.typeName == TypeName("User", userPkg))
    assert(td.domainAspect.typeName == td.typeName)
    assert(User.elementTypeNames == Seq("Create", "Delete", "Latent", "Read", "Request", "Response", "Update"))
    assert(User.domainType.typeDictionary.size == 7)
    assert(RuleAspect.isEmpty(td.ruleAspect) && ActorAspect.isEmpty(td.actorAspect) && CodecAspect.isEmpty(td.codecAspect))
    Seq(Create, Delete, Latent, Read, Request, Response, Update).foreach(m => assert(m.typeDefinition.domainAspect.typeName == td.typeName))
    // Request is the abstract head of the family (2026-10-13): no elements, no factory.
    // Latent is the candidate SHAPE (definition + members), abstract since 2026-10-14, and
    // Create and Update derive it — distinct intents, each refusing the other's case; Read
    // and Delete derive Request directly. The Editor's message type is Request.
    // Since 2026-10-15 a request knows what it PROPOSES and what it REPORTS ON: three bodiless
    // dyns on Request — proposal, subject, contents, each over the accepted ontology — that
    // every verb implements, so the Editor actor needs no dispatch of its own.
    assert(Request.typeDefinition.dracoAspect.elements.map(_.name) == Seq("proposal", "subject", "contents"))
    assert(Request.typeDefinition.dracoAspect.factory.parameters.isEmpty)
    Seq(Latent, Read, Delete).foreach(m => assert(DracoAspect.parents(m.typeDefinition.dracoAspect) == Seq(Request.typeDefinition.typeName)))
    Seq(Create, Update).foreach(m => assert(DracoAspect.parents(m.typeDefinition.dracoAspect) == Seq(Latent.typeDefinition.typeName)))
    assert(Latent.typeDefinition.dracoAspect.elements.map(_.name) == Seq("definition", "members") && Latent.typeDefinition.dracoAspect.factory.parameters.isEmpty)
    Seq(Read, Delete).foreach(m => assert(m.typeDefinition.dracoAspect.elements.map(_.name) == Seq("domain", "proposal", "subject", "contents"),
      "a Read or Delete names one domain and knows what it proposes and reports on"))
    val loaded = DomainBuilder.define("User", userPkg)
    assert(loaded.typeDictionary.elementTypes.forall(m => !DomainBuilder.isStub(m)))
    assert(DomainBuilder.validate(loaded).isEmpty)
    assert(DomainBuilder.generate("User", userPkg).keySet ==
      Set(td.typeName, Create.typeDefinition.typeName, Delete.typeDefinition.typeName, Latent.typeDefinition.typeName,
        Read.typeDefinition.typeName, Request.typeDefinition.typeName, Response.typeDefinition.typeName, Update.typeDefinition.typeName))
  }

  test("Response is the abstract reply: elements without a factory, accepted iff no problems; Actual is one") {
    val td = Response.typeDefinition
    assert(td.dracoAspect.elements.map(_.name) == Seq("problems", "accepted"))
    assert(td.dracoAspect.factory.valueType.isNull && td.dracoAspect.factory.parameters.isEmpty,
      "an abstract shape has no factory of its own")
    assert(DracoAspect.parents(definitionAt("draco/dreams/user/Response").dracoAspect).isEmpty, "no authored parent")
    assert(DracoAspect.parents(td.dracoAspect) == Seq(TypeName("DracoType", Seq("draco"))), "rooted at the universal root on load")
    val problem = Problem(TypeName("X", userPkg), "example")
    val replies: Seq[Response] = Seq(Actual(TypeDefinition(TypeName("X", userPkg))), Actual(TypeDefinition(TypeName("X", userPkg)), Seq.empty, Seq(problem)))
    assert(replies.map(_.accepted) == Seq(true, false), "a user reads acceptance from the Response alone")
    assert(replies.last.problems.map(_.message) == Seq("example"))
  }

  test("Dreams, Editor and User compose as peers with Draco and Service in one ontology") {
    val dreams = DomainBuilder.define("Dreams", Seq("draco", "dreams"))
    val user = DomainBuilder.define("User", userPkg)
    val editor = DomainBuilder.define("Editor", Seq("draco", "dreams", "editor"))
    val core = DomainBuilder.define("Draco", Seq("draco"))
    val service = DomainBuilder.define("Service", Seq("draco", "service"))
    val all = Seq(dreams, user, editor, core, service)
    val ontology = DomainBuilder.ontology(all: _*)
    assert(ontology.size == 5)
    assert(ontology.keys.map(_.typeDefinition.typeName).toSet == all.map(_.typeDefinition.typeName).toSet)
    assert(ontology.get(dreams).exists(_.size == 1))
    assert(!core.typeDictionary.contains(dreams.typeDefinition.typeName))
    assert(!service.typeDictionary.contains(dreams.typeDefinition.typeName))
    assert(ontology.get(user).exists(_.size == 7))
    assert(ontology.get(editor).exists(_.size == 6))
    assert(ontology.get(core).exists(_.nonEmpty))
    assert(ontology.get(service).exists(_.size == 2))
    // Package nesting is not membership: Dreams does not define User's members, and
    // Latent is User's request now, not Editor's.
    assert(!ontology.defines(TypeName("Latent", Seq("draco", "dreams"))))
    assert(!ontology.defines(TypeName("Latent", Seq("draco", "dreams", "editor"))))
    assert(ontology.defines(TypeName("Latent", userPkg)))
    assert(ontology.defines(Editor.typeDefinition.typeName))
    // Every loaded definition is rooted at DracoType, so each dreams domain depends on
    // Draco; the one edge AMONG the dreams domains runs from Editor to User, never back.
    val names = (d: DomainType) => ontology.dependencies(d).map(_.typeDefinition.typeName.name).toSet
    assert(names(editor) == Set("User", "Draco"))
    assert(names(user) == Set("Draco") && names(dreams) == Set("Draco"))
  }
}
