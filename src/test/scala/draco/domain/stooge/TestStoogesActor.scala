package draco.domain.stooge

import draco.domain.stooge.StoogeRules._
import org.scalatest.funsuite.AnyFunSuite

class TestStoogesActor extends AnyFunSuite {

    test("Test StoogeActor Actor creation") {

        // Accessing this method in the StoogeRulesEngine triggers its initialization
        println(Greet())

        // Instantiate the actor reference map in the StoogeRulesEngine
        initializeActorRefMap()

        // Create an ActorSystem
        val system = {
            ActorSystem("ActorSystem")
        }

        // Create the 3 stooge actors and add the ActorRef values to the rules engine actor reference map
        val moeActor: ActorRef = system.actorOf(Props(actor_moe), "moeActor")
        addActor(StoogeName.moe, moeActor)
        val larryActor: ActorRef = system.actorOf(Props(actor_larry), "larryActor")
        addActor(StoogeName.larry, larryActor)
        val curlyActor: ActorRef = system.actorOf(Props(actor_curly), "curlyActor")
        addActor(StoogeName.curly, curlyActor)

        // Initialize the stooge rules engine
        initializeStoogesRules()

        /*
         Send messages to the actor.
         Messages are sent to the actor using the ! operator (tell).
         The actor processes the message based on its "receive" method.
         */
        moeActor ! (Action.Start, StoogeName.moe, stoogesRules)  // This kicks off the processing

        // Give the threads in the actor system time to run
        Thread.sleep(5000)

        // Shutdown the ActorSystem
        system.terminate()

    }
}