package draco

trait DracoType {
  val typeDefinition: TypeDefinition
}

object DracoType {
  val typeInstance: Type[DracoType] = new Type[DracoType] {
    override val typeDefinition: TypeDefinition = TypeDefinition (
      _typeName = TypeName (
        _name = "DracoType",
        _namePackage = Seq ("draco")
      ),
      _elements = Seq (
        Fixed (
          _name = "typeDefinition",
          _valueType = "TypeDefinition"
        )
      )
    )
  }
}
