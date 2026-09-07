package draco

trait Target extends DracoType

object Target extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Target", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Target] = Type[Target] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
