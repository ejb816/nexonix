package draco
import org.evrete.api.Knowledge

trait ServiceDomain[T] extends Service[T] {
  val domain: DomainType
}

object ServiceDomain {
  def apply[T] (
                  _domainName: DomainName,
                  _typeDictionary: TypeDictionary = TypeDictionary.Null,
                  _domains: Seq[DomainType] = Seq(),
                  _knowledge: Knowledge
               ) : ServiceDomain[T] = new ServiceDomain[T] {
    override val knowledge: Knowledge = _knowledge
    override val domain: DomainType = DomainType(
       _domainName,
       _typeDictionary,
       _domains
    )
  }
}