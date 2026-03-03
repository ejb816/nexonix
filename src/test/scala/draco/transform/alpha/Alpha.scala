package draco.transform.alpha

import draco._
import draco.transform._
trait Alpha extends dataModel.DataModel

object Alpha {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Alpha",
      _namePackage = Seq ("draco", "transform", "alpha")
    )
  )
  lazy val typeInstance: Type[Alpha] = Type[Alpha] (typeDefinition)
  lazy val domainInstance: Domain[Alpha] = Domain[Alpha] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}