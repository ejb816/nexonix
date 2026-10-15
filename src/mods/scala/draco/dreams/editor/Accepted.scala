package draco.dreams.editor

import draco.dreams._
import draco._

trait Accepted extends DracoType {
  val ontology: draco.DomainOntology
}

object Accepted extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Accepted", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[Accepted] = Type[Accepted] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  def apply (
    _ontology: => draco.DomainOntology
  ) : Accepted = new Accepted {
    override lazy val ontology: draco.DomainOntology = _ontology
    override lazy val typeDefinition: TypeDefinition = Accepted.typeDefinition
  }

  lazy val Null: Accepted = apply(
    _ontology = null.asInstanceOf[draco.DomainOntology]
  )


}
