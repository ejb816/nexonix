package draco
import org.evrete.api.Knowledge

trait Service[T] extends RuleActorBehavior[T] {}

object Service {
  def apply[T] (_knowledge: Knowledge) : Service[T] = new Service[T] {
    override val knowledge: Knowledge = _knowledge
  }
}