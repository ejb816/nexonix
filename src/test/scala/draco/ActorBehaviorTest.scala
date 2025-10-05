package draco

import org.apache.pekko.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

class ActorBehaviorTest extends AnyFunSuite {
  test("ActorBehavior") {
    val systemBehavior = ActorBehavior[Int]()
    val system = ActorSystem[Int](systemBehavior, "actorInt")
    val actorInt = system.systemActorOf(systemBehavior,"actorInt")
    println ("Sending number 10 to actorInt:")
    actorInt ! 10
  }
}
