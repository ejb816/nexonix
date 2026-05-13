package draco.language

import draco._

trait Language

object Language extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Language", _namePackage = Seq ("draco", "language")))
  lazy val dracoType: Type[Language] = Type[Language] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("YAML")

  lazy val domainType: Domain[Language] = Domain[Language] (typeDefinition)
}
