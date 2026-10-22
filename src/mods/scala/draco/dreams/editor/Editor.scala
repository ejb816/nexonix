package draco.dreams.editor

import draco.dreams.user.Response

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

  lazy val elementTypeNames: Seq[String] = Seq ("Accepted", "Actual", "CreateHeld", "DeleteUnheld", "Generated", "GenerateRequested", "TransformBinding", "ReadUnheld", "UpdateUnheld")

  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Editor")
    CreateHeld.ruleType.pattern(k)
    DeleteUnheld.ruleType.pattern(k)
    GenerateRequested.ruleType.pattern(k)
    ReadUnheld.ruleType.pattern(k)
    UpdateUnheld.ruleType.pattern(k)
    k
  }

  def actorType(_initial: => draco.DomainOntology, _actual: => Response => Unit, _ontology: => draco.DomainOntology => Unit, _transforms: => Seq[TransformBinding] = Seq.empty): ActorType = new Actor[draco.dreams.user.Request] {
    override lazy val typeDefinition: TypeDefinition = Editor.typeDefinition
    lazy val initial: draco.DomainOntology = _initial
    lazy val actual: Response => Unit = _actual
    lazy val ontology: draco.DomainOntology => Unit = _ontology
    lazy val transforms: Seq[TransformBinding] = _transforms

    lazy val rules: org.evrete.api.Knowledge = draco.Rule.knowledgeService.newKnowledge("Editor")
    draco.Completeness.ruleType.pattern(rules)
    draco.SelfDeclaration.ruleType.pattern(rules)
    draco.DerivationResolvable.ruleType.pattern(rules)
    draco.Uniqueness.ruleType.pattern(rules)
    draco.CollectProblems.ruleType.pattern(rules)
    CreateHeld.ruleType.pattern(rules)
    UpdateUnheld.ruleType.pattern(rules)
    ReadUnheld.ruleType.pattern(rules)
    DeleteUnheld.ruleType.pattern(rules)
    GenerateRequested.ruleType.pattern(rules)
    var accepted: draco.DomainOntology = initial
    lazy val accept: draco.DomainOntology => Unit = next => accepted = next

    override def receive(ctx: TypedActorContext[draco.dreams.user.Request], msg: draco.dreams.user.Request): Behavior[draco.dreams.user.Request] = {
      val provisional: draco.DomainOntology = msg.proposal(accepted)
      val subject: draco.TypeDefinition = msg.subject(accepted)
      val contents: Seq[draco.TypeDefinition] = msg.contents(accepted)
      val changing: Boolean = provisional != accepted
      lazy val definitions: Seq[draco.TypeDefinition] = provisional.keys.toSeq.flatMap(domain => domain.typeDictionary.elementTypes.prepended(domain.typeDefinition))
      lazy val problems: mutable.Buffer[draco.Problem] = scala.collection.mutable.ArrayBuffer()
      lazy val responses: mutable.Buffer[Seq[draco.Problem] => Response] = scala.collection.mutable.ArrayBuffer()
      lazy val session: org.evrete.api.StatefulSession = rules.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)
      session.set("problems", problems)
      session.set("transforms", transforms)
      session.set("responses", responses)
      session.insert(Seq(msg): _*)
      session.insert(Seq(Accepted(accepted)): _*)
      session.insert(provisional.keys.toSeq: _*)
      session.insert(definitions.toSeq: _*)
      session.insert(Seq(provisional): _*)
      session.fire()
      session.close()
      (if (problems.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(accept(provisional), accept(accepted))
      actual(responses.headOption.fold[Response](Actual(subject, contents, problems.toSeq))(respond => respond(problems.toSeq)))
      (if (problems.isEmpty && changing) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(ontology(accepted), ())
      Behaviors.same[draco.dreams.user.Request]
    }

    override def receiveSignal(ctx: TypedActorContext[draco.dreams.user.Request], signal: Signal): Behavior[draco.dreams.user.Request] = {
      Behaviors.same[draco.dreams.user.Request]
    }
  }
}
