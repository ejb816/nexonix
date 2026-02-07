package draco

import org.evrete.KnowledgeService
import org.evrete.api.Knowledge
import org.nexonix.domains
import org.nexonix.domains.Domain

trait Draco extends DomainElement {
  val knowledgeService: KnowledgeService = DomainElement.knowledgeService
  val knowledge: Knowledge = knowledgeService.newKnowledge("Draco")
}

object Draco extends App {
  lazy val draco: Draco  = new Draco {
    override val domain: Domain[Draco] = domains.Domain[Draco] (
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
          "Main",
          "Primal",
          "Rule",
          "RuleActorBehavior",
          "RuleDefinition",
          "Service",
          "SourceContent",
          "Test",
          "TypeDefinition",
          "TypeDictionary",
          "TypeElement",
          "TypeName",
          "Value"
        )
      )
    )
    override val typeDefinition: TypeDefinition = TypeDefinition.Null
  }
}
