package draco

import org.evrete.api.Knowledge

trait RuleActorBehavior[T] extends ActorBehavior[T] {
  val knowledge: Knowledge
}

object RuleActorBehavior extends App with TypeInstance {
  // Provisional until type parameters are handled in TypeName
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "RuleActorBehavior[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("ActorBehavior[T]", _namePackage = Seq ("draco"))
    ),
    _elements = Seq (
      Fixed ("knowledge", "Knowledge")
    ),
    _factory = Factory (
      "RuleActorBehavior[T]",
      _parameters = Seq (
        Parameter ("knowledge", "Knowledge", "")
      )
    )
  )
  lazy val typeInstance: Type[RuleActorBehavior[_]] = Type[RuleActorBehavior[_]] (typeDefinition)

  def apply[T](_knowledge: Knowledge): RuleActorBehavior[T] = new RuleActorBehavior[T] {
    override val knowledge: Knowledge = _knowledge
  }
}