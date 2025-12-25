package draco.base.primes

import draco.ActorBehavior
import draco.primes.{Numbers, Primes}
import io.circe.syntax.EncoderOps
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait PrimesActor extends ActorBehavior[Primes]

// Create the actor system and spawn the actor
object PrimesActor extends App {
  // Define the actor's behavior
  val primesBehavior: Behavior[Primes] = Behaviors.receive { (context, message) =>
    message match {
      case numbers: Numbers  =>
        context.log.info(s"First 22 primes: ${numbers.primeSequence}")
        context.log.info(s"22nd prime: ${numbers.primeSequence.last}")
        context.log.info(s"Naturals for 22 primes: ${numbers.naturalSequence}")
        context.log.info(s"Composites between first 22 primes: ${numbers.compositeSequence}")
        Behaviors.stopped
    }
  }
  val actorSystem: ActorSystem[Primes] = ActorSystem(primesBehavior, "PrimesActorSystem")

  // Send a message to the actor
  actorSystem ! Numbers()
}