
package domains.heliocentric

import draco._
import domains._
import domains.cosmocentric._

trait Heliocentric extends Extensible with Cosmocentric 

object Heliocentric extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Heliocentric", _namePackage = Seq("domains", "heliocentric")))
  lazy val typeInstance: Type[Heliocentric] = Type[Heliocentric] (typeDefinition)

  lazy val domainInstance: Domain[Heliocentric] = Domain[Heliocentric] (
    _domainDefinition = TypeDefinition (
      typeDefinition.typeName,
      _elementTypeNames = Seq (
      "Elements",
      "Epoch",
      "Ephemeris"
    )
    )
  )
}
