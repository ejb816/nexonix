
package domains.aerial

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Consumer extends Actor[draco.format.json.JSON]

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Consumer] = Type[Consumer] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Consumer")
    ConsumeReportRule.ruleType.pattern.accept(k)
    OriginateReportRule.ruleType.pattern.accept(k)
    k
  }

  def actorType(): ActorType = new Actor[draco.format.json.JSON] {
    override lazy val typeDefinition: TypeDefinition = Consumer.typeDefinition

    val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
    val consumed: java.util.ArrayList[String] = new java.util.ArrayList[String]()
    session.set("consumed", consumed)

    override def receive(ctx: TypedActorContext[draco.format.json.JSON], msg: draco.format.json.JSON): Behavior[draco.format.json.JSON] = {
      session.insert(Seq(msg): _*)
      session.fire()
      Behaviors.same[draco.format.json.JSON]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.json.JSON], signal: Signal): Behavior[draco.format.json.JSON] = {
      signal match {
        case org.apache.pekko.actor.typed.PostStop =>
          consumed.forEach((e: String) => domains.aerial.AerialSink.record(e))
          session.close()
          Behaviors.same[draco.format.json.JSON]
        case _ => Behaviors.same[draco.format.json.JSON]
      }
    }
  }
}
