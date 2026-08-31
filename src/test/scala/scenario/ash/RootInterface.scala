package scenario.ash

import draco._
import scenario._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait RootInterface extends DracoType

object RootInterface extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("RootInterface", _namePackage = Seq ("scenario", "ash")))
  lazy val dracoType: Type[RootInterface] = Type[RootInterface] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("RootInterface")
    scenario.forest.AshBirchAlarm.ruleType.pattern.accept(k)
    scenario.forest.BirchAlarmReceived.ruleType.pattern.accept(k)
    k
  }

  def actorType(): ActorType = new Actor[AshSap] {
    override lazy val typeDefinition: TypeDefinition = RootInterface.typeDefinition

    val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()

    override def receive(ctx: TypedActorContext[AshSap], msg: AshSap): Behavior[AshSap] = {
      msg match { case a: AlarmSignal => session.insert(Seq(a.infochemical): _*); case d: DroughtCue => session.insert(Seq(d.infochemical): _*); case _ => () }
      session.fire()
      Behaviors.same[AshSap]
    }

    override def receiveSignal(ctx: TypedActorContext[AshSap], signal: Signal): Behavior[AshSap] = {
      Behaviors.same[AshSap]
    }
  }
}
