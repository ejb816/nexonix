
package domains.terrestrial

import draco._
import domains._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Creator extends Actor[draco.format.json.Json]

object Creator extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Creator", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Creator] = Type[Creator] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("OriginateReport")

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Creator")
    OriginateReportRule.ruleType.pattern.accept(k)
    k
  }

  def actorType(consumer: ActorRef[draco.format.json.Json]): ActorType = new Actor[draco.format.json.Json] {
    override lazy val typeDefinition: TypeDefinition = Creator.typeDefinition

    override def receive(ctx: TypedActorContext[draco.format.json.Json], msg: draco.format.json.Json): Behavior[draco.format.json.Json] = {
      val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
      session.set("consumer", consumer)
      session.insert(Seq(msg): _*)
      session.fire()
      session.close()
      Behaviors.same[draco.format.json.Json]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.json.Json], signal: Signal): Behavior[draco.format.json.Json] = {
      Behaviors.same[draco.format.json.Json]
    }
  }
}
