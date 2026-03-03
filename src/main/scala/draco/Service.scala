package draco
import org.evrete.api.Knowledge

trait Service[T] extends RuleActorBehavior[T] {}

object Service extends App with TypeInstance {
  // Provisional until type parameters are handled in TypeName
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Service[T]",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("RuleActorBehavior[T]", _namePackage = Seq ("draco"))
    ),
    _factory = Factory (
      "Service[T]",
      _parameters = Seq (
        Parameter ("knowledge", "Knowledge", "")
      )
    )
  )
  lazy val typeInstance: Type[Service[_]] = Type[Service[_]] (typeDefinition)

  def apply[T] (_knowledge: Knowledge) : Service[T] = new Service[T] {
    override val knowledge: Knowledge = _knowledge
  }
}