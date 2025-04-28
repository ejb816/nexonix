In Apache Pekko (formerly known as Akka), you can define and add behavior to a typed actor using the `Behaviors` API. The simplest way to add a behavior instance to a typed actor is by using the `Behaviors.setup` method to define the actor's behavior and then creating the actor with the `spawn` method in an actor system.

Here’s an example of how to create and add a behavior instance to a typed actor in Scala using Apache Pekko:

```scala
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

// Define a message type
sealed trait Command
case object SayHello extends Command

// Define the actor's behavior
val myBehavior: Behavior[Command] = Behaviors.receive { (context, message) =>
  message match {
    case SayHello =>
      context.log.info("Hello, Pekko!")
      Behaviors.same
  }
}

// Create the actor system and spawn the actor
object Main extends App {
  val actorSystem: ActorSystem[Command] = ActorSystem(myBehavior, "MyActorSystem")

  // Send a message to the actor
  actorSystem ! SayHello
}
```

### Explanation:
1. **Define the message type**: Create a sealed trait or case class to represent the messages the actor can handle.
2. **Define the behavior**: Use `Behaviors.receive` to define how the actor responds to incoming messages.
3. **Create the actor system**: Use `ActorSystem` to create the actor system and spawn the actor with the defined behavior.
4. **Send messages**: Use the `!` operator to send messages to the actor.

This is the simplest way to add a behavior instance to a typed actor in Apache Pekko. You can extend this approach by using `Behaviors.setup` if you need to initialize resources or access the actor's context during setup:

```scala
val myBehavior: Behavior[Command] = Behaviors.setup { context =>
  context.log.info("Actor is starting up...")
  Behaviors.receiveMessage {
    case SayHello =>
      context.log.info("Hello, Pekko!")
      Behaviors.same
  }
}
```

This approach is particularly useful when you need to perform initialization logic or access the actor's context during startup.<!---
[]-->