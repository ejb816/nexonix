package draco.base

trait Meters extends Distance[Double] {
  override val name: String = "Meters"
  override val description: String = "Distance or length measure"
  override val typeInstance: draco.DracoType = Meters.typeInstance
  override val typeDefinition: draco.TypeDefinition = Meters.typeInstance.typeDefinition
}

object Meters extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Meters", _namePackage = Seq("draco", "base")))
  lazy val typeInstance: draco.Type[Meters] = draco.Type[Meters] (typeDefinition)

  def apply (_value: Double) : Meters = new Meters {
    override val value: Double = _value
  }
}
