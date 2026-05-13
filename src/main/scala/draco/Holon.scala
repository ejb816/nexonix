package draco

trait Holon[T <: Product] extends DracoType

object Holon extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Holon", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Holon[_]] = Type[Holon[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
