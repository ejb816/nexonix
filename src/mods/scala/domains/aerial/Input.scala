package domains.aerial

import draco._
import domains._
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Input extends DracoType

object Input extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Input", _namePackage = Seq ("domains", "aerial")))
  lazy val dracoType: Type[Input] = Type[Input] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Input")
    ConsumeReport.ruleType.pattern(k)
    OriginateReport.ruleType.pattern(k)
    k
  }

  def actorType(_worldConsumer: => domains.world.World => Unit): ActorType = new Actor[draco.format.dracon.Dracon] {
    override lazy val typeDefinition: TypeDefinition = Input.typeDefinition
    lazy val worldConsumer: domains.world.World => Unit = _worldConsumer

    override def receive(ctx: TypedActorContext[draco.format.dracon.Dracon], msg: draco.format.dracon.Dracon): Behavior[draco.format.dracon.Dracon] = {
      lazy val cursor: io.circe.HCursor = msg.json.hcursor
      lazy val position: domains.aerial.Position = Position(_latitude = cursor.get[Double]("latitude").getOrElse(0.0), _longitude = cursor.get[Double]("longitude").getOrElse(0.0), _altitudeFeet = cursor.get[Int]("altitudeFeet").getOrElse(0))
      worldConsumer(position)
      Behaviors.same[draco.format.dracon.Dracon]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.format.dracon.Dracon], signal: Signal): Behavior[draco.format.dracon.Dracon] = {
      Behaviors.same[draco.format.dracon.Dracon]
    }
  }
}
