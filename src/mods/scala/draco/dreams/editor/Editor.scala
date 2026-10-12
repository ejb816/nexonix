package draco.dreams.editor

import draco.dreams._
import draco._
import scala.collection.mutable
import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Editor extends DracoType

object Editor extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Editor", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[Editor] = Type[Editor] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Actual", "Latent")

  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  private lazy val knowledge: Knowledge = Rule.knowledgeService.newKnowledge("Editor")

  def actorType(_ontology: => draco.DomainOntology, _actual: => Actual => Unit): ActorType = new Actor[Latent] {
    override lazy val typeDefinition: TypeDefinition = Editor.typeDefinition
    lazy val ontology: draco.DomainOntology = _ontology
    lazy val actual: Actual => Unit = _actual

    lazy val rules: org.evrete.api.Knowledge = draco.Rule.knowledgeService.newKnowledge("Editor")
    draco.Completeness.ruleType.pattern(rules)
    draco.SelfDeclaration.ruleType.pattern(rules)
    draco.DerivationResolvable.ruleType.pattern(rules)
    draco.Uniqueness.ruleType.pattern(rules)
    draco.CollectProblems.ruleType.pattern(rules)
    var accepted: draco.DomainOntology = ontology
    lazy val accept: draco.DomainOntology => Unit = next => accepted = next

    override def receive(ctx: TypedActorContext[Latent], msg: Latent): Behavior[Latent] = {
      lazy val candidate: draco.DomainType = draco.Domain(msg.definition, msg.members)
      lazy val provisional: draco.DomainOntology = accepted.including(candidate)
      lazy val definitions: Seq[draco.TypeDefinition] = provisional.keys.toSeq.flatMap(domain => domain.typeDictionary.elementTypes.prepended(domain.typeDefinition))
      lazy val problems: mutable.Buffer[draco.Problem] = scala.collection.mutable.ArrayBuffer()
      lazy val session: org.evrete.api.StatefulSession = rules.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)
      session.set("problems", problems)
      session.insert(provisional.keys.toSeq: _*)
      session.insert(definitions.toSeq: _*)
      session.insert(Seq(provisional): _*)
      session.fire()
      session.close()
      (if (problems.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(accept(provisional), accept(accepted))
      actual(Actual(msg.definition, msg.members, problems.toSeq))
      Behaviors.same[Latent]
    }

    override def receiveSignal(ctx: TypedActorContext[Latent], signal: Signal): Behavior[Latent] = {
      Behaviors.same[Latent]
    }
  }
}
