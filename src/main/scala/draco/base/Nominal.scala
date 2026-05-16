package draco.base

import draco._

trait Nominal extends Unit with Primal[String]

object Nominal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Nominal", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Nominal] = Type[Nominal] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
