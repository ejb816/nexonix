package draco.domain.primes

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