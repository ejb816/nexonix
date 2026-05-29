package draco

trait CLI extends DracoType

object CLI extends DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("CLI", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CLI] = Type[CLI] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def main(args: Array[String]): Unit = println("Draco 2.0.0-alpha.1")
}
