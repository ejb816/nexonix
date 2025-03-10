package draco.domain.define

import draco.domain.{Base, Domain, Transform, TypeData, TypeName}

trait TransformDomain extends Domain[TypeData] with Transform

object TransformDomain {
  def apply (
              sourceName: TypeName,
              sinkName: TypeName,
              domainName: TypeName,
              domains: DataDomainDictionary
             ) : TransformDomain = {
    new TransformDomain {
      val source: Domain[TypeData] = domains(s"${sourceName}")
      val sink: Domain[TypeData] = domains(s"${sinkName}")
      override val superDomain: Domain[TypeData] = domains(s"${domainName.name}")
      override val subDomains: DataDomainDictionary = domains
      override val base: Domain[TypeData] = Base.base
    }
  }
}
