package draco

trait Specifically[T] extends Extensible

object Specifically extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition.load(TypeName ("Specifically", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val typeInstance: Type[Specifically[_]] = Type[Specifically[_]] (typeDefinition)
}
