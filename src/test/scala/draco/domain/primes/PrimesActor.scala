package draco.domain.primes

import draco.domain.rule.DomainRuleActor
import io.circe.syntax.EncoderOps
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait PrimesActor extends DomainRuleActor

// Create the actor system and spawn the actor
object PrimesActor extends App {
  // Define the actor's behavior
  val primesBehavior: Behavior[Primes] = Behaviors.receive { (context, message) =>
    message match {
      case fp: FindPrimes  =>
        context.log.info(fp.asJson.spaces2)
        Behaviors.stopped
    }
  }
  val actorSystem: ActorSystem[FindPrimes] = ActorSystem(primesBehavior, "PrimesActorSystem")

  // Send a message to the actor
  actorSystem ! FindPrimes(25, 25, 1)
}