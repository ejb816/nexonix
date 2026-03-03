package draco.dreams

import draco.DomainType

trait Transform {
  val source: DomainType
  val sink:  DomainType
}

object Transform extends App with draco.TypeInstance {
  lazy val typeDefinition: draco.TypeDefinition = draco.TypeDefinition (
    _typeName = draco.TypeName (
      _name = "Transform",
      _namePackage = Seq ("draco", "dreams")
    ),
    _elements = Seq (
      draco.Fixed ("source", "DomainType"),
      draco.Fixed ("sink", "DomainType")
    ),
    _factory = draco.Factory (
      "Transform",
      _parameters = Seq (
        draco.Parameter ("source", "DomainType", ""),
        draco.Parameter ("sink", "DomainType", "")
      )
    )
  )
  lazy val typeInstance: draco.Type[Transform] = draco.Type[Transform] (typeDefinition)

  def apply(
             _source: DomainType,
             _sink: DomainType
           ) : Transform = new Transform {
    override val source: DomainType = _source
    override val sink: DomainType = _sink
  }
}
