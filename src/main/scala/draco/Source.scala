package draco

trait Source extends DracoType

object Source extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Source", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Source] = Type[Source] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
