package draco.transform.alpha

import draco.transform.dataModel.DataModel
import draco.{DomainElement, DomainName, Draco, TypeDefinition, TypeName}
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Alpha extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Alpha")
}

object Alpha {
  lazy val alpha: Alpha = new Alpha {
    val domain: Domain[Alpha] = domains.Domain[Alpha] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Alpha",
          _namePackage = Seq ("draco", "transform", "alpha")
        ),
        _elementTypeNames = Seq ()
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition (
      _typeName = TypeName (
        _name = "Alpha",
        _namePackage = Seq ("draco", "transform", "alpha")
      )
    )
  }
}