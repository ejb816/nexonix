package draco.rete

import draco._

trait Knowledge extends DracoType

object Knowledge extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Knowledge", _namePackage = Seq ("draco", "rete")))
  lazy val dracoType: Type[Knowledge] = Type[Knowledge] (typeDefinition)
  lazy val domainType: Domain[Rete] = Domain[Rete] (typeDefinition)
}
