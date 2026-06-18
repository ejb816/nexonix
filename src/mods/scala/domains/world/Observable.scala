package domains.world

import draco._

/** The world-fact: the objective thing the media's reports are reports *of*, the
  * meaning-invariant a transform must preserve. A `Holon` is the perspective that
  * observes; an `Observable` is what is observed. It carries the canonical position
  * in BOTH frames simultaneously — `geocentric` (Geocentric/Axial, ECEF) and
  * `heliocentric` (Heliocentric/Ecliptic) — and will later grow identity / velocity
  * / time as the tracked thing acquires more state. Heliocentric is `Cartesian.Null`
  * until the first crossing to Ethereal. */
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

  /** Geocentric/Axial input-adapter primitive: geodetic (degrees, metres) -> Observable.
    * Uses Cartesian's positional-value constructor to map the ECEF triple into x/y/z. */
  def fromGeodetic (latitude: Double, longitude: Double, heightMetres: Double) : Observable =
    apply(_geocentric = Cartesian(Geodesy.geodeticToEcef(latitude, longitude, heightMetres)))

  /** Geocentric/Axial output-adapter primitive: Observable -> geodetic (degrees, metres). */
  def toGeodetic (observable: Observable) : (Double, Double, Double) =
    Geodesy.ecefToGeodetic(observable.geocentric.x, observable.geocentric.y, observable.geocentric.z)
}
