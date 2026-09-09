package draco.gendrake

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait Emit

object Emit extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emit", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[Emit] = Type[Emit] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val definition: TypeDefinition = ctx.get[TypeDefinition]("$definition")
      val emission: Emission = Emission(definition)
      ctx.insert(emission)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.gendrake.Emit")
    .forEach (
      "$definition", classOf[TypeDefinition]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[Emit] (
    _pattern = pattern,
    _action = action
  )
}
