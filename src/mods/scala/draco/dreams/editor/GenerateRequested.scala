package draco.dreams.editor

import draco.drake.Presence
import draco.dreams.user.Generate
import draco.dreams.user.Response

import draco.dreams._
import draco._
import org.evrete.api.{Knowledge, RhsContext}
import scala.collection.mutable

trait GenerateRequested

object GenerateRequested extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("GenerateRequested", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[GenerateRequested] = Type[GenerateRequested] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val g: Generate = ctx.get[Generate]("$g")
      val a: Accepted = ctx.get[Accepted]("$a")
      val request: Generate = g
      val held: draco.DomainOntology = a.ontology
      val bindings: Seq[TransformBinding] = ctx.getRuntime().get("transforms")
      val candidates: Seq[TransformBinding] = bindings.filter(b => b.domain.typeDefinition.typeName.namePath == request.transform.namePath)
      val binding: Presence[TransformBinding] = (candidates.headOption match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
      lazy val issues: Seq[draco.Problem] = (if (held.holds(request.domain)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(request.domain, "domain is not in the accepted ontology"))) ++ (if (held.holds(request.transform)) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(request.transform, "transform is not in the accepted ontology"))) ++ (if (candidates.size == 1) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(request.transform, "transform requires exactly one executable binding"))) ++ (if (binding.fold(true, b => b.matches(held))) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq(draco.Problem(request.transform, "executable binding does not match the held transform definitions")))
      issues.foreach(p => ctx.insert(p))
      lazy val responses: mutable.Buffer[Seq[draco.Problem] => Response] = ctx.getRuntime().get("responses")
      lazy val respond: Seq[draco.Problem] => Response = problems => (if (problems.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(binding.fold(draco.dreams.editor.Generated(request.domain, request.transform, Seq.empty, problems), b => b.generate(request.domain, request.transform, request.contents(held).prepended(request.subject(held)), held)), draco.dreams.editor.Generated(request.domain, request.transform, Seq.empty, problems))
      responses += respond
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.dreams.editor.GenerateRequested")
    .forEach (
      "$g", classOf[Generate],
      "$a", classOf[Accepted]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[GenerateRequested] (
    _pattern = pattern,
    _action = action
  )
}
