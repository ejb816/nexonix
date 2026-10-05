package draco.service

import draco._

trait Service extends DracoType

object Service extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Service", _namePackage = Seq ("draco", "service")))
  lazy val dracoType: Type[Service] = Type[Service] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("TextOutput")

  lazy val domainType: Domain[Service] = Domain[Service] (typeDefinition)
}
