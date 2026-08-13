package draco

import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.evrete.api.Knowledge

trait Draco extends DracoType

object Draco extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Draco", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Draco] = Type[Draco] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Action", "Actor", "ActorAspect", "ActorType", "Aspects", "Assembly", "Binding", "BodyElement", "CLI", "CodecAspect", "CollectProblems", "Completeness", "Condition", "ContentSink", "DefinitionPath", "DerivationResolvable", "Dictionary", "Domain", "DomainAspect", "DomainDictionary", "DomainTransform", "DomainType", "DracoAspect", "DracoType", "Dynamic", "Factory", "Fixed", "Holon", "Main", "Monadic", "Mutable", "Parameter", "Pattern", "Primal", "Problem", "REPL", "Rule", "RuleAspect", "RuleType", "SelfDeclaration", "Source", "SourceContent", "Test", "Type", "TypeDefinition", "TypeDictionary", "TypeElement", "TypeLoader", "TypeName", "TypeTransform", "Variable")

  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Draco")
    CollectProblems.ruleType.pattern.accept(k)
    Completeness.ruleType.pattern.accept(k)
    DerivationResolvable.ruleType.pattern.accept(k)
    SelfDeclaration.ruleType.pattern.accept(k)
    k
  }

  def actorType(problems: java.util.List[Problem]): ActorType = new Actor[DracoType] {
    override lazy val typeDefinition: TypeDefinition = Draco.typeDefinition

    val session: org.evrete.api.StatefulSession = knowledge.newStatefulSession(org.evrete.api.ActivationMode.CONTINUOUS)
    session.set("problems", problems)

    override def receive(ctx: TypedActorContext[DracoType], msg: DracoType): Behavior[DracoType] = {
      session.insert(Seq(msg): _*)
      session.fire()
      Behaviors.same[DracoType]
    }

    override def receiveSignal(ctx: TypedActorContext[DracoType], signal: Signal): Behavior[DracoType] = {
      signal match {
        case org.apache.pekko.actor.typed.PostStop =>
          session.close()
          Behaviors.same[DracoType]
        case _ => Behaviors.same[DracoType]
      }
    }
  }
}
