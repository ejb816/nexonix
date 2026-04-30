package domains.egocentric

import draco._
import io.circe._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}

trait EgoActor extends ActorInstance

object EgoActor extends App with ActorInstance {

  private lazy val actorSourceContent: String = SourceContent (
    _sourceRoot = Test.roots.sourceRoot,
    _logicalPath = "domains/egocentric/Ego.actor.json"
  ).sourceString

  lazy val typeDefinition: TypeDefinition = Ego.typeDefinition
  lazy val typeInstance: DracoType = Type[Ego] (Ego.typeDefinition)
  lazy val actorInstance: ActorType = new Actor[Ego] {
    override val actorDefinition: TypeDefinition = parser.parse(actorSourceContent).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
    override val typeDefinition: TypeDefinition = Ego.typeDefinition

    override def receive(ctx: TypedActorContext[Ego], msg: Ego): Behavior[Ego] = {
      Behaviors.same[Ego]
    }

    override def receiveSignal(ctx: TypedActorContext[Ego], msg: Signal): Behavior[Ego] = {
      Behaviors.same[Ego]
    }
  }
}
