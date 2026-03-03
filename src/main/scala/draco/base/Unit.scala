package draco.base

trait Unit extends draco.DracoType {
  val name: String = "Unit"
  val description: String = "Abstract Unit root"
}

object Unit extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Unit",
      _namePackage = Seq ("draco", "base")
    ),
    _elements = Seq (
      draco.Fixed (
        _name = "name",
        _valueType = "String"
      ),
      draco.Fixed (
        _name = "description",
        _valueType = "String"
      )
    )
  )
  lazy val typeInstance: draco.Type[Unit] = draco.Type[Unit] (typeDefinition)
}
