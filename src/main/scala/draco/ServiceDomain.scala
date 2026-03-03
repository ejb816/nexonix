package draco
import org.evrete.api.Knowledge

trait ServiceDomain[T] extends Service[T] {
  val domain: DomainType
}

object ServiceDomain extends App with TypeInstance {
  // Provisional until type parameters are handled in TypeName
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ServiceDomain[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("Service[T]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      Fixed ("domain", "DomainType")
    )
  )
  lazy val typeInstance: Type[ServiceDomain[_]] = Type[ServiceDomain[_]] (typeDefinition)

  def apply[T <: DomainType] (
               _domain: T,
               ) : ServiceDomain[T] = new ServiceDomain[T] {
    override val domain: DomainType = _domain
    override val knowledge: Knowledge = Rule.knowledgeService.newKnowledge(s"${_domain.domainName.typeName.name}Service")
  }
}