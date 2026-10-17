package domains.aerial

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge
import scala.collection.mutable

trait Consumer extends DracoType

object Consumer extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Consumer", _namePackage = Seq ("domains", "aerial")))
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

    lazy val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
    lazy val consumed: mutable.Buffer[String] = mutable.Buffer.empty[String]
    session.set("consumed", consumed)

    override def receive(ctx: TypedActorContext[draco.format.dracon.Dracon], msg: draco.format.dracon.Dracon): Behavior[draco.format.dracon.Dracon] = {
      session.insert(Seq(msg): _*)
      session.fire()
      Behaviors.same[draco.format.dracon.Dracon]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.dracon.Dracon], signal: Signal): Behavior[draco.format.dracon.Dracon] = {
      signal match {
        case org.apache.pekko.actor.typed.PostStop =>
          consumed.foreach((e: String) => domains.aerial.AerialSink.record(e))
          session.close()
          Behaviors.same[draco.format.dracon.Dracon]
        case _ => Behaviors.same[draco.format.dracon.Dracon]
      }
    }
  }
}
