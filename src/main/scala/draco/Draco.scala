package draco

trait Draco extends DomainInstance {
  val superDomain: DomainType = DomainType.Null
}

object Draco extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Draco", _namePackage = Seq ("draco")))
  lazy val typeInstance: Type[Draco] = Type[Draco] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Draco] {
    override lazy val domainDefinition: TypeDefinition = typeDefinition
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
