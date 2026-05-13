package draco

trait ActorType extends DracoType {
  val actorDefinition: TypeDefinition
}

object ActorType extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("ActorType", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[ActorType] = Type[ActorType] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)
}
