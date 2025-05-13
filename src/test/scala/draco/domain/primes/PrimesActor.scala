package draco.domain.primes

import io.circe.syntax.EncoderOps
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

// Create the actor system and spawn the actor
object PrimesActor extends App {
  // Define the actor's behavior
  val primesBehavior: Behavior[FindPrimes] = Behaviors.receive { (context, message) =>
    message match {
      case fp  =>
        context.log.info(fp.asJson.spaces2)
        Behaviors.same
    }
  }
  val actorSystem: ActorSystem[FindPrimes] = ActorSystem(primesBehavior, "PrimesActorSystem")

  // Send a message to the actor
  actorSystem ! FindPrimes(25, 1)
}