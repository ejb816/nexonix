package draco

trait ActorType extends DracoType {
  val actorDefinition: TypeDefinition
}

object ActorType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ActorType",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("DracoType", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      Fixed (
        _name = "actorDefinition",
        _valueType = "TypeDefinition"
      )
    )
  )
  lazy val typeInstance: Type[ActorType] = Type[ActorType] (typeDefinition)
}