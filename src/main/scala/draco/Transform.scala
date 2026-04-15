package draco

trait Transform[S <: DracoType, T <: DracoType] extends Extensible with Holon[(S, T)]

object Transform extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Transform", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Transform[_, _]] = Type[Transform[_, _]] (typeDefinition)
}
