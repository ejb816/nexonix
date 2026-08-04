package draco.rete

import draco._

trait RhsContext extends DracoType

object RhsContext extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RhsContext", _namePackage = Seq ("draco", "rete")))
  lazy val dracoType: Type[RhsContext] = Type[RhsContext] (typeDefinition)
  lazy val domainType: Domain[Rete] = Domain[Rete] (typeDefinition)
}
