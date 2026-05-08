package draco.base

trait Meters extends Distance[Double] {
  override val name: String = "Meters"
  override val description: String = "Distance or length measure"
}

object Meters extends App {
  lazy val typeDefinition: draco.TypeDefinition = draco.Generator.loadType(draco.TypeName ("Meters", _namePackage = Seq("draco", "base")))
  lazy val dracoType: draco.Type[Meters] = draco.Type[Meters] (typeDefinition)

  def apply (_value: Double) : Meters = new Meters {
    override val value: Double = _value
    override val typeDefinition: draco.TypeDefinition = Meters.typeDefinition
  }
}
