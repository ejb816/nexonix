package domains.dataModel

import domains.bravo.Bravo
import draco._
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.StatefulSession

trait DataModelActor extends Extensible

object DataModelActor extends App {
  lazy val typeDefinition: TypeDefinition = DataModel.typeDefinition

  def actorWithSession(bravoRef: ActorRef[Bravo]): Actor[DataModel] = {
    val knowledge = Rule.knowledgeService.newKnowledge("DataModelAssembly")
    AssembleResultRule.ruleType.pattern.accept(knowledge)
    knowledge.set("bravoActorRef", bravoRef)
    val session: StatefulSession = knowledge.newStatefulSession()

    new Actor[DataModel] {
      override val actorDefinition: TypeDefinition = TypeDefinition(DataModel.typeDefinition.typeName)
      override val typeDefinition: TypeDefinition = DataModel.typeDefinition

      override def receive(ctx: TypedActorContext[DataModel], msg: DataModel): Behavior[DataModel] = {
        session.insert(Seq(msg): _*)
        session.fire()
        Behaviors.same[DataModel]
      }

      override def receiveSignal(ctx: TypedActorContext[DataModel], msg: Signal): Behavior[DataModel] = {
        msg match {
          case _: org.apache.pekko.actor.typed.PostStop => session.close()
          case _ =>
        }
        Behaviors.same[DataModel]
      }
    }
  }

  lazy val actorType: ActorType = Actor[DataModel](
    TypeDefinition(DataModel.typeDefinition.typeName)
  )
}
