package draco

trait Primal[T] extends DracoType {
  val value: T
}

object Primal extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Primal", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Primal[_]] = Type[Primal[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
