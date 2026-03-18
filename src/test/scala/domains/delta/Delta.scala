package domains.delta

import domains.dataModel.DataModel
import draco._

trait Delta extends DataModel

object Delta {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Delta",
      _namePackage = Seq ("domains", "delta")
    )
  )
  lazy val typeInstance: Type[Delta] = Type[Delta] (typeDefinition)
  lazy val domainInstance: Domain[Delta] = Domain[Delta] (
    _domainDefinition = DomainDefinition (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
