package domains.marine

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Consumer extends DracoType

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Consumer")
    ConsumeReport.ruleType.pattern(k)
    OriginateReport.ruleType.pattern(k)
    k
  }

  def actorType(): ActorType = new Actor[draco.format.dracon.Dracon] {
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    override def receive(ctx: TypedActorContext[draco.format.dracon.Dracon], msg: draco.format.dracon.Dracon): Behavior[draco.format.dracon.Dracon] = {
      lazy val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
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
