package draco.gendrake

import draco._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Emitter extends DracoType

object Emitter extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emitter", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[Emitter] = Type[Emitter] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Emitter")
    Emit.ruleType.pattern.accept(k)
    draco.generator.SurfaceReceived.ruleType.pattern.accept(k)
    k
  }

  def actorType(received: java.util.List[draco.draketarget.Surface]): ActorType = new Actor[draco.TypeDefinition] {
    override lazy val typeDefinition: TypeDefinition = Emitter.typeDefinition

    val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession()
    session.set("received", received)

    override def receive(ctx: TypedActorContext[draco.TypeDefinition], msg: draco.TypeDefinition): Behavior[draco.TypeDefinition] = {
      session.insert(Seq(msg): _*)
      session.fire()
      Behaviors.same[draco.TypeDefinition]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.TypeDefinition], signal: Signal): Behavior[draco.TypeDefinition] = {
      signal match {
        case org.apache.pekko.actor.typed.PostStop =>
          session.close()
          Behaviors.same[draco.TypeDefinition]
        case _ => Behaviors.same[draco.TypeDefinition]
      }
    }
  }
}
