package draco

trait DomainInstance extends TypeInstance {
  val domainInstance: DomainType
}

object DomainInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainInstance",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed (
        _name = "domainInstance",
        _valueType = "DomainType"
      )
    )
  )
  lazy val typeInstance: Type[DomainInstance] = Type[DomainInstance] (typeDefinition)
}