package draco

import draco.base.Base
import draco.primes.Primes
import org.evrete.KnowledgeService
import org.evrete.api.Knowledge

trait Draco extends DomainType {
  private val knowledgeService: KnowledgeService = new KnowledgeService()
  val knowledge: Knowledge = knowledgeService.newKnowledge("Draco Knowledge")
  override val domainName: DomainName
  override val typeDefinition: TypeDefinition
  override val typeDictionary: TypeDictionary
  override val domains: Seq[DomainType]
}

object Draco {
  val typeElementNames: Seq[String] = Seq (
    "ActorBehavior",
    "Dictionary",
    "DomainDictionary",
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

  val domainName: DomainName = DomainName(TypeName("Draco"), typeElementNames)

  private val domainType: DomainType = DomainType (
    _domainName = domainName,
    _typeDictionary = TypeDictionary(domainName),
  )
  lazy val draco: Draco = new Draco {
    override val domainName: DomainName = domainType.domainName
    override val domainDictionary: DomainDictionary = domainType.domainDictionary
    override val typeDefinition: TypeDefinition =  TypeDefinition.load (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
    override val domains: Seq[DomainType] = Seq(
      Base.base,
      Primes.primes
    )
  }
}