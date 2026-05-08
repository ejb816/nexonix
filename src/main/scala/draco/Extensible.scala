package draco

trait Extensible

object Extensible extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Extensible", _namePackage = Seq("draco")))
  lazy val dracoType: Type[Extensible] = Type[Extensible] (typeDefinition)
}
