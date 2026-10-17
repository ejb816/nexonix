package domains.ethereal

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Creator extends DracoType

object Creator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Creator", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[Creator] = Type[Creator] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Creator")
    ConsumeReport.ruleType.pattern(k)
    OriginateReport.ruleType.pattern(k)
    k
  }

  def actorType(_consumer: => draco.format.dracon.Dracon => Unit): ActorType = new Actor[draco.format.dracon.Dracon] {
    override lazy val typeDefinition: TypeDefinition = Creator.typeDefinition
    lazy val consumer: draco.format.dracon.Dracon => Unit = _consumer

    override def receive(ctx: TypedActorContext[draco.format.dracon.Dracon], msg: draco.format.dracon.Dracon): Behavior[draco.format.dracon.Dracon] = {
      lazy val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
      session.set("consumer", consumer)
      session.insert(Seq(msg): _*)
      session.fire()
      session.close()
      Behaviors.same[draco.format.dracon.Dracon]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.dracon.Dracon], signal: Signal): Behavior[draco.format.dracon.Dracon] = {
      Behaviors.same[draco.format.dracon.Dracon]
    }
  }
}
