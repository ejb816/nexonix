package draco.domain.stooge

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.scalatest.funsuite.AnyFunSuite

/*
  This code defines a suite of tests for the Stooge domain actors using ScalaTest and Apache Pekko's
  typed actor API. Each test verifies specific functionality of the actors, including their ability to
  process messages, initialize rules engines, and interact within a hierarchical actor system.

  Key Components
    1. Test Framework
    The tests are written using ScalaTest's AnyFunSuite, which provides a simple way to define
    and run unit tests. Each test is encapsulated in a test block with a descriptive name.

    2. Actor System
    Each test creates an ActorSystem for the actor being tested. The ActorSystem is the entry
    point for interacting with actors in Apache Pekko.

    3. Message Sending
    Messages are sent to the actors using the ! operator, which is the standard way to send messages in Pekko.

    4. Thread Sleep
    The tests use Thread.sleep to allow asynchronous operations to complete before terminating the ActorSystem.
    This is necessary because actors process messages asynchronously.

    5. Actor Termination
    Each test terminates the ActorSystem after execution to avoid resource leaks.

  Observations
    Modular Testing Each test focuses on a specific aspect of the Stooge domain,
    making it easier to isolate and debug issues.

    Actor Initialization The tests ensure that actors are properly initialized before processing messages.
    This is critical for actors like Moe, Larry, and Curly, which depend on their rules engines.

    Actor Hierarchy The Test Stooge Actor Hierarchy test verifies the interaction between the root actor
    and its child actors, ensuring that the hierarchical structure works as expected.

    Graceful Termination Each test terminates the ActorSystem after execution to prevent resource leaks.
*/

class TestDomainActors extends AnyFunSuite {

  /*
    This test verifies the basic functionality of a simple actor that responds
    to a SayHello message. It demonstrates how to define and interact with a basic actor.
 */
  test("Test Actor Type") {

    // Define a message type
    sealed trait Command
    case object SayHello extends Command

    // Define the actor's behavior
    val myBehavior: Behavior[Command] = Behaviors.receive { (context, message) =>
      message match {
        case SayHello =>
          //context.log.info("Hello, Pekko!")
          println("Hello, Pekko!")
          Behaviors.same
      }
    }

    println("Test Stooge Domain")

    val actorSystem: ActorSystem[Command] = ActorSystem(myBehavior, "MyActorSystem")

    // Send a message to the actor
    actorSystem ! SayHello
    Thread.sleep(1000)
    // Always terminate the ActorSystem after the test to avoid resource leaks.
    actorSystem.terminate()

    println("Test Stooge Domain complete")
    println(" ")
  }

  /*
    This test verifies the behavior of the Moe actor:
      Initializes Moe's rules engine using InitializeRulesEngineMoe.
      Sends a custom message (MoeCustomMessage) to Moe.
      Ensures Moe processes the message correctly if the rules engine is initialized.
   */
  test("Test Stooge Moe Domain") {

    println("Test Stooge Moe Domain")

    // Create the ActorSystem for Moe
    val moeSystem: ActorSystem[StoogeAction] = ActorSystem(Moe(), "MoeActorSystem")

    // Send a message to Moe
    moeSystem ! Moe.InitializeRulesEngineMoe("Moe")
    moeSystem ! Moe.MoeCustomMessage(StoogeAction.Start)

    // Shut down the ActorSystem after a brief delay
    Thread.sleep(1000)
    moeSystem.terminate()

    println("Test Moe Stooge Domain complete")
    println(" ")

  }

  /*
    This test verifies the behavior of the Larry actor:
      Initializes Larry's rules engine using InitializeRulesEngineLarry.
      Sends a custom message (LarryCustomMessage) to Larry.
      Ensures Larry processes the message correctly if the rules engine is initialized.
   */
  test("Test Stooge Larry Domain") {

    println("Test Stooge Larry Domain")

    // In Apache Pekko Typed, actors are typically defined as objects
    // or classes with an apply method that returns a Behavior.
    val larrySystem: ActorSystem[StoogeAction] = ActorSystem(Larry(), "LarryActorSystem")

    // Send a message to the actor
    larrySystem ! Larry.InitializeRulesEngineLarry("Larry")
    larrySystem ! Larry.LarryCustomMessage(StoogeAction.Start)
    Thread.sleep(1000)
    // Always terminate the ActorSystem after the test to avoid resource leaks.
    larrySystem.terminate()

    println("Test Larry Stooge Domain complete")
    println(" ")

  }

  /*
    This test verifies the behavior of the Curly actor:
      Initializes Curly's rules engine using InitializeRulesEngineCurly.
      Sends a custom message (CurlyCustomMessage) to Curly.
      Ensures Curly processes the message correctly if the rules engine is initialized.
   */
  test("Test Stooge Curly Domain") {

    println("Test Stooge Curly Domain")

    // In Apache Pekko Typed, actors are typically defined as objects
    // or classes with an apply method that returns a Behavior.
    val curlySystem: ActorSystem[StoogeAction] = ActorSystem(Curly(), "CurlyActorSystem")

    // Send a message to the actor
    curlySystem ! Curly.InitializeRulesEngineCurly("Curly")
    curlySystem ! Curly.CurlyCustomMessage(StoogeAction.Start)
    Thread.sleep(1000)
    // Always terminate the ActorSystem after the test to avoid resource leaks.
    curlySystem.terminate()

    println("Test Curly Stooge Domain complete")
    println(" ")
  }

  /*
    This test verifies the interaction between the root Stooge actor and its child actors:
      Creates child actors (Moe, Larry, Curly) using CreateChild.
      Initializes the rules engines for each child actor using InitializeChildRulesEngine.
      Sends messages to each child actor using SendMessageToChild.
      Tests the sequence of actions (Start, Bonk, Poke, Slap, Stop) for each stooge.
      Stops the root actor and terminates the ActorSystem.
  */
  test("Test Stooge Actor Hierarchy") {
    println("Test Stooge Actor Hierarchy")

    // In Apache Pekko Typed, actors are typically defined as objects
    // or classes with an apply method that returns a Behavior.
    //
    // Create the ActorSystem with the RootActor as the root
    val actorSystem: ActorSystem[StoogeAction] = ActorSystem(Stooge(), "RootStoogeActorSystem")

    // Interact with the root actor
    //actorSystem ! receive("hello receive!")
    actorSystem ! CreateChild("Moe")
    actorSystem ! CreateChild("Larry")
    actorSystem ! CreateChild("Curly")
    Thread.sleep(1000)
    actorSystem ! InitializeChildRulesEngine("Moe", "Moe")
    Thread.sleep(1000)
    actorSystem ! InitializeChildRulesEngine("Larry", "Larry")
    Thread.sleep(1000)
    actorSystem ! InitializeChildRulesEngine("Curly", "Curly")
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Moe", StoogeAction.Start)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Larry", StoogeAction.Bonk)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Curly", StoogeAction.Bonk)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Moe",StoogeAction.Poke)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Larry",StoogeAction.Poke)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Curly",StoogeAction.Poke)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Moe",StoogeAction.Slap)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Larry",StoogeAction.Slap)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Curly",StoogeAction.Slap)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Moe",StoogeAction.Stop)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Larry",StoogeAction.Stop)
    Thread.sleep(1000)
    actorSystem ! SendMessageToChild("Curly",StoogeAction.Stop)
    Thread.sleep(1000)

    // Shut down the ActorSystem after a brief delay
    actorSystem ! StoogeAction.Stop
    actorSystem.terminate()

    println("Test Stooge Actor Hierarchy complete")
    println(" ")
  }
}