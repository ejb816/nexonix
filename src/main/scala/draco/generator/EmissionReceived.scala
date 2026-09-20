package draco.generator

import draco._
import org.evrete.api.{Knowledge, RhsContext}

trait EmissionReceived

object EmissionReceived extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("EmissionReceived", _namePackage = Seq ("draco", "generator")))
  lazy val dracoType: Type[EmissionReceived] = Type[EmissionReceived] (typeDefinition)
  lazy val domainType: Domain[draco.generator.Generator] = Domain[draco.generator.Generator] (typeDefinition)

  private lazy val action: RhsContext => Unit = (ctx: RhsContext) => {
      val emission: Emission = ctx.get[Emission]("$emission")
      lazy val received: java.util.List[Emission] = ctx.getRuntime().get("received")
      received.add(emission)
  }

  private lazy val pattern: Knowledge => Unit = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.generator.EmissionReceived")
    .forEach (
      "$emission", classOf[Emission]
    )

    .execute (action(_))
    .build()
  }

  lazy val ruleType: RuleType = Rule[EmissionReceived] (
    _pattern = pattern,
    _action = action
  )
}
