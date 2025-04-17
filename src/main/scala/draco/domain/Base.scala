package draco.domain

trait Base extends DomainDictionary

object Base {
  def apply(_typeNames: Seq[TypeName]): Base = new Base {
    val dictionary: DomainDictionary = DomainDictionary (_typeNames)
    override val kvMap: Map[TypeName, TypeDefinition] = dictionary.kvMap
    override val typeNames: Seq[TypeName] = dictionary.typeNames
    override val domains: Map[TypeName, TypeDefinition] = dictionary.domains
  }
}
