package domains.world

import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/** World.Provider — the transform OUTPUT face: re-dispatches a transformed typed
  * value (a World subtype) to the target subdomain's output path. Thin in this
  * first vertical (one configured downstream); type-directed routing across many
  * targets arrives with the JSON-backed transform rules. */
trait Provider extends Actor[World]

object Provider extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Provider", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Provider] = Type[Provider] (typeDefinition)

  def actorType(target: ActorRef[World]): ActorType = new Actor[World] {
    override lazy val actorDefinition: TypeDefinition = Provider.typeDefinition
    override lazy val typeDefinition: TypeDefinition = Provider.typeDefinition

    override def receive(ctx: TypedActorContext[World], msg: World): Behavior[World] = {
      target ! msg
      Behaviors.same[World]
    }

    override def receiveSignal(ctx: TypedActorContext[World], signal: Signal): Behavior[World] = {
      Behaviors.same[World]
    }
  }
}
