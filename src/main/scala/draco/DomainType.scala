package draco

trait DomainType extends DracoType {
  val typeDictionary: TypeDictionary
}

object DomainType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("DomainType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainType] = Type[DomainType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
