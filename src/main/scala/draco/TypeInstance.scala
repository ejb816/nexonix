package draco

trait TypeInstance extends DracoType {
  val typeInstance: DracoType
}

object TypeInstance extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeInstance",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed (
        _name = "typeInstance",
        _valueType = "DracoType"
      )
    )
  )
  lazy val typeInstance: Type[TypeInstance] = Type[TypeInstance] (typeDefinition)
}
