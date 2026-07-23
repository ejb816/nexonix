package draco

trait REPL[L] extends DracoType

object REPL extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("REPL", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[REPL[_]] = Type[REPL[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
