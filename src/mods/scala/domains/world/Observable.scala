package domains.world

import draco._

trait Observable extends World {
  val geocentric: Cartesian
  val heliocentric: Cartesian
}

object Observable extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Observable", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Observable] = Type[Observable] (typeDefinition)
  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)

  def apply (
    _geocentric: Cartesian = Cartesian.Null,
    _heliocentric: Cartesian = Cartesian.Null
  ) : Observable = new Observable {
    override lazy val geocentric: Cartesian = _geocentric
    override lazy val heliocentric: Cartesian = _heliocentric
    override lazy val typeDefinition: TypeDefinition = Observable.typeDefinition
  }

  lazy val Null: Observable = apply()

  def fromGeodetic (latitude: Double, longitude: Double, heightMetres: Double) : Observable =
    apply(_geocentric = Cartesian(Geodesy.geodeticToEcef(latitude, longitude, heightMetres)))

  def toGeodetic (observable: Observable) : (Double, Double, Double) =
    Geodesy.ecefToGeodetic(observable.geocentric.x, observable.geocentric.y, observable.geocentric.z)
}
