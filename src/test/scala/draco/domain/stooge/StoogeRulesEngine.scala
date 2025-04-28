package draco.domain.stooge

// Identify the 3 stooge
trait StoogeName
object StoogeName {
  val moe: String = "Moe"
  val larry: String = "Larry"
  val curly: String = "Curly"
}

/*
    Rule Engine:
    For a named stooge character and action taken on him, designate
    the next stooge character and the action to take on that character.
    This map uses 2 keys (the stooge and action taken on that stooge) to
    identify the next stooge and the action to take on him.
    String, Action, ActorRef, Action
 */
trait StoogeRules[K1, K2, V1, V2] {
  def map: Map[(K1, K2), (V1, V2)]
  def put(key1: K1, key2: K2, value1: V1, value2: V2): Unit
  def get(key1: K1, key2: K2): Option[(V1, V2)]
  def remove(key1: K1, key2: K2): Unit
  def allEntries: Map[(K1, K2), (V1, V2)]
}

object StoogeRules {
  //println("Initializing StoogeRulesEngine")
  def apply[K1, K2, V1, V2](): StoogeRules[K1, K2, V1, V2] = {
    new StoogeRules[K1, K2, V1, V2] {
      private var internalMap: Map[(K1, K2), (V1, V2)] = Map()

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
  }

  // Contains a mapping of the name of a stooge and the associated actor reference
  var actorRefMap: Map[String, ActorRef] = _
  def initializeActorRefMap(): Unit = {
    actorRefMap = Map[String, ActorRef]()
  }
  def addActor(name: String, actorRef: ActorRef): Unit = {
    actorRefMap = actorRefMap + (name -> actorRef)
  }

  /*
    Create the map that holds the rules to follow for the stooge actors.
    The left two values are keys for the right two values in the map and they
    represent the identified stooge (incoming - name and his action) and the action
    for that stooge to take next on another stooge (e.g. outgoing - actor ref and action).
  */
  var stoogesRules: StoogeRules[String, Action, ActorRef, Action] = _
  def initializeStoogesRules(): Unit = {
    // Create a new instance of StoogeRules
    stoogesRules = StoogeRules[String, Action, ActorRef, Action]()

    // Populate the rules
    stoogesRules.put(StoogeName.moe, Action.Start, actorRefMap(StoogeName.larry), Action.Bonk)
    stoogesRules.put(StoogeName.moe, Action.Bonk, actorRefMap(StoogeName.larry), Action.Bonk)
    stoogesRules.put(StoogeName.moe, Action.Poke, actorRefMap(StoogeName.larry), Action.Poke)
    stoogesRules.put(StoogeName.moe, Action.Slap, actorRefMap(StoogeName.larry), Action.Slap)
    stoogesRules.put(StoogeName.moe, Action.Stop, actorRefMap(StoogeName.larry), Action.Stop)

    stoogesRules.put(StoogeName.larry, Action.Start, actorRefMap(StoogeName.curly), Action.Bonk)
    stoogesRules.put(StoogeName.larry, Action.Bonk, actorRefMap(StoogeName.curly), Action.Bonk)
    stoogesRules.put(StoogeName.larry, Action.Poke, actorRefMap(StoogeName.curly), Action.Poke)
    stoogesRules.put(StoogeName.larry, Action.Slap, actorRefMap(StoogeName.curly), Action.Slap)
    stoogesRules.put(StoogeName.larry, Action.Stop, actorRefMap(StoogeName.curly), Action.Stop)

    stoogesRules.put(StoogeName.curly, Action.Start, actorRefMap(StoogeName.moe), Action.Bonk)
    stoogesRules.put(StoogeName.curly, Action.Bonk, actorRefMap(StoogeName.moe), Action.Poke)
    stoogesRules.put(StoogeName.curly, Action.Poke, actorRefMap(StoogeName.moe), Action.Slap)
    stoogesRules.put(StoogeName.curly, Action.Slap, actorRefMap(StoogeName.moe), Action.Stop)
    stoogesRules.put(StoogeName.curly, Action.Stop, actorRefMap(StoogeName.moe), Action.Stop)
  }

  def Greet(): String = "Initializing StoogeRulesEngine object"
}