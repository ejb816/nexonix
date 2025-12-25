package draco.transform.alpha

import draco.transform.dataModel.DataModel
import draco.{Domain, DomainName, TypeName}
import org.evrete.api.Knowledge

trait Alpha extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Alpha")
}

object Alpha {
  lazy val alpha: Alpha = new Alpha {
    val domain: Domain[Alpha] = Domain[Alpha] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Alpha",
          _namePackage = Seq ("draco", "transform", "alpha")
        ),
        _elementTypeNames = Seq ()
      )
    )
  }
}