package domains.charlie

import domains.dataModel.DataModel
import draco._

trait Charlie extends DataModel

object Charlie {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Charlie",
      _namePackage = Seq ("domains", "charlie")
    )
  )
  lazy val typeInstance: Type[Charlie] = Type[Charlie] (typeDefinition)
  lazy val domainInstance: Domain[Charlie] = Domain[Charlie] (
    _domainDefinition = DomainDefinition (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
