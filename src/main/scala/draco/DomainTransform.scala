package draco

trait DomainTransform[S <: DomainType, T <: DomainType] extends Holon[(S, T)]

object DomainTransform extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("DomainTransform", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DomainTransform[_, _]] = Type[DomainTransform[_, _]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
