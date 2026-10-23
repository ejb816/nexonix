package domains

import draco._
import java.nio.file.{Files, Paths}
import org.scalatest.funsuite.AnyFunSuite
import scala.jdk.CollectionConverters._

class ExampleMembershipTest extends AnyFunSuite {
  private val roots = Seq("world/World", "aerial/Aerial", "terrestrial/Terrestrial",
    "marine/Marine", "ethereal/Ethereal", "sentient/Sentient")
  private def read(path: java.nio.file.Path): String = new String(Files.readAllBytes(path), "UTF-8")
  private def load(path: java.nio.file.Path): TypeDefinition =
    io.circe.parser.parse(read(path)).flatMap(_.as[TypeDefinition]).fold(throw _, identity)
  private val resourceRoot = Paths.get("src/mods/resources/domains")

  test("every example definition and its declared owner agree on membership") {
    val stream = Files.walk(resourceRoot)
    val definitions = try stream.iterator.asScala.filter(_.toString.endsWith(".json")).map(load).toVector
      finally stream.close()
    val domains = roots.map(stem => load(resourceRoot.resolve(stem + ".json")))
    assert(definitions.map(_.typeName.namePath).distinct.size == definitions.size)
    val byName = definitions.map(td => td.typeName.namePath -> td).toMap
    val owners = domains.map(td => td.typeName.namePath -> td).toMap
    domains.foreach { owner =>
      val names = owner.domainAspect.elementTypeNames
      assert(names.distinct.size == names.size, owner.typeName.namePath)
      names.foreach { name =>
        val identity = TypeName(name, owner.typeName.namePackage).namePath
        assert(byName.contains(identity), s"listed member has no definition: $identity")
        assert(byName(identity).domainAspect.typeName == owner.typeName, identity)
      }
    }
    definitions.filterNot(td => owners.contains(td.typeName.namePath)).foreach { td =>
      val owner = owners.getOrElse(td.domainAspect.typeName.namePath, fail(s"unknown owner: ${td.typeName.namePath}"))
      assert(owner.typeName.namePackage == td.typeName.namePackage, td.typeName.namePath)
      assert(owner.domainAspect.elementTypeNames.contains(td.typeName.name), td.typeName.namePath)
    }
    val scope = DomainBuilder.identities(domains.map(_.typeName))
    val resolution = new NameResolution(scope, Map.empty)
    definitions.foreach(td => assert(resolution.ontological(td.typeName.namePath), td.typeName.namePath))
  }

  test("updated domain membership agrees across Drake, JSON and compiled companions") {
    val compiled = Map(
      "aerial/Aerial" -> aerial.Aerial.elementTypeNames,
      "ethereal/Ethereal" -> ethereal.Ethereal.elementTypeNames,
      "marine/Marine" -> marine.Marine.elementTypeNames,
      "terrestrial/Terrestrial" -> terrestrial.Terrestrial.elementTypeNames,
      "world/World" -> world.World.elementTypeNames)
    compiled.foreach { case (stem, members) =>
      val td = load(resourceRoot.resolve(stem + ".json"))
      val source = read(resourceRoot.resolve(stem + ".drake"))
      assert(td.domainAspect.elementTypeNames == members, stem)
      assert(Drake.emit(td) == source, stem)
      assert(TypeDefinition.encoder(Drake.parse(source)) == TypeDefinition.encoder(td), stem)
    }
  }
}
