package draco.transform.bravo

import draco._
import draco.transform._

trait Bravo extends dataModel.DataModel

object Bravo {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Bravo",
      _namePackage = Seq ("draco", "transform", "bravo")
    )
  )
  lazy val typeInstance: Type[Bravo] = Type[Bravo] (typeDefinition)
  lazy val domainInstance: Domain[Bravo] = Domain[Bravo] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
