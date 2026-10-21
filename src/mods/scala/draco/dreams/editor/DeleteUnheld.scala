package draco.dreams.editor

import draco.dreams.user.Delete

import draco.dreams._
import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait DeleteUnheld

object DeleteUnheld extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DeleteUnheld", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[DeleteUnheld] = Type[DeleteUnheld] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)
  def w0(d: Delete, a: Accepted): Boolean = !a.ontology.holds(d.domain)
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val d: Delete = ctx.get[Delete]("$d")
      val a: Accepted = ctx.get[Accepted]("$a")
      ctx.insert(draco.Problem(d.domain, s"domain ${d.domain.name} is not in the accepted ontology"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.dreams.editor.DeleteUnheld")
    .forEach (
      "$d", classOf[Delete],
      "$a", classOf[Accepted]
    )
    .where("draco.dreams.editor.DeleteUnheld.w0($d, $a)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[DeleteUnheld] (
    _pattern = pattern,
    _action = action
  )
}
