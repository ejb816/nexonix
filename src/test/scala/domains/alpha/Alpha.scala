package domains.alpha

import domains.dataModel.DataModel
import draco._
trait Alpha extends DataModel

object Alpha {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Alpha",
      _namePackage = Seq ("domains", "alpha")
    )
  )
  lazy val typeInstance: Type[Alpha] = Type[Alpha] (typeDefinition)
  lazy val domainInstance: Domain[Alpha] = Domain[Alpha] (
    _domainDefinition = DomainDefinition (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}