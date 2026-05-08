package draco

trait DracoType extends Extensible {
  val typeDefinition: TypeDefinition
}

object DracoType extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
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
  lazy val dracoType: Type[DracoType] = Type[DracoType] (typeDefinition)
  lazy val Null: DracoType = new DracoType {
    override val typeDefinition: TypeDefinition = TypeDefinition.Null
  }
}
