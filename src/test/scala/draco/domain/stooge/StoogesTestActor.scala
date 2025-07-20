package draco.domain.stooge

import draco.domain.DomainActor
import org.apache.pekko.actor.ActorRef

// Identify the actions a stooge can do to another stooge. Also, include a start and stop action.
trait Action
object Action {
  case object Start extends Action
  case object Bonk extends Action
  case object Poke extends Action
  case object Slap extends Action
  case object Stop extends Action
  case object Unknown extends Action  // use to test error case
}

// Define a case class for messages with additional parameters
//class CustomMessage(message: Action, name: String, mapping: TwoKeyTwoValueMap[String, Action, ActorRef, Action])

/*
  From MChat AI

  StoogesActor Trait:
    Made StoogesActor a trait that extends Actor. This is a common pattern in Akka for defining reusable actor behavior.
    Defined name and mapping as abstract members so that subclasses (like Moe) must provide implementations.
    Provided a default implementation of receive in the StoogesActor trait.

  StoogesActor Companion Object:
    The apply method creates an anonymous instance of StoogesActor with the provided name and mapping.
    processMessage Method:
      Added a reusable processMessage method in the StoogesActor object to handle common message processing logic.
      This method can be reused by subclasses.

  Moe object:
    Moe extends StoogesActor and overrides the receive method to add Moe-specific logic.
    The overridden receive method still calls processMessage to reuse the shared logic.

  =======

  case message =>:
    This is a pattern match that captures the incoming message.
    The message variable is implicitly defined by Akka when the actor receives a message.
    The type of message is Any, as actors can receive messages of any type.

  Incoming Messages:
    The message comes from the sender (another actor or external code) using the ! (tell) operator or
    other Pekka messaging mechanisms. For example, when a message is sent to the actor
    (e.g., moeActor ! "Hello, Moe!"), Pekka passes the message to the receive method.

  Processing the Message:
    The case message => pattern matches the incoming message and binds it to the variable 'message'.
    The 'message' variable is then passed to the processMessage method for further processing.
 */
trait StoogesActor extends DomainActor[StoogeAction] {
  val name: String
  val mapping: StoogeRules[String, Action, ActorRef, Action]
}

object StoogesActor {
  def apply (_name: String, _mapping:StoogeRules[String, Action, ActorRef, Action]) : StoogesActor = {
    new StoogesActor {
      override val name: String = _name
      override val mapping: StoogeRules[String, Action, ActorRef, Action] = _mapping
    }
  }

  def takeAction(name: String, action: Action, mapping: StoogeRules[String, Action, ActorRef, Action]): Unit = {
    //println(s"name key = $name  action key = $action")
    val nextAction = mapping.get(name, action)
    nextAction match {
      case Some((actorRef, actionValue)) =>
        //println(s"$actorRef // $actionValue")
        actorRef ! (actionValue, name, mapping) // ! is "tell", send the action to the actor
      case None =>
        println("$name - No actorRef and associated Action found")
    }
  }

  def processMessage(message: Any, name: String, mapping: StoogeRules[String, Action, ActorRef, Action]): Unit = {
    message match {
      case Action.Start =>
        println(s"$name started")
        takeAction(name, Action.Start, mapping)
      case Action.Bonk =>
        println(s"$name bonked")
        takeAction(name, Action.Bonk, mapping)
      case Action.Poke =>
        println(s"$name poked")
        takeAction(name, Action.Poke, mapping)
      case Action.Slap =>
        println(s"$name slapped")
        takeAction(name, Action.Slap, mapping)
      case Action.Stop =>
        name match {
          case StoogeName.curly =>
            println(s"$name Stopped") // stop the sequence of actions from continuing
          case other =>
            println(s"$name Stopped")
            takeAction(name, Action.Stop, mapping)
        }
      case _ =>
        println(s"$name is confused - received an unknown message.")
    }
  }
}