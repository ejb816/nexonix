package draco

trait REPL[L] extends Extensible

object REPL extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(
    TypeName ("REPL", _namePackage = Seq ("draco"), _typeParameters = Seq ("L"))
  )
  lazy val typeInstance: Type[REPL[_]] = Type[REPL[_]] (typeDefinition)
  lazy val Null: REPL[Nothing] = new REPL[Nothing] {}
}
