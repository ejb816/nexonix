package draco.transform.charlie

import draco.transform.dataModel.DataModel
import draco.{DomainName, TypeName}
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Charlie extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Charlie")
}

object Charlie {
  val charlie: Charlie = new Charlie {
    val domain: Domain[Charlie] = domains.Domain[Charlie] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Charlie",
          _namePackage = Seq ("draco", "transform", "charlie")
        ),
        _elementTypeNames = Seq ()
      )
    )
  }
}