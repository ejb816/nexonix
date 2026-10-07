package draco.dreams

import draco._

trait Dreams extends DracoType

object Dreams extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Dreams", _namePackage = Seq ("draco", "dreams")))
  lazy val dracoType: Type[Dreams] = Type[Dreams] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Dreams] = Domain[Dreams] (typeDefinition)
}
