package domains.terrestrial

import draco._

trait Location extends Terrestrial {
  val latitude: Double
  val longitude: Double
  val elevationMetres: Int
}

object Location extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Location", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Location] = Type[Location] (typeDefinition)
  lazy val domainType: Domain[Terrestrial] = Domain[Terrestrial] (typeDefinition)

  def apply (
    _latitude: Double = 0.0,
    _longitude: Double = 0.0,
    _elevationMetres: Int = 0
  ) : Location = new Location {
    override lazy val latitude: Double = _latitude
    override lazy val longitude: Double = _longitude
    override lazy val elevationMetres: Int = _elevationMetres
    override lazy val typeDefinition: TypeDefinition = Location.typeDefinition
  }

  lazy val Null: Location = apply()
}
