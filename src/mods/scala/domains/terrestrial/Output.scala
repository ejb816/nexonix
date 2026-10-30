package domains.terrestrial

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Output extends DracoType

object Output extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Output", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Output] = Type[Output] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Output")
    ConsumeReport.ruleType.pattern(k)
    OriginateReport.ruleType.pattern(k)
    k
  }

  def actorType(_consumer: => draco.format.dracon.Dracon => Unit): ActorType = new Actor[domains.world.World] {
    override lazy val typeDefinition: TypeDefinition = Output.typeDefinition
    lazy val consumer: draco.format.dracon.Dracon => Unit = _consumer

    override def receive(ctx: TypedActorContext[domains.world.World], msg: domains.world.World): Behavior[domains.world.World] = {
      msg match {
        case location: Location =>
          val payload: io.circe.Json = io.circe.Json.obj(("type", io.circe.Json.fromString("LOCATION")), ("latitude", io.circe.Json.fromDoubleOrNull(location.latitude)), ("longitude", io.circe.Json.fromDoubleOrNull(location.longitude)), ("elevationMetres", io.circe.Json.fromInt(location.elevationMetres)))
          val report: LocationReport = new LocationReport { override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition; override lazy val json: io.circe.Json = payload }
          consumer(report)
        case _ => ()
      }
      Behaviors.same[domains.world.World]
    }

    override def receiveSignal(ctx: TypedActorContext[domains.world.World], signal: Signal): Behavior[domains.world.World] = {
      Behaviors.same[domains.world.World]
    }
  }
}
