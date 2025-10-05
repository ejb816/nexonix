package draco

trait Draco extends DomainType {
  private val elementTypeNames: Seq[String] = Seq (
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
  override val domainName: DomainName = DomainName ( TypeName ("Draco"), elementTypeNames)
  override val subDomainNames: Seq[String] = Seq ()
}

object Draco {
  lazy val draco: Draco = new Draco {
    override val typeDefinition: TypeDefinition = TypeDefinition.load (domainName.typeName)
    override val typeDictionary: TypeDictionary = TypeDictionary (domainName)
  }
}