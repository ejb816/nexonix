package draco.base

import draco._

trait Base extends DomainInstance

object Base extends App with DomainInstance{
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Base",
      _namePackage = Seq ("draco", "base")
    ),
    _derivation = Seq (
      draco.TypeName ("DomainInstance", _namePackage = Seq ("draco"))
    )
  )
  lazy val typeInstance: Type[Base] = Type[Base] (typeDefinition)
  lazy val domainInstance: draco.DomainType = new Domain[Base] {
    override val domainName: draco.DomainName = draco.DomainName(
      _typeName = draco.TypeName(
        _name = "Base",
        _parent = "draco.base"
      ),
      _elementTypeNames = Seq(
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
    override val typeDictionary: TypeDictionary = TypeDictionary(domainName)
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
  }
}
