package draco.generator.carrier

import draco.generator._
import draco._
import java.net.URI
import java.net.URL

trait DefinitionPath extends DracoType {
  val roots: Seq[URI]
  def sources(resourcePath: String): Seq[URL] = roots.map(root => new URL(root.toString + resourcePath.stripPrefix("/"))).filter(url => scala.util.Try(url.openStream().close()).isSuccess)
  def source(resourcePath: String): Option[URL] = {
    val found: Seq[URL] = sources(resourcePath)
    require(found.size <= 1, s"draco: $resourcePath is defined at ${found.size} roots, and a type name must resolve to exactly one definition: " + found.mkString(", "))
    found.headOption
  }
}

object DefinitionPath extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DefinitionPath", _namePackage = Seq ("draco", "generator", "carrier")))
  lazy val dracoType: Type[DefinitionPath] = Type[DefinitionPath] (typeDefinition)
  lazy val domainType: Domain[Carrier] = Domain[Carrier] (typeDefinition)

  def apply (
    _roots: Seq[URI] = Seq.empty
  ) : DefinitionPath = new DefinitionPath {
    override lazy val roots: Seq[URI] = _roots
    override lazy val typeDefinition: TypeDefinition = DefinitionPath.typeDefinition
  }

  lazy val Null: DefinitionPath = apply()

  lazy val hostRoots: Seq[URI] = System.getProperty("java.class.path", "").split(java.io.File.pathSeparatorChar).filter(_.nonEmpty).map(entry => new java.io.File(entry)).map(file => if (file.getName.endsWith(".jar")) URI.create("jar:" + file.toURI.toString + "!/") else URI.create(if (file.toURI.toString.endsWith("/")) file.toURI.toString else file.toURI.toString + "/")).toSeq
  lazy val default: DefinitionPath = DefinitionPath(hostRoots)
}
