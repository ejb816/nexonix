package domains.world

import draco._

trait Cartesian extends DracoType {
  val x: Double
  val y: Double
  val z: Double
}

object Cartesian extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Cartesian", _namePackage = Seq ("domains", "world")))
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

  def apply (value: (Double, Double, Double)) : Cartesian = apply(value._1, value._2, value._3)

  lazy val Null: Cartesian = apply()
}
