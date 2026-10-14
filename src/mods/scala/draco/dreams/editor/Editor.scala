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

  lazy val elementTypeNames: Seq[String] = Seq ("Actual")

  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  private lazy val knowledge: Knowledge = Rule.knowledgeService.newKnowledge("Editor")

  def actorType(_initial: => draco.DomainOntology, _actual: => draco.dreams.user.Response => Unit, _ontology: => draco.DomainOntology => Unit): ActorType = new Actor[draco.dreams.user.Request] {
    override lazy val typeDefinition: TypeDefinition = Editor.typeDefinition
    lazy val initial: draco.DomainOntology = _initial
    lazy val actual: draco.dreams.user.Response => Unit = _actual
    lazy val ontology: draco.DomainOntology => Unit = _ontology

    lazy val rules: org.evrete.api.Knowledge = draco.Rule.knowledgeService.newKnowledge("Editor")
    draco.Completeness.ruleType.pattern(rules)
    draco.SelfDeclaration.ruleType.pattern(rules)
    draco.DerivationResolvable.ruleType.pattern(rules)
    draco.Uniqueness.ruleType.pattern(rules)
    draco.CollectProblems.ruleType.pattern(rules)
    var accepted: draco.DomainOntology = initial
    lazy val accept: draco.DomainOntology => Unit = next => accepted = next

    override def receive(ctx: TypedActorContext[draco.dreams.user.Request], msg: draco.dreams.user.Request): Behavior[draco.dreams.user.Request] = {
      msg match {
        case latent: draco.dreams.user.Latent =>
          lazy val candidate: draco.DomainType = draco.Domain(latent.definition, latent.members)
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
          actual(Actual(latent.definition, latent.members, problems.toSeq))
          (if (problems.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(ontology(accepted), ())
        case read: draco.dreams.user.Read =>
          lazy val owner: draco.drake.Presence[draco.DomainType] = (accepted.keys.find(d => d.typeDefinition.typeName.namePath == read.domain.namePath) match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
          actual(owner.fold(Actual(draco.TypeDefinition(read.domain), Seq.empty, Seq(draco.Problem(read.domain, s"domain ${read.domain.name} is not in the accepted ontology"))), d => Actual(d.typeDefinition, d.typeDictionary.elementTypes)))
        case _ => ()
      }
      Behaviors.same[draco.dreams.user.Request]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.dreams.user.Request], signal: Signal): Behavior[draco.dreams.user.Request] = {
      Behaviors.same[draco.dreams.user.Request]
    }
  }
}
