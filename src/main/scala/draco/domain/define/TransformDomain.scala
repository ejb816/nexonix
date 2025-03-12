package draco.domain.define

import draco.domain.{Base, Dictionary, Domain, Transform, TypeData, TypeName}

trait TransformDomain extends Domain[TypeData] with Transform

object TransformDomain {
  def apply (
              sourceName: TypeName,
              sinkName: TypeName,
              domains: Dictionary[Domain[TypeData]] = Dictionary[Domain[TypeData]]()
             ) : TransformDomain = {
    new TransformDomain {
      val domainName: String = s"${sourceName.name}_To_${sinkName.name}"
      val source: Domain[TypeData] = domains(s"${sourceName}")
      val sink: Domain[TypeData] = domains(s"${sinkName}")
      override val superDomain: Domain[TypeData] = domains(s"${domainName}")
      override val subDomains: Dictionary[Domain[TypeData]] = domains
      override val base: Domain[TypeData] = Base.base
    }
  }
}
