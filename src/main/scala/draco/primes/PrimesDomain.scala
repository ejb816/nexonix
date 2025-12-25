package draco.primes

import draco.{Domain, DomainDictionary, DomainName, DomainType, TypeDefinition, TypeDictionary, TypeName}

trait PrimesDomain extends Domain[Primes] {
  override val domainName: DomainName = DomainName (
    _typeName = TypeName ("Primes", "draco"),
    _elementTypeNames = Seq ()
  )
  override val typeDefinition: TypeDefinition = TypeDefinition(domainName.typeName)
  override val typeDictionary: TypeDictionary = TypeDictionary(domainName)
  override val domains: Seq[DomainType] = Seq ()
  override val domainDictionary: DomainDictionary = DomainDictionary (Seq (this))
}

object PrimesDomain {
  private val domainType: PrimesDomain = new PrimesDomain {}
  def apply () : PrimesDomain = domainType
}