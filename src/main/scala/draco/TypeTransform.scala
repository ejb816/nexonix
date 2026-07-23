package draco

trait TypeTransform[S <: DracoType, T <: DracoType] extends Holon[(S, T)]

object TypeTransform extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeTransform", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeTransform[_, _]] = Type[TypeTransform[_, _]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
