package draco

trait DracoType {
  val typeDefinition: TypeDefinition
}

object DracoType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("DracoType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[DracoType] = Type[DracoType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
