package draco

import org.evrete.KnowledgeService
import org.evrete.api.Knowledge

trait DomainElement {
  val knowledgeService: KnowledgeService
  val knowledge: Knowledge
  val domain: DomainType
}

object DomainElement {
  val knowledgeService: KnowledgeService = new KnowledgeService()
}