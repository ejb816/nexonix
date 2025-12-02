package draco.base

import draco.base.Base.typeElementNames
import draco.{DomainDictionary, DomainName, DomainType, Draco, TypeDefinition, TypeDictionary, TypeName}

trait Base extends Draco {}

object Base {
  val typeElementNames: Seq[String] = Seq(
    "Cardinal",
    "Cartesian",
    "Coordinates",
    "Cylindrical",
    "Distance",
    "Meters",
    "Nominal",
    "Ordinal",
    "Orientable",
    "Polar",
    "Radians",
    "Rectangular",
    "Rotation",
    "Spacetime",
    "Spherical",
    "Unit"
  )


  val base: Base = new Base {
    override val domainName: DomainName = DomainName(TypeName(_name = "Base", _parent = "draco.base"), typeElementNames)
    val typeDefinition: TypeDefinition = TypeDefinition.load(domainName.typeName)
    val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq ()
    override val domainDictionary: DomainDictionary = Draco.draco.domainDictionary
  }
}
