package draco.draketarget

import draco._

trait DrakeTarget extends Target

object DrakeTarget extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DrakeTarget", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[DrakeTarget] = Type[DrakeTarget] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Surface", "DomainLine")

  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)
}
