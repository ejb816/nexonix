package domains.world

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Provider extends Actor[domains.world.World]

object Provider extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Provider", _namePackage = Seq ("domains", "world")))
  lazy val dracoType: Type[Provider] = Type[Provider] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = Rule.knowledgeService.newKnowledge("Provider")

  def actorType(_target: => domains.world.World => Unit): ActorType = new Actor[World] {
    override lazy val typeDefinition: TypeDefinition = Provider.typeDefinition
    lazy val target: domains.world.World => Unit = _target

    override def receive(ctx: TypedActorContext[World], msg: World): Behavior[World] = {
      target(msg)
      Behaviors.same[World]
    }

    override def receiveSignal(ctx: TypedActorContext[World], signal: Signal): Behavior[World] = {
      Behaviors.same[World]
    }
  }
}
