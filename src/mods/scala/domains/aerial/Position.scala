package domains.aerial

import draco._

trait Position extends Aerial {
  val latitude: Double
  val longitude: Double
  val altitudeFeet: Int
}

object Position extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Position", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Position] = Type[Position] (typeDefinition)
  lazy val domainType: Domain[Aerial] = Domain[Aerial] (typeDefinition)

  def apply (
    _latitude: Double = 0.0,
    _longitude: Double = 0.0,
    _altitudeFeet: Int = 0
  ) : Position = new Position {
    override lazy val latitude: Double = _latitude
    override lazy val longitude: Double = _longitude
    override lazy val altitudeFeet: Int = _altitudeFeet
    override lazy val typeDefinition: TypeDefinition = Position.typeDefinition
  }

  lazy val Null: Position = apply()
}
