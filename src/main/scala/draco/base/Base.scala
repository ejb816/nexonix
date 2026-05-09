package draco.base

import draco._

trait Base extends DracoType

object Base extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Base", _namePackage = Seq ("draco", "base")))
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
