package draco.dreams.editor

import draco.dreams.user.Read

import draco.dreams._
import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait ReadUnheld

object ReadUnheld extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ReadUnheld", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[ReadUnheld] = Type[ReadUnheld] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)
  def w0(r: Read, a: Accepted): Boolean = !a.ontology.holds(r.domain)
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val r: Read = ctx.get[Read]("$r")
      val a: Accepted = ctx.get[Accepted]("$a")
      ctx.insert(draco.Problem(r.domain, s"domain ${r.domain.name} is not in the accepted ontology"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.dreams.editor.ReadUnheld")
    .forEach (
      "$r", classOf[Read],
      "$a", classOf[Accepted]
    )
    .where("draco.dreams.editor.ReadUnheld.w0($r, $a)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[ReadUnheld] (
    _pattern = pattern,
    _action = action
  )
}
