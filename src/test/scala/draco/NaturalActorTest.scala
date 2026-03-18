package draco


import domains.natural._
import domains.natural.actor._
import org.apache.pekko.actor.typed._
import org.scalatest.funsuite.AnyFunSuite

class NaturalActorTest extends AnyFunSuite {
  test("Actor[Natural]") {
    val system = ActorSystem[Natural](NaturalActor.actorInstance.asInstanceOf[Actor[Natural]], "naturalActor")
    println("Sending number 10 to naturalActor:")
    system ! Natural(10)
    // give the async actor time to process before the test exits
    Thread.sleep(100)
    system.terminate()
  }
}
