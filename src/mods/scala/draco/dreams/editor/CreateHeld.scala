package draco.dreams.editor

import draco.dreams._
import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait CreateHeld

object CreateHeld extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CreateHeld", _namePackage = Seq ("draco", "dreams", "editor")))
  lazy val dracoType: Type[CreateHeld] = Type[CreateHeld] (typeDefinition)
  lazy val domainType: Domain[Editor] = Domain[Editor] (typeDefinition)
  def w0(c: draco.dreams.user.Create, a: Accepted): Boolean = a.ontology.holds(c.definition.typeName)
  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val c: draco.dreams.user.Create = ctx.get[draco.dreams.user.Create]("$c")
      val a: Accepted = ctx.get[Accepted]("$a")
      ctx.insert(draco.Problem(c.definition.typeName, s"domain ${c.definition.typeName.name} is already in the accepted ontology"))
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.dreams.editor.CreateHeld")
    .forEach (
      "$c", classOf[draco.dreams.user.Create],
      "$a", classOf[Accepted]
    )
    .where("draco.dreams.editor.CreateHeld.w0($c, $a)")
    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[CreateHeld] (
    _pattern = pattern,
    _action = action
  )
}
