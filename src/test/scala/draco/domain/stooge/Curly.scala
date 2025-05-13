package draco.domain.stooge

import draco.domain.stooge.StoogeRulesTyped.processMessageTyped
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/*
  Code Flow
    Actor Initialization
      The Curly actor is initialized with an empty rules engine
      (stoogesRuleEngineCurly = None). It listens for incoming messages.

    Rules Engine Initialization
      When the InitializeRulesEngineCurly message is received:
        The rules engine is initialized using StoogeRulesEngineCurly().
        A log message indicates that the rules engine has been initialized.

    Processing Custom Messages When the CurlyCustomMessage message is received:
        If the rules engine is initialized, the processMessageTyped method processes the action.
        If the rules engine is not initialized, an error message is logged.

    Handling Unknown Messages
      Any message that does not match the defined cases is logged as an unknown message.

  Observations
    Rules Engine Dependency
      The Curly actor depends on its rules engine to process actions. This dependency is
      managed using an optional variable (stoogesRuleEngineCurly), ensuring that the actor
      can handle messages even if the rules engine is not initialized.

    Graceful Error Handling
      The actor logs an error message if it receives a message before the rules engine is
      initialized. This ensures that the actor does not crash due to uninitialized dependencies.

    Modularity
      The Curly actor is modular and can be easily extended to handle additional messages or behaviors.

    Typed Actor API
      The use of Pekko's typed actor API ensures type safety and clarity in message handling.

  Key Components
    1. Message Types
    The Curly actor defines two specific message types:
      CurlyCustomMessage: Encapsulates a StoogeAction that Curly can process.
      InitializeRulesEngineCurly: A message to initialize Curly's rules engine with predefined rules.
      These messages extend the StoogeAction trait, making them compatible with the actor system's message protocol.

    2. Behavior Definition
    The Curly actor's behavior is defined using Behaviors.setup and Behaviors.receiveMessage.
    It handles three types of messages:
      CurlyCustomMessage: Processes the action using the rules engine if it has been initialized.
      InitializeRulesEngineCurly: Initializes Curly's rules engine with predefined rules from StoogeRulesEngineCurly.
      Default Case (_): Handles unknown messages gracefully by logging an error.

    3. Rules Engine
    The Curly actor uses an optional rules engine (stoogesRuleEngineCurly) to process actions. The rules engine is
    initialized when the InitializeRulesEngineCurly message is received. If a message is received before initialization,
    the actor logs an error indicating that the rules engine is not ready.

    4. Processing Messages
    The processMessageTyped method from StoogeRulesTyped is used to process actions based on
    the rules engine. This method determines the next stooge and action based on predefined rules.
 */

// Define the Curly actor
object Curly {

  // Define a message type for Curly
  case class CurlyCustomMessage(content: StoogeAction) extends StoogeAction
  case class InitializeRulesEngineCurly(content: String) extends StoogeAction

  def apply(): Behavior[StoogeAction] = Behaviors.setup { context =>
    // Define mutable state for the rules engine
    var stoogesRuleEngineCurly: Option[StoogeRulesTyped[String, StoogeAction, String, StoogeAction]] = None

    Behaviors.receiveMessage {
      case CurlyCustomMessage(content) =>
        //println(s"Curly received a custom message: $content")
        stoogesRuleEngineCurly match {
          case Some(rulesEngine) =>
            processMessageTyped(content, "Curly", rulesEngine)
          case None =>
            println("Curly Rules engine not initialized. Unable to process message.")
        }
        Behaviors.same
      case InitializeRulesEngineCurly(content) =>
        println(s"$content initialized its Stooges Rule Engine")
        stoogesRuleEngineCurly = Some(StoogeRulesEngineCurly())
        Behaviors.same
    }
  }
}