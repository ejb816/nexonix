package draco

trait RuleInstance extends TypeInstance {
  val ruleInstance: RuleType
}

object RuleInstance extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "RuleInstance",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed (
        _name = "ruleInstance",
        _valueType = "RuleType"
      )
    )
  )
  lazy val typeInstance: Type[RuleInstance] = Type[RuleInstance] (typeDefinition)
}