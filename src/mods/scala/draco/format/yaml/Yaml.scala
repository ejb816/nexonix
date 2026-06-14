package draco.format.yaml

import draco._
import draco.format._

trait Yaml extends Format[String]

object Yaml extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Yaml", _namePackage = Seq ("draco", "format", "yaml")))
  lazy val dracoType: Type[Yaml] = Type[Yaml] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Yaml] = Domain[Yaml] (typeDefinition)
}
