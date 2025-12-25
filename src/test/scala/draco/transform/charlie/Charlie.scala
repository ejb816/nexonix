package draco.transform.charlie

import draco.transform.dataModel.DataModel
import draco.{Domain, DomainName, TypeName}
import org.evrete.api.Knowledge

trait Charlie extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Charlie")
}

object Charlie {
  val charlie: Charlie = new Charlie {
    val domain: Domain[Charlie] = Domain[Charlie] (
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