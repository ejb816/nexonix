package draco.transform.bravo

import draco.transform.dataModel.DataModel
import draco.{Domain, DomainName, TypeName}
import org.evrete.api.Knowledge

trait Bravo extends DataModel {
  override val knowledge: Knowledge = knowledgeService.newKnowledge("Bravo")
}

object Bravo {
  lazy val bravo: Bravo = new Bravo {
    val domain: Domain[Bravo] = Domain[Bravo] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "Bravo",
          _namePackage = Seq ("draco", "transform", "bravo")
        ),
        _elementTypeNames = Seq ()
      )
    )
  }
}