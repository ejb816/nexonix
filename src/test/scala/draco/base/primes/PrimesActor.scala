package draco.base.primes

import draco.ActorBehavior
import io.circe.syntax.EncoderOps
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait PrimesActor extends ActorBehavior[Primes]

// Create the actor system and spawn the actor
object PrimesActor extends App {
  // Define the actor's behavior
  val primesBehavior: Behavior[Primes] = Behaviors.receive { (context, message) =>
    message match {
      case ps: Primes  =>
        context.log.info(ps.asJson.spaces2)
        Behaviors.stopped
    }
  }
  val actorSystem: ActorSystem[Primes] = ActorSystem(primesBehavior, "PrimesActorSystem")

  // Send a message to the actor
  actorSystem ! Primes(25)
}