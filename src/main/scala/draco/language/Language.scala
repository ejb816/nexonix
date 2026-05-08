package draco.language

import draco._

trait Language extends Extensible

object Language extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Language", _namePackage = Seq ("draco", "language")))
  lazy val domainType: Domain[Language] = Domain[Language] (typeDefinition)
}
