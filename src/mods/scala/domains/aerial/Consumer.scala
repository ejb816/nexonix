
package domains.aerial

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Consumer extends Actor[draco.format.json.Json]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("ConsumeReport")

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Consumer")
    ConsumeReportRule.ruleType.pattern.accept(k)
    k
  }

  def actorType(): ActorType = new Actor[draco.format.json.Json] {
    override lazy val actorDefinition: TypeDefinition = Consumer.typeDefinition
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
    val consumed: java.util.ArrayList[String] = new java.util.ArrayList[String]()
    session.set("consumed", consumed)

    override def receive(ctx: TypedActorContext[draco.format.json.Json], msg: draco.format.json.Json): Behavior[draco.format.json.Json] = {
      session.insert(Seq(msg): _*)
      session.fire()
      Behaviors.same[draco.format.json.Json]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.json.Json], signal: Signal): Behavior[draco.format.json.Json] = {
      signal match {
        case org.apache.pekko.actor.typed.PostStop =>
          consumed.forEach((e: String) => domains.aerial.AerialSink.record(e))
          session.close()
          Behaviors.same[draco.format.json.Json]
        case _ => Behaviors.same[draco.format.json.Json]
      }
    }
  }
}
