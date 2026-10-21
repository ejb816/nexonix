package draco.gendrake

import draco.generator.Emission

import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait Emit

object Emit extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emit", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[Emit] = Type[Emit] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val definition: draco.TypeDefinition = ctx.get[draco.TypeDefinition]("$definition")
      lazy val emission: Emission = Emission(draco.Drake.emitFromSession(definition, ctx.getRuntime().get("nameResolution")))
      ctx.insert(emission)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.gendrake.Emit")
    .forEach (
      "$definition", classOf[draco.TypeDefinition]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[Emit] (
    _pattern = pattern,
    _action = action
  )
}
