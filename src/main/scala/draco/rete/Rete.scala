package draco.rete

import draco._

trait Rete extends DracoType

object Rete extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Rete", _namePackage = Seq ("draco", "rete")))
  lazy val dracoType: Type[Rete] = Type[Rete] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Knowledge", "RhsContext", "StatefulSession")

  lazy val domainType: Domain[Rete] = Domain[Rete] (typeDefinition)
}
