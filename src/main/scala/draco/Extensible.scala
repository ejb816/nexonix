package draco

trait Extensible

object Extensible extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition.load(TypeName ("Extensible", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Extensible] = Type[Extensible] (typeDefinition)
}
