package draco

import draco.generator.carrier.DefinitionPath
import draco.scalatarget.DefinitionTrees._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path}

/** Two-stage loading from explicit domain declarations (Dev accepted 2026-10-21).
 *
 *  `DomainBuilder.identities` is stage one: the identity-only ontology of a declared set
 *  of domains, composed from their anchors alone — core's name-only placeholders stand in
 *  for every listed member. `DomainBuilder.load` is stage two: every body is resolved
 *  through `NameResolution` over that identity ontology and an explicit binding
 *  vocabulary, and the populated ontology is composed from the resolved definitions.
 *  Nothing is discovered; the bootstrap files and the generated companions are untouched.
 */
class DomainLoadingTest extends AnyFunSuite {

  /** The canonical domains, as `InitializationTest` supplies them. */
  private val canonical: Seq[TypeName] = Seq(
    "draco.dreams.editor.Editor", "draco.dreams.user.User", "draco.dreams.Dreams", "draco.service.Service",
    "draco.gendrake.GenDrake", "draco.genscala.GenScala", "draco.scalatarget.ScalaTarget",
    "draco.draketarget.DrakeTarget", "draco.generator.carrier.Carrier", "draco.generator.Generator",
    "draco.drake.Drake", "draco.rete.Rete", "draco.format.xml.XML", "draco.format.dracon.Dracon",
    "draco.format.Format", "draco.primes.Primes", "draco.base.Base", "draco.Draco").map(typeName)

  private def typeName(path: String): TypeName = { val parts = path.split('.').toSeq; TypeName(parts.last, parts.dropRight(1)) }

  /** The host names the canonical corpus still spells as leaves, each bound to itself: the
   *  vocabulary is explicit, and realizing these names is a target's concern, not this test's.
   *  `Unit` is the host unit type drake.dlt specifies for every consumer arrow; with Base declared,
   *  the ontology's `draco.base.Unit` takes precedence over this binding (measured 2026-10-21,
   *  the next design point before any bootstrap file is shortened). */
  private val hostVocabulary: Map[String, TypeName] = Seq(
    "Any", "Array", "Boolean", "BufferedSource", "Double", "DracoGenerator", "Enumeration", "Int", "Integer", "Unit",
    "Iterator", "Json", "KnowledgeService", "Long", "Map", "Option", "Product", "Seq", "Some", "String", "System",
    "URI", "URL", "draco.Drake", "draco.Local", "draco.NameResolution", "io.circe.Decoder", "io.circe.Encoder",
    "io.circe.Json", "java.io.File", "java.net.URI", "java.nio.charset.StandardCharsets", "java.nio.file.Files",
    "java.nio.file.Path", "java.nio.file.Paths", "org.apache.pekko.actor.typed.ExtensibleBehavior",
    "org.evrete.api.ActivationMode", "org.evrete.api.Knowledge", "org.evrete.api.StatefulSession",
    "scala.collection.mutable.ArrayBuffer", "scala.io.BufferedSource", "scala.io.Source", "scala.util.Try"
  ).map(n => n -> typeName(n)).toMap

  private def populated: Seq[DomainType] = canonical.map(d => DomainBuilder.define(d.name, d.namePackage))
  private def definitionsOf(o: DomainOntology): Seq[TypeDefinition] =
    o.kvMap.keys.toSeq.sortBy(_.typeDefinition.typeName.namePath).flatMap(d => d.typeDefinition +: d.typeDictionary.elementTypes)
  private def encoded(tds: Seq[TypeDefinition]) = tds.sortBy(_.typeName.namePath).map(TypeDefinition.encoder(_))

  test("stage one: the identity-only ontology from the declared anchors is, as a scope, the populated ontology") {
    val identities = DomainBuilder.identities(canonical)
    val full = DomainOntology(populated)
    assert(identities.kvMap.size == canonical.size)
    assert(identities.kvMap.keys.forall(_.typeDictionary.elementTypes.forall(DomainBuilder.isStub)), "stage one holds placeholders only")
    assert(definitionsOf(identities).map(_.typeName.namePath) == definitionsOf(full).map(_.typeName.namePath),
      "the same identities, keyed bare — parameters compose references, not definitions — with placeholders for bodies")
    val byIdentity = new NameResolution(identities, hostVocabulary)
    val byPopulated = new NameResolution(full, hostVocabulary)
    val all = definitionsOf(full)
    assert(all.nonEmpty)
    all.foreach(td => assert(TypeDefinition.encoder(byIdentity.resolve(td)) == TypeDefinition.encoder(byPopulated.resolve(td)), td.typeName.namePath))
  }

  test("stage two: load composes the canonical corpus resolved, in any declaration order, with every member defined") {
    val loaded = DomainBuilder.load(canonical, hostVocabulary)
    val reversed = DomainBuilder.load(canonical.reverse, hostVocabulary)
    val scope = new NameResolution(DomainBuilder.identities(canonical), hostVocabulary)
    val expected = encoded(populated.flatMap(d => d.typeDefinition +: d.typeDictionary.elementTypes).map(scope.resolve))
    assert(loaded.kvMap.size == canonical.size && reversed.kvMap.size == canonical.size)
    assert(encoded(definitionsOf(loaded)) == expected, "stage two is the resolution of the unscoped load")
    assert(encoded(definitionsOf(reversed)) == expected, "order-independent")
    assert(definitionsOf(loaded).forall(td => !DomainBuilder.isStub(td)), "every declared member has a definition")
    canonical.foreach(d => assert(loaded.holds(d), d.namePath))
    assert(loaded.defines(typeName("draco.format.dracon.Dracon")) && loaded.defines(typeName("draco.TypeName")))
    val element = definitionsOf(loaded).find(_.typeName.namePath == "draco.TypeName").get
      .dracoAspect.elements.find(_.name == "typeParameters").get
    assert(TypeForm.text(element.valueType) == "[draco.format.dracon.Dracon]", "a qualified reference stays qualified")
  }

  test("a body saying Dracon loads with its full identity from an explicit root; nothing undeclared is discovered") {
    val root: Path = Files.createTempDirectory("draco-loading")
    val pkg = root.resolve("example/loading"); Files.createDirectories(pkg)
    // The fixture is written as the tools write it: parsed from drake, encoded to the carrier.
    // The parser qualifies nominal anchors and leaves value types and expressions as authored.
    def write(file: String, source: String): Unit =
      Files.writeString(pkg.resolve(file), TypeDefinition.encoder(Drake.parse(source)).spaces2)
    write("Sample.json", "type Sample\ndomain example loading Sample\n  types [\n    Envelope\n  ]\n")
    write("Envelope.json", "type Envelope\n  elements\n    fix content Dracon\n  factory\n    parameters\n      par content Dracon Dracon.Null\ndomain example loading Sample\n")
    val path = DefinitionPath(DefinitionPath.hostRoots :+ root.toUri)
    val sample = typeName("example.loading.Sample")
    // Every declared body is resolved, and draco's own bodies reach across the whole canonical set
    // (Dracon names Presence, Rule names Knowledge), so the sample joins the canonical declarations.
    val declarations = sample +: canonical
    val loaded = DomainBuilder.load(declarations, hostVocabulary, path)
    val envelope = loaded.holding(sample).fold(fail("Sample not held"), _.typeDictionary.elementTypes.head)
    assert(TypeForm.text(envelope.dracoAspect.elements.head.valueType) == "draco.format.dracon.Dracon")
    assert(envelope.dracoAspect.factory.parameters.head.value.json == io.circe.Json.fromString("draco.format.dracon.Dracon.Null"))
    assert(DracoAspect.parents(envelope.dracoAspect).map(_.namePath) == Seq("draco.DracoType"), "rooted as the companions root")
    // Dracon undeclared: its short name is unknown, whatever the roots hold.
    val undeclared = intercept[IllegalArgumentException](DomainBuilder.load(declarations.filterNot(_.name == "Dracon"), hostVocabulary, path))
    assert(undeclared.getMessage.contains("Unknown type 'Dracon'"), undeclared.getMessage)
    // A declaration nothing defines, or one that is not a domain, is refused outright.
    assert(intercept[IllegalArgumentException](DomainBuilder.identities(Seq(typeName("example.loading.Missing")), path)).getMessage.contains("no definition"))
    assert(intercept[IllegalArgumentException](DomainBuilder.identities(Seq(typeName("example.loading.Envelope")), path)).getMessage.contains("not a domain"))
  }
}
