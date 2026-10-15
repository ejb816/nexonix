package draco.dreams.editor

import draco.dreams._
import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait UpdateUnheld

object UpdateUnheld extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("UpdateUnheld", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[UpdateUnheld] = Type[UpdateUnheld] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)
  def w0(u: draco.dreams.user.Update, a: Accepted): Boolean = !a.ontology.holds(u.definition.typeName)
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val u: draco.dreams.user.Update = ctx.get[draco.dreams.user.Update]("$u")
      val a: Accepted = ctx.get[Accepted]("$a")
      ctx.insert(draco.Problem(u.definition.typeName, s"domain ${u.definition.typeName.name} is not in the accepted ontology"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.dreams.editor.UpdateUnheld")
    .forEach (
      "$u", classOf[draco.dreams.user.Update],
      "$a", classOf[Accepted]
    )
    .where("draco.dreams.editor.UpdateUnheld.w0($u, $a)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[UpdateUnheld] (
    _pattern = pattern,
    _action = action
  )
}
