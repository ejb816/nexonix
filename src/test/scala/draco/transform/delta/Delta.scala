package draco.transform.delta

import draco.transform.dataModel.DataModel
import draco.{DomainName, TypeName}
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Delta extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Delta")
}

object Delta {
  val delta: Delta = new Delta {
    val domain: Domain[Delta] = domains.Domain[Delta] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Delta",
          _namePackage = Seq ("draco", "transform", "delta")
        ),
        _elementTypeNames = Seq ()
      )
    )
  }
}