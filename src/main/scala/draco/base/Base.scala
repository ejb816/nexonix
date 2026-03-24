package draco.base

import draco._

trait Base extends DomainInstance

object Base extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Base",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      TypeName ("DomainInstance", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Base] = Type[Base] (typeDefinition)
  lazy val domainInstance: DomainType = new Domain[Base] {
    override lazy val domainDefinition: TypeDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq (
        "Cardinal",
        "Coordinate",
        "Distance",
        "Meters",
        "Nominal",
        "Ordinal",
        "Radians",
        "Rotation",
        "Unit"
      )
    )
    override lazy val typeDictionary: TypeDictionary = TypeDictionary (domainDefinition)
    override lazy val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
