package draco

trait Primal [T] extends DracoType {
  val value : T
}

object Primal extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Primal",
      _namePackage = Seq ("draco"),
      _typeParameters = Seq ("T")
    ),
    _elements = Seq (
      Fixed (
        _name = "value",
        _valueType = "T"
      )
    )
  )
  lazy val typeInstance: Type[Primal[_]] = Type[Primal[_]] (typeDefinition)
}