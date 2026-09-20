package draco.gendrake

import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait Emit

object Emit extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Emit", _namePackage = Seq ("draco", "gendrake")))
  lazy val dracoType: Type[Emit] = Type[Emit] (typeDefinition)
  lazy val domainType: Domain[GenDrake] = Domain[GenDrake] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val definition: TypeDefinition = ctx.get[TypeDefinition]("$definition")
      lazy val emission: draco.generator.Emission = draco.generator.Emission(draco.Drake.emit(definition))
      ctx.insert(emission)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.gendrake.Emit")
    .forEach (
      "$definition", classOf[TypeDefinition]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[Emit] (
    _pattern = pattern,
    _action = action
  )
}
