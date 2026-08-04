package draco.rete

import draco._

trait StatefulSession extends DracoType

object StatefulSession extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("StatefulSession", _namePackage = Seq ("draco", "rete")))
  lazy val dracoType: Type[StatefulSession] = Type[StatefulSession] (typeDefinition)
  lazy val domainType: Domain[Rete] = Domain[Rete] (typeDefinition)
}
