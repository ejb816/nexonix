package domains.terrestrial

import draco._
import io.circe.Json
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait Output extends Actor[domains.world.World]

object Output extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Output", _namePackage = Seq ("domains", "terrestrial")))
  lazy val dracoType: Type[Output] = Type[Output] (typeDefinition)

  def actorType(consumer: ActorRef[draco.format.json.JSON]): ActorType = new Actor[domains.world.World] {
    override lazy val typeDefinition: TypeDefinition = Output.typeDefinition

    override def receive(ctx: TypedActorContext[domains.world.World], msg: domains.world.World): Behavior[domains.world.World] = {
      msg match {
        case location: Location =>
          val payload = Json.obj(
            "type"            -> Json.fromString("LOCATION"),
            "latitude"        -> Json.fromDoubleOrNull(location.latitude),
            "longitude"       -> Json.fromDoubleOrNull(location.longitude),
            "elevationMetres" -> Json.fromInt(location.elevationMetres)
          )
          val report = new LocationReport {
            override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition
            override val json: Json = payload
          }
          consumer ! report
        case _ => // not a Location; nothing to encode
      }
      Behaviors.same[domains.world.World]
    }

    override def receiveSignal(ctx: TypedActorContext[domains.world.World], signal: Signal): Behavior[domains.world.World] = {
      Behaviors.same[domains.world.World]
    }
  }
}
