package draco

trait Generator[T] extends DracoType

object Generator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Generator", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Generator[_]] = Type[Generator[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
