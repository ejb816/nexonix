package draco.domain

trait DomainDictionary extends KeyValueMap[TypeName,TypeDefinition] with Domain

object DomainDictionary {
  private def typeDefinition(tn: TypeName) : TypeDefinition = {

    TypeDefinition.Null
  }
  def apply (
              _typeNames: Seq[TypeName] = Seq(),
              _domains: Map[TypeName, TypeDefinition] = Map[TypeName, TypeDefinition]()
            ) : DomainDictionary = {
    new DomainDictionary {
      override val typeNames: Seq[TypeName] = if (_typeNames.isEmpty) _domains.keys.toSeq else _typeNames
      val kvMap: Map[TypeName,TypeDefinition] = if (_domains.isEmpty) typeNames.foldLeft(Map[TypeName,TypeDefinition]()) {
        (map: Map[TypeName,TypeDefinition], tn: TypeName) =>
          map + (tn -> typeDefinition(tn))
      } else _domains
      override val domains: Map[TypeName, TypeDefinition] = kvMap
    }
  }
}