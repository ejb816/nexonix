package draco

trait DomainElement {
  val knowledgeService: org.evrete.KnowledgeService
  val knowledge: org.evrete.api.Knowledge
  val domain: DomainType
  val typeDefinition: TypeDefinition = TypeDefinition.Null
}

object DomainElement {
  val knowledgeService: org.evrete.KnowledgeService = new org.evrete.KnowledgeService()
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "DomainElement",
      _namePackage = Seq (
        "draco"
      )
    ),
    _elements = Seq (
      Fixed (
        _name = "knowledgeService",
        _valueType = "org.evrete.KnowledgeService"
      ),
      Fixed (
        _name = "knowledge",
        _valueType = "org.evrete.api.Knowledge"
      ),
      Fixed (
        _name = "domain",
        _valueType = "DomainType"
      ),
      Fixed (
        _name = "typeDefinition",
        _valueType = "TypeDefinition"
      )
    )
  )
}