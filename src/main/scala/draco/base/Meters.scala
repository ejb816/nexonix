package draco.base

import draco._

trait Meters extends Distance[Double]

object Meters extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Meters", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Meters] = Type[Meters] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)

  def apply (
    _value: Double
  ) : Meters = new Meters {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Meters.typeDefinition
  }

  lazy val Null: Meters = apply(
    _value = 0.0
  )

}
