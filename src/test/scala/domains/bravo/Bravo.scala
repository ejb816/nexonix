package domains.bravo

import domains.dataModel.DataModel
import draco._

trait Bravo extends DataModel

object Bravo {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Bravo",
      _namePackage = Seq ("domains", "bravo")
    )
  )
  lazy val typeInstance: Type[Bravo] = Type[Bravo] (typeDefinition)
  lazy val domainInstance: Domain[Bravo] = Domain[Bravo] (
    _domainDefinition = DomainDefinition (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
