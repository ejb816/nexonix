package domains.world

import draco._

/** A named-field 3D Cartesian coordinate (metres). The composition tree exposes
  * `x`, `y`, `z` as fields (messages-as-named-trees), and the factory *also* accepts
  * a positional coordinate value, mapping `(_1, _2, _3) -> (x, y, z)` — so the named
  * surface and draco's positional `Coordinate` substrate compose through the
  * constructor rather than being a choice between them.
  *
  * Frame-agnostic: which frame a `Cartesian` is in (Geocentric/Axial vs
  * Heliocentric/Ecliptic) is fixed by where it sits in the world-fact, not by the
  * coordinate itself. */
trait Cartesian extends DracoType {
  val x: Double
  val y: Double
  val z: Double
}

object Cartesian extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Cartesian", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Cartesian] = Type[Cartesian] (typeDefinition)
  lazy val domainType: Domain[World] = Domain[World] (typeDefinition)

  def apply (
    _x: Double = 0.0,
    _y: Double = 0.0,
    _z: Double = 0.0
  ) : Cartesian = new Cartesian {
    override lazy val x: Double = _x
    override lazy val y: Double = _y
    override lazy val z: Double = _z
    override lazy val typeDefinition: TypeDefinition = Cartesian.typeDefinition
  }

  /** Map a positional coordinate value into the named fields. */
  def apply (value: (Double, Double, Double)) : Cartesian = apply(value._1, value._2, value._3)

  lazy val Null: Cartesian = apply()
}
