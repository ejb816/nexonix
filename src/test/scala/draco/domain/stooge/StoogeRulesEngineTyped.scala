package draco.domain.stooge

/*
  Code Flow

  Rule Initialization
  Each stooge's rules are initialized using the apply method in their respective objects
  (StoogeRulesEngineMoe, StoogeRulesEngineLarry, StoogeRulesEngineCurly). The rules are
  stored as a map where:
    Keys are tuples (String, StoogeAction) representing the stooge and the action.
    Values are tuples (String, StoogeAction) representing the next stooge and action.

  Processing Messages
  The processMessageTyped method handles incoming messages (actions) for a stooge. It:
    Matches the message to a StoogeAction.
    Prints the current action.
    Calls takeAction to determine the next stooge and action based on the rules.

  Taking Actions
    The takeAction method retrieves the next stooge and action from the rules using the
    get method of StoogeRulesTyped. If a rule exists, it prints the next action; otherwise,
    it indicates no rule was found.
 */

/*
  StoogeRulesTyped Trait
  This trait defines the contract for a typed rules engine. It provides methods for managing a mapping of rules:
    map: Retrieves the current rules.
    put: Adds or updates a rule.
    get: Retrieves a rule based on a key pair.
    remove: Removes a rule.
    allEntries: Retrieves all rules.
 */
trait StoogeRulesTyped[K1, K2, V1, V2] {
  def map: Map[(K1, K2), (V1, V2)]
  def put(key1: K1, key2: K2, value1: V1, value2: V2): Unit
  def get(key1: K1, key2: K2): Option[(V1, V2)]
  def remove(key1: K1, key2: K2): Unit
  def allEntries: Map[(K1, K2), (V1, V2)]
}

/*
  This is a concrete implementation of the StoogeRulesTyped trait. It uses a
  mutable map (internalMap) to store the rules and implements the trait's methods.
 */
class StoogeRulesTypedImpl[K1, K2, V1, V2](initialRules: Map[(K1, K2), (V1, V2)]
                                          ) extends StoogeRulesTyped[K1, K2, V1, V2] {

  private var internalMap: Map[(K1, K2), (V1, V2)] = initialRules

  override def map: Map[(K1, K2), (V1, V2)] = internalMap

  override def put(key1: K1, key2: K2, value1: V1, value2: V2): Unit = {
    internalMap += ((key1, key2) -> (value1, value2))
  }

  override def get(key1: K1, key2: K2): Option[(V1, V2)] = {
    internalMap.get((key1, key2))
  }

  override def remove(key1: K1, key2: K2): Unit = {
    internalMap -= ((key1, key2))
  }

  override def allEntries: Map[(K1, K2), (V1, V2)] = internalMap
}

/*
  The companion object provides utility methods:
    apply: A factory method to create instances of StoogeRulesTypedImpl.
    processMessageTyped: A method to process messages (actions) and determine
                         the next stooge and action based on the rules.
 */
object StoogeRulesTyped {
  def apply[K1, K2, V1, V2](initialRules: Map[(K1, K2), (V1, V2)]
                           ): StoogeRulesTyped[K1, K2, V1, V2] = {
    new StoogeRulesTypedImpl[K1, K2, V1, V2](initialRules)
  }
  def processMessageTyped(message: Any,
                          name: String,
                          mapping: StoogeRulesTyped[String, StoogeAction, String, StoogeAction]): Unit = {
    message match {
      case StoogeAction.Start =>
        println(s"$name started")
        takeAction(name, StoogeAction.Start, mapping)
      case StoogeAction.Bonk =>
        println(s"$name bonked")
        takeAction(name, StoogeAction.Bonk, mapping)
      case StoogeAction.Poke =>
        println(s"$name poked")
        takeAction(name, StoogeAction.Poke, mapping)
      case StoogeAction.Slap =>
        println(s"$name slapped")
        takeAction(name, StoogeAction.Slap, mapping)
      case StoogeAction.Stop =>
        name match {
          case "Curly" =>
            println(s"$name Stopped") // stop the sequence of actions from continuing
          case other =>
            println(s"$name Stopped")
            takeAction(name, StoogeAction.Stop, mapping)
        }
      case _ =>
        println(s"$name is confused - received an unknown message.")
    }

    def takeAction(name: String,
                   action: StoogeAction,
                   mapping: StoogeRulesTyped[String, StoogeAction, String, StoogeAction]): Unit = {
      val nextAction = mapping.get(name, action)
      nextAction match {
        case Some((stooge, actionValue)) =>
          println(s"Next: $actionValue --> $stooge")
        case None =>
          println("$name - No stooge and associated Action found")
      }
    }
  }
}

/*
  StoogeRulesEngineMoe, StoogeRulesEngineLarry, and StoogeRulesEngineCurly Objects
  These objects define the specific rules for each stooge:
    Moe: Interacts with Larry for all actions.
    Larry: Interacts with Curly for all actions.
    Curly: Interacts with Moe, with a sequence of actions leading to Stop.
 */
object StoogeRulesEngineMoe {
  def apply(): StoogeRulesTyped[String, StoogeAction, String, StoogeAction] = {
    // Define the initial rules directly in the apply method
    val initialRules: Map[(String, StoogeAction), (String, StoogeAction)] = Map(
      ("Moe", StoogeAction.Start) -> (("Larry"), StoogeAction.Bonk),
      ("Moe", StoogeAction.Bonk)  -> (("Larry"), StoogeAction.Bonk),
      ("Moe", StoogeAction.Poke)  -> (("Larry"), StoogeAction.Poke),
      ("Moe", StoogeAction.Slap)  -> (("Larry"), StoogeAction.Slap),
      ("Moe", StoogeAction.Stop)  -> (("Larry"), StoogeAction.Stop)
    )

    // Return the StoogeRulesTyped instance
    StoogeRulesTyped(initialRules)
  }
}

object StoogeRulesEngineLarry {
  def apply(): StoogeRulesTyped[String, StoogeAction, String, StoogeAction] = {
    // Define the initial rules directly in the apply method
    val initialRules: Map[(String, StoogeAction), (String, StoogeAction)] = Map(
      ("Larry", StoogeAction.Start) -> (("Curly"), StoogeAction.Bonk),
      ("Larry", StoogeAction.Bonk)  -> (("Curly"), StoogeAction.Bonk),
      ("Larry", StoogeAction.Poke)  -> (("Curly"), StoogeAction.Poke),
      ("Larry", StoogeAction.Slap)  -> (("Curly"), StoogeAction.Slap),
      ("Larry", StoogeAction.Stop)  -> (("Curly"), StoogeAction.Stop)
    )

    // Return the StoogeRulesTyped instance
    StoogeRulesTyped(initialRules)
  }
}

object StoogeRulesEngineCurly {
  def apply(): StoogeRulesTyped[String, StoogeAction, String, StoogeAction] = {
    // Define the initial rules directly in the apply method
    val initialRules: Map[(String, StoogeAction), (String, StoogeAction)] = Map(
      ("Curly", StoogeAction.Start) -> (("Moe"), StoogeAction.Bonk),
      ("Curly", StoogeAction.Bonk)  -> (("Moe"), StoogeAction.Poke),
      ("Curly", StoogeAction.Poke)  -> (("Moe"), StoogeAction.Slap),
      ("Curly", StoogeAction.Slap)  -> (("Moe"), StoogeAction.Stop),
      ("Curly", StoogeAction.Stop)  -> (("Moe"), StoogeAction.Stop)
    )

    // Return the StoogeRulesTyped instance
    StoogeRulesTyped(initialRules)
  }
}