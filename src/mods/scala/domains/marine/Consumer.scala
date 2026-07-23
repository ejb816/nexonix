
package domains.marine

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Consumer extends Actor[draco.format.json.JSON]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "marine")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Consumer")
    ConsumeReport.ruleType.pattern.accept(k)
    OriginateReport.ruleType.pattern.accept(k)
    k
  }

  def actorType(): ActorType = new Actor[draco.format.json.JSON] {
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    override def receive(ctx: TypedActorContext[draco.format.json.JSON], msg: draco.format.json.JSON): Behavior[draco.format.json.JSON] = {
      val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
      session.insert(Seq(msg): _*)
      session.fire()
      session.close()
      Behaviors.same[draco.format.json.JSON]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.json.JSON], signal: Signal): Behavior[draco.format.json.JSON] = {
      Behaviors.same[draco.format.json.JSON]
    }
  }
}
