package draco.domain.stooge

import draco.domain.stooge.StoogeRules._
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Props}
import org.scalatest.funsuite.AnyFunSuite

class TestStoogesActor extends AnyFunSuite {

    test("Test Stooge Actor creation") {

        // Accessing this method in the StoogeRulesEngine triggers its initialization
        println(Greet())

        // Instantiate the actor reference map in the StoogeRulesEngine
        initializeActorRefMap()

        // Create an ActorSystem
        val systemBehavior = Stooge()
        val system: ActorSystem[StoogeAction] = ActorSystem[StoogeAction](systemBehavior,"StoogesActorSystem")
        val stoogesActor = system.systemActorOf(systemBehavior, "StoogesActorSystem")
        // Initialize the stooge rules engine
        initializeStoogesRules()

        /*
         Send messages to the actor.
         Messages are sent to the actor using the ! operator (tell).
         The actor processes the message based on its "receive" method.
         */
        stoogesActor ! StoogeAction.Start  // This kicks off the processing

        // Give the threads in the actor system time to run
        Thread.sleep(5000)

        // Shutdown the ActorSystem
        system.terminate()

    }
}