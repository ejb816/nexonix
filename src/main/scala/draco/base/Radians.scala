package draco.base

import draco._

trait Radians extends Rotation[Double]

object Radians extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Radians", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Radians] = Type[Radians] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)

  def apply (
    _value: Double
  ) : Radians = new Radians {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Radians.typeDefinition
  }

  lazy val Null: Radians = apply(
    _value = 0.0
  )

}
