package draco.transform.dataModel

import draco._
import draco.transform.alpha.Alpha
import draco.transform.bravo.Bravo
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge

trait DataModel extends DomainElement {
  override val knowledgeService: KnowledgeService = DomainElement.knowledgeService
  override val knowledge: Knowledge = knowledgeService.newKnowledge("DataModel")
}
object DataModel {
  lazy val dataModel: DataModel = new DataModel {
    val domain: Domain[DataModel] = Domain[DataModel] (
      _domainName = DomainName (
        _typeName = TypeName (
          _name = "DataModel",
          _namePackage = Seq ("draco", "transform", "dataModel")
        ),
        _elementTypeNames = Seq ()
      ),
      _domains = Seq (
        DataModel.dataModel.domain,
        Alpha.alpha.domain,
        Bravo.bravo.domain
      )
    )
  }
}