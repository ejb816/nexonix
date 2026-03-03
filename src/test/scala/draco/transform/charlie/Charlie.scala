package draco.transform.charlie

import draco._
import draco.transform._

trait Charlie extends dataModel.DataModel

object Charlie {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Charlie",
      _namePackage = Seq ("draco", "transform", "charlie")
    )
  )
  lazy val typeInstance: Type[Charlie] = Type[Charlie] (typeDefinition)
  lazy val domainInstance: Domain[Charlie] = Domain[Charlie] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
