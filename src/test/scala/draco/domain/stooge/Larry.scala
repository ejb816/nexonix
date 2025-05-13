package draco.domain.stooge

import draco.domain.stooge.StoogeRulesTyped.processMessageTyped
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/*
  Code Flow
    Actor Initialization
      The Larry actor is initialized with an empty rules engine
      (stoogesRuleEngineLarry = None). It listens for incoming messages.

    Rules Engine Initialization
      When the InitializeRulesEngineLarry message is received:
        The rules engine is initialized using StoogeRulesEngineLarry().
        A log message indicates that the rules engine has been initialized.

    Processing Custom Messages When the LarryCustomMessage message is received:
        If the rules engine is initialized, the processMessageTyped method processes the action.
        If the rules engine is not initialized, an error message is logged.

    Handling Unknown Messages
      Any message that does not match the defined cases is logged as an unknown message.

  Observations
    Rules Engine Dependency
      The Larry actor depends on its rules engine to process actions. This dependency is
      managed using an optional variable (stoogesRuleEngineLarry), ensuring that the actor
      can handle messages even if the rules engine is not initialized.

    Graceful Error Handling
      The actor logs an error message if it receives a message before the rules engine is
      initialized. This ensures that the actor does not crash due to uninitialized dependencies.

    Modularity
      The Larry actor is modular and can be easily extended to handle additional messages or behaviors.

    Typed Actor API
      The use of Pekko's typed actor API ensures type safety and clarity in message handling.

  Key Components
    1. Message Types
    The Larry actor defines two specific message types:
      LarryCustomMessage: Encapsulates a StoogeAction that Larry can process.
      InitializeRulesEngineLarry: A message to initialize Larry's rules engine with predefined rules.
      These messages extend the StoogeAction trait, making them compatible with the actor system's message protocol.

    2. Behavior Definition
    The Larry actor's behavior is defined using Behaviors.setup and Behaviors.receiveMessage.
    It handles three types of messages:
      LarryCustomMessage: Processes the action using the rules engine if it has been initialized.
      InitializeRulesEngineLarry: Initializes Larry's rules engine with predefined rules from StoogeRulesEngineLarry.
      Default Case (_): Handles unknown messages gracefully by logging an error.

    3. Rules Engine
    The Larry actor uses an optional rules engine (stoogesRuleEngineLarry) to process actions. The rules engine is
    initialized when the InitializeRulesEngineLarry message is received. If a message is received before initialization,
    the actor logs an error indicating that the rules engine is not ready.

    4. Processing Messages
    The processMessageTyped method from StoogeRulesTyped is used to process actions based on
    the rules engine. This method determines the next stooge and action based on predefined rules.
 */

// Define the Larry actor
object Larry {

  // Define a message type for Larry
  case class LarryCustomMessage(content: StoogeAction) extends StoogeAction
  case class InitializeRulesEngineLarry(content: String) extends StoogeAction

  def apply(): Behavior[StoogeAction] = Behaviors.setup { context =>
    // Define mutable state for the rules engine
    var stoogesRuleEngineLarry: Option[StoogeRulesTyped[String, StoogeAction, String, StoogeAction]] = None

    Behaviors.receiveMessage {
      case LarryCustomMessage(content) =>
        //println(s"Larry received a custom message: $content")
        stoogesRuleEngineLarry match {
          case Some(rulesEngine) =>
            processMessageTyped(content, "Larry", rulesEngine)
          case None =>
            println("Larry Rules engine not initialized. Unable to process message.")
        }
        Behaviors.same
      case InitializeRulesEngineLarry(content) =>
        println(s"$content initialized its Stooges Rule Engine")
        stoogesRuleEngineLarry = Some(StoogeRulesEngineLarry())
        Behaviors.same
    }
  }
}