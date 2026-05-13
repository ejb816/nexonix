package draco

trait REPL[L]

object REPL extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("REPL", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[REPL[_]] = Type[REPL[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
