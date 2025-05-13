package draco.domain.stooge

import draco.domain.actor.DomainActor
import draco.domain.stooge.Curly.{CurlyCustomMessage, InitializeRulesEngineCurly}
import draco.domain.stooge.Larry.{InitializeRulesEngineLarry, LarryCustomMessage}
import draco.domain.stooge.Moe.{InitializeRulesEngineMoe, MoeCustomMessage}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}

/*
  Code Flow
    Root Actor Initialization
      The Stooge actor is initialized with an empty children map. It listens
      for commands and performs actions based on the received messages.

    Creating Child Actors
      When the CreateChild command is received, the root actor creates a child actor
      (Moe, Larry, or Curly) using context.spawn and adds it to the children map.

    Sending Messages to Child Actors
      The SendMessageToChild command retrieves the child actor from the children
      map and sends a custom message (MoeCustomMessage, LarryCustomMessage, or
      CurlyCustomMessage) based on the child's name.

    Initializing Child Rules Engine
      The InitializeChildRulesEngine command retrieves the child actor and sends
      an initialization message (InitializeRulesEngineMoe, InitializeRulesEngineLarry,
      or InitializeRulesEngineCurly) to set up its rules engine.

    Stopping the Root Actor
      The StoogeAction.Stop command stops the root actor using Behaviors.stopped.

  Observations
    Actor Hierarchy
      The root actor (Stooge) manages child actors (Moe, Larry, Curly)
      and acts as a central coordinator.

    Dynamic Behavior
      The system dynamically creates child actors and sends messages to
      them based on commands. This allows for flexible interactions.

    Rule Initialization
      The InitializeChildRulesEngine command sets up the rules engine for each
      stooge, enabling them to process actions according to predefined rules.

    Logging
      The code uses println for logging, which is sufficient for debugging but
      could be replaced with a proper logging framework for production use.
 */

/*
  This trait defines the message protocol for the actors. It includes predefined actions
  (SayHello, Start, Bonk, Poke, Slap, Stop, Unknown) and custom commands:
    CreateChild: Command to create a child actor.
    InitializeChildRulesEngine: Command to initialize a child actor's rules engine.
    SendMessageToChild: Command to send a message to a child actor.
 */
trait StoogeAction
object StoogeAction {
  case object SayHello extends StoogeAction
  case object Start extends StoogeAction
  case object Bonk extends StoogeAction
  case object Poke extends StoogeAction
  case object Slap extends StoogeAction
  case object Stop extends StoogeAction
  case object Unknown extends StoogeAction
}

/*
  This object provides a reusable behavior for handling commands. It takes a handler function as
  input and executes it whenever a message is received. This is useful for modularizing behavior logic.
 */
object CommandHandlerBehavior {
  def handleCommands[T](handler: T => Unit): Behavior[T] = Behaviors.receive { (context, message) =>
    handler(message) // Execute the provided handler function
    Behaviors.same
  }
}

case class CreateChild(name: String) extends StoogeAction
case class InitializeChildRulesEngine(name: String, message: String) extends StoogeAction
case class SendMessageToChild(name: String, message: StoogeAction) extends StoogeAction

/*
  The Stooge trait represents a base actor with a receive method that handles incoming messages.
  It includes a name property derived from the class name and logs received messages.
 */
trait Stooge extends DomainActor {
  lazy val name: String = this.getClass.getSimpleName

  def receive: Receive = {
    case msg =>
      val msgFrom = sender()
      println(s"${name}: Received message: $msg from $msgFrom");
  }
}

/*
  The companion object defines the behavior of the root actor. It
  manages child actors and handles the following commands:
    CreateChild: Creates a child actor (Moe, Larry, or Curly) and stores it in a children map.
    SendMessageToChild: Sends a message to a specific child actor.
    InitializeChildRulesEngine: Sends a message to initialize a child actor's rules engine.
    StoogeAction.Stop: Stops the root actor.
 */
object Stooge {
  def apply(): Behavior[StoogeAction] = Behaviors.setup { context =>
    var children: Map[String, ActorRef[StoogeAction]] = Map.empty
    var child: ActorRef[StoogeAction] = null
    Behaviors.receiveMessage {
      case CreateChild(name) =>
        // Create a new child actor
        name match {
          case "Moe" =>
            println(s"CreateChild actor with name: $name")
            child = context.spawn(Moe(), name)
          case "Larry" =>
            println(s"CreateChild actor with name: $name")
            child = context.spawn(Larry(), name)
          case "Curly" =>
            println(s"CreateChild actor with name: $name")
            child = context.spawn(Curly(), name)
          case _ =>
            println("Stooge/CreateChild: Unknown child actor name")
        }
        children += (name -> child)
        //context.log.info(s"Created child actor with name: $name")
        Behaviors.same

      /*
        The child actors (Moe, Larry, Curly) are expected to handle custom messages:
          MoeCustomMessage
          LarryCustomMessage
          CurlyCustomMessage
          These messages encapsulate the StoogeAction and allow child-specific processing.
      */
      case SendMessageToChild(name, message) =>
        // Send a message to the specified child actor
        children.get(name) match {
          case Some(child) =>
            //child ! ChildMessage(message)
            name match {
              case "Moe" =>
                //println(s"SendingMessageToChild actor $name: $message")
                child ! MoeCustomMessage(message)
              case "Larry" =>
                //println(s"SendingMessageToChild actor $name: $message")
                child ! LarryCustomMessage(message)
              case "Curly" =>
                //println(s"SendingMessageToChild actor $name: $message")
                child ! CurlyCustomMessage(message)
              case _ =>
                println(s"Stooge/SendMessageToChild: Unknown child actor $name")
            }
            //context.log.info(s"Sent message to child actor $name: $message")

          case None =>
            //context.log.warn(s"No child actor found with name: $name")
            println(s"No child actor found with name: $name")
        }
        Behaviors.same

      /*
        The InitializeChildRulesEngine command sends initialization messages
        (InitializeRulesEngineMoe, InitializeRulesEngineLarry, InitializeRulesEngineCurly)
        to the respective child actors. These messages set up the rules engine for each stooge.
       */
      case InitializeChildRulesEngine(name, message) =>
        // Send a message to the specified child actor to initialize its rules engine
        children.get(name) match {
          case Some(child) =>
             name match {
              case "Moe" =>
                println(s"Initializing $name Rule Engine")
                child ! InitializeRulesEngineMoe(message)
              case "Larry" =>
                println(s"Initializing $name Rule Engine")
                child ! InitializeRulesEngineLarry(message)
              case "Curly" =>
                println(s"Initializing $name Rule Engine")
                child ! InitializeRulesEngineCurly(message)
              case _ =>
                println(s"Stooge/InitializeChildRulesEngine: Unknown child actor $name")
            }
          //context.log.info(s"Sent message to child actor $name: $message")

          case None =>
            //context.log.warn(s"No child actor found with name: $name")
            println(s"No child actor found with name: $name")
        }
        Behaviors.same

      case StoogeAction.Stop =>
        //context.log.info("Stopping root actor")
        println("Stopping root actor")
        Behaviors.stopped
    }
  }
}