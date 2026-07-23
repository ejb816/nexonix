
package domains.ethereal

import draco._
import domains._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Creator extends Actor[draco.format.json.JSON]

object Creator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Creator", _namePackage = Seq ("domains", "ethereal")))
  lazy val dracoType: Type[Creator] = Type[Creator] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Creator")
    ConsumeReport.ruleType.pattern.accept(k)
    OriginateReport.ruleType.pattern.accept(k)
    k
  }

  def actorType(consumer: ActorRef[draco.format.json.JSON]): ActorType = new Actor[draco.format.json.JSON] {
    override lazy val typeDefinition: TypeDefinition = Creator.typeDefinition

    override def receive(ctx: TypedActorContext[draco.format.json.JSON], msg: draco.format.json.JSON): Behavior[draco.format.json.JSON] = {
      val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
      session.set("consumer", consumer)
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
