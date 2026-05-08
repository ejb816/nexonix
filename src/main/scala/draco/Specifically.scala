package draco

trait Specifically[T] extends Extensible

object Specifically extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Specifically", _namePackage = Seq("draco"), _typeParameters = Seq("T")))
  lazy val dracoType: Type[Specifically[_]] = Type[Specifically[_]] (typeDefinition)
}
