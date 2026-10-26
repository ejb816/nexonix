package domains.world

import draco._
import org.scalatest.funsuite.AnyFunSuite

/** A NAMED-argument construction spells its head as the author spelled it — or as the scope in
  * effect spells it — exactly as a positional call does (2026-10-26). The engine used to spell the
  * slot type's LAST SEGMENT instead, so a construction of another package's type was emitted bare
  * with no import to resolve it: `domains.terrestrial.Location(latitude:…)` authored in
  * domains.world became `Location(_latitude = …)`, which scalac refuses. The probe is authored in
  * domains.world and constructs domains.terrestrial.Location; the generated file imports `draco._`
  * and `domains._` only, so no import masks how the head is spelled, and each generation is
  * compiled and run, which is the one judge of whether the head resolves. The accepted Consumer
  * keeps its positional call; this probe is the named form of the same construction. */
class NamedConstructionTest extends AnyFunSuite {
  private val probe =
    """type Probe
      |  globals
      |    dyn make domains.terrestrial.Location [
      |      body
      |        fix built domains.terrestrial.Location domains.terrestrial.Location(latitude:1.5, longitude:-2.5, elevationMetres:7)
      |        fix value domains.terrestrial.Location built
      |    ]
      |domain domains world World
      |""".stripMargin

  private val call = "Location(_latitude = 1.5, _longitude = -2.5, _elevationMetres = 7)"

  private lazy val td: TypeDefinition = Drake.parse(probe)

  /** The example caller's scope: canonical and example domain identities plus its explicit bindings. */
  private lazy val scope: NameResolution =
    new NameResolution(DomainBuilder.identities(domains.ExampleScope.declarations), domains.ExampleScope.bindings)

  private def noMaskingImport(source: String): Unit =
    assert(!source.contains("import domains.terrestrial._"), s"a wildcard import would mask the head's spelling:\n$source")

  private def compileAndMake(source: String, label: String): domains.terrestrial.Location = {
    val compiled = DracoGenerator.compile(source, "Probe.scala")
    assert(compiled.isRight, s"$label generation did not compile:\n${compiled.left.getOrElse(Seq.empty).mkString("\n")}\n$source")
    val companion = DracoGenerator.loadClass(compiled.toOption.get, "domains.world.Probe$")
    val module = companion.getField("MODULE$").get(null)
    companion.getMethod("make").invoke(module).asInstanceOf[domains.terrestrial.Location]
  }

  private def constructed(location: domains.terrestrial.Location): Unit = {
    assert(location.latitude == 1.5)
    assert(location.longitude == -2.5)
    assert(location.elevationMetres == 7)
  }

  test("unscoped: a cross-package named construction keeps its qualified head, compiles and constructs") {
    val source = DracoGenerator.generate(td)
    assert(source.contains(s"= domains.terrestrial.$call"), source)
    noMaskingImport(source)
    constructed(compileAndMake(source, "unscoped"))
  }

  test("scoped: the head is spelled as the example scope spells it, and resolves in the generated file") {
    val source = DracoGenerator.generate(td, scope)
    val qualified = source.contains(s"= domains.terrestrial.$call")
    val imported  = source.contains(s"= $call") && source.contains("import domains.terrestrial.Location")
    assert(qualified || imported, source)
    noMaskingImport(source)
    constructed(compileAndMake(source, "scoped"))
  }

  test("own package: a head authored bare stays bare, as aerial/Input spells its Position") {
    val authored =
      """type Probe
        |  globals
        |    dyn make domains.aerial.Position [
        |      body
        |        fix built domains.aerial.Position Position(latitude:1.0, longitude:2.0, altitudeFeet:3)
        |        fix value domains.aerial.Position built
        |    ]
        |domain domains aerial Aerial
        |""".stripMargin
    val source = DracoGenerator.generate(Drake.parse(authored))
    assert(source.contains("= Position(_latitude = 1.0, _longitude = 2.0, _altitudeFeet = 3)"), source)
    assert(!source.contains("domains.aerial.Position(_latitude"), source)
  }
}
