package domains.aerial

import draco.format.dracon.Dracon

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Creator extends DracoType

object Creator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Creator", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Creator] = Type[Creator] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Creator")
    ConsumeReport.ruleType.pattern(k)
    OriginateReport.ruleType.pattern(k)
    k
  }

  def actorType(_consumer: => Dracon => Unit): ActorType = new Actor[Dracon] {
    override lazy val typeDefinition: TypeDefinition = Creator.typeDefinition
    lazy val consumer: Dracon => Unit = _consumer

    override def receive(ctx: TypedActorContext[Dracon], msg: Dracon): Behavior[Dracon] = {
      lazy val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
      session.set("consumer", consumer)
      session.insert(Seq(msg): _*)
      session.fire()
      session.close()
      Behaviors.same[Dracon]
    }

    override def receiveSignal(ctx: TypedActorContext[Dracon], signal: Signal): Behavior[Dracon] = {
      Behaviors.same[Dracon]
    }
  }
}
