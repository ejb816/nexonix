package draco.base

import draco._

trait Unit

object Unit extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Unit", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Unit] = Type[Unit] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
