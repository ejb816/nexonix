package draco.domain.stooge

import draco.domain.stooge.StoogeRulesTyped.processMessageTyped
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/*
  Code Flow
    Actor Initialization
      The Moe actor is initialized with an empty rules engine
      (stoogesRuleEngineMoe = None). It listens for incoming messages.

    Rules Engine Initialization
      When the InitializeRulesEngineMoe message is received:
        The rules engine is initialized using StoogeRulesEngineMoe().
        A log message indicates that the rules engine has been initialized.

    Processing Custom Messages When the MoeCustomMessage message is received:
        If the rules engine is initialized, the processMessageTyped method processes the action.
        If the rules engine is not initialized, an error message is logged.

    Handling Unknown Messages
      Any message that does not match the defined cases is logged as an unknown message.

  Observations
    Rules Engine Dependency
      The Moe actor depends on its rules engine to process actions. This dependency is
      managed using an optional variable (stoogesRuleEngineMoe), ensuring that the actor
      can handle messages even if the rules engine is not initialized.

    Graceful Error Handling
      The actor logs an error message if it receives a message before the rules engine is
      initialized. This ensures that the actor does not crash due to uninitialized dependencies.

    Modularity
      The Moe actor is modular and can be easily extended to handle additional messages or behaviors.

    Typed Actor API
      The use of Pekko's typed actor API ensures type safety and clarity in message handling.

  Key Components
    1. Message Types
    The Moe actor defines two specific message types:
      MoeCustomMessage: Encapsulates a StoogeAction that Moe can process.
      InitializeRulesEngineMoe: A message to initialize Moe's rules engine with predefined rules.
      These messages extend the StoogeAction trait, making them compatible with the actor system's message protocol.

    2. Behavior Definition
    The Moe actor's behavior is defined using Behaviors.setup and Behaviors.receiveMessage.
    It handles three types of messages:
      MoeCustomMessage: Processes the action using the rules engine if it has been initialized.
      InitializeRulesEngineMoe: Initializes Moe's rules engine with predefined rules from StoogeRulesEngineMoe.
      Default Case (_): Handles unknown messages gracefully by logging an error.

    3. Rules Engine
    The Moe actor uses an optional rules engine (stoogesRuleEngineMoe) to process actions. The rules engine is
    initialized when the InitializeRulesEngineMoe message is received. If a message is received before initialization,
    the actor logs an error indicating that the rules engine is not ready.

    4. Processing Messages
    The processMessageTyped method from StoogeRulesTyped is used to process actions based on
    the rules engine. This method determines the next stooge and action based on predefined rules.
 */

// Define the Moe actor
object Moe {

  // Define a message type for Moe
  case class MoeCustomMessage(content: StoogeAction) extends StoogeAction
  case class InitializeRulesEngineMoe(content: String) extends StoogeAction

  def apply(): Behavior[StoogeAction] = Behaviors.setup(context => {
    var stoogesRuleEngineMoe: Option[StoogeRulesTyped[String, StoogeAction, String, StoogeAction]] = None

    Behaviors.receiveMessage {
      case MoeCustomMessage(content) =>
        //println(s"Moe received a custom message: $content")
        stoogesRuleEngineMoe match {
          case Some(rulesEngine) =>
            processMessageTyped(content, "Moe", rulesEngine)
          case None =>
            println("Moe Rules engine not initialized. Unable to process message.")
        }
        Behaviors.same
      case InitializeRulesEngineMoe(content) =>
        println(s"$content initialized its Stooges Rule Engine")
        stoogesRuleEngineMoe = Some(StoogeRulesEngineMoe())
        Behaviors.same
      case _ =>
        println(s"Moe: Unknown receiveMessage")
        Behaviors.same
    }
  })
}