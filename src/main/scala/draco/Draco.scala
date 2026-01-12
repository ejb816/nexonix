package draco

import org.evrete.KnowledgeService
import org.evrete.api.Knowledge

trait Draco extends DomainElement {
  val knowledgeService: KnowledgeService = DomainElement.knowledgeService
  val knowledge: Knowledge = knowledgeService.newKnowledge("Draco")
}

object Draco {
  lazy val draco: Draco  = new Draco {
    override val domain: Domain[Draco] = Domain[Draco] (
      _domainName = DomainName (
        _typeName = TypeName (
          "Draco"
        ),
        _elementTypeNames = Seq (
          "ActorBehavior",
          "ContentSink",
          "Dictionary",
          "Domain",
          "DomainDictionary",
          "DomainElement",
          "DomainName",
          "DomainType",
          "Generator",
          "Member",
          "Rule",
          "RuleActorBehavior",
          "RuleDefinition",
          "RuleSet",
          "SourceContent",
          "TypeDefinition",
          "TypeDictionary",
          "TypeName",
          "Value"
        )
      )
    )
  }
}
