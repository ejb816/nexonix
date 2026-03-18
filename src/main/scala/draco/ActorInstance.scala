package draco

trait ActorInstance extends TypeInstance {
  val actorInstance: ActorType
}

object ActorInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ActorInstance",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed (
        _name = "actorInstance",
        _valueType = "ActorType"
      )
    )
  )
  lazy val typeInstance: Type[ActorInstance] = Type[ActorInstance] (typeDefinition)
}
