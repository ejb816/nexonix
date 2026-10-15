package draco.dreams.user

import draco.dreams._
import draco._

trait Request extends DracoType {
  def proposal(_accepted: => draco.DomainOntology): draco.DomainOntology
  def subject(_accepted: => draco.DomainOntology): draco.TypeDefinition
  def contents(_accepted: => draco.DomainOntology): Seq[draco.TypeDefinition]
}

object Request extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Request", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Request] = Type[Request] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)
}
