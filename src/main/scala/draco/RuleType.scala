package draco

import org.evrete.api.{Knowledge, RhsContext}

import java.util.function.Consumer

trait RuleType extends DracoType {
  val ruleDefinition: TypeDefinition
  val action: Consumer[RhsContext]
  val pattern: Consumer[Knowledge]
}

object RuleType extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "RuleType",
      _namePackage = Seq ("draco")
    ),
    _derivation =  Seq (
      TypeName (
        _name = "DracoType",
        _namePackage = Seq ("draco")
      )
    ),
    _elements = Seq (
      Fixed (
        _name = "ruleDefinition",
        _valueType = "TypeDefinition"
      )
    )
  )
  lazy val typeInstance: Type[RuleType] = Type[RuleType] (typeDefinition)
  def apply (
            _ruleDefinition: TypeDefinition,
            _pattern: Consumer[Knowledge],
            _action: Consumer[RhsContext]
            ) : RuleType = new RuleType {
    override val ruleDefinition: TypeDefinition = _ruleDefinition
    override val typeDefinition: TypeDefinition = _ruleDefinition
    override val action: Consumer[RhsContext] = _action
    override val pattern: Consumer[Knowledge] = _pattern
  }
}
