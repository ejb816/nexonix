package domains.natural

import draco._
import io.circe._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}

trait NaturalActor extends ActorInstance

object NaturalActor extends App with ActorInstance {

  private lazy val actorSourceContent: String = SourceContent (
    _sourceRoot = Test.roots.sourceRoot,
    _logicalPath = "domains/natural/Natural.actor.json"
  ).sourceString

  lazy val typeDefinition: TypeDefinition = Natural.typeDefinition
  lazy val typeInstance: DracoType = Type[Natural] (Natural.typeDefinition)
  lazy val actorInstance: ActorType = new Actor[Natural] {
    override val actorDefinition: TypeDefinition = parser.parse(actorSourceContent).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
    override val typeDefinition: TypeDefinition = Natural.typeDefinition

    override def receive(ctx: TypedActorContext[Natural], msg: Natural): Behavior[Natural] = {
      println(s"msg.value = ${msg.value}")
      Behaviors.same[Natural]
    }

    override def receiveSignal(ctx: TypedActorContext[Natural], msg: Signal): Behavior[Natural] = {
      Behaviors.same[Natural]
    }
  }
}
