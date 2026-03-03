package draco.transform.delta

import draco._
import draco.transform._

trait Delta extends dataModel.DataModel

object Delta {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Delta",
      _namePackage = Seq ("draco", "transform", "delta")
    )
  )
  lazy val typeInstance: Type[Delta] = Type[Delta] (typeDefinition)
  lazy val domainInstance: Domain[Delta] = Domain[Delta] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
