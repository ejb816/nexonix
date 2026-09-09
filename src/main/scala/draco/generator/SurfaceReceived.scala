package draco.generator

import draco._
import org.evrete.api.{Knowledge, RhsContext}
import java.util.function.Consumer

trait SurfaceReceived

object SurfaceReceived extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("SurfaceReceived", _namePackage = Seq ("draco", "generator")))
  lazy val dracoType: Type[SurfaceReceived] = Type[SurfaceReceived] (typeDefinition)
  lazy val domainType: Domain[draco.generator.Generator] = Domain[draco.generator.Generator] (typeDefinition)

  private lazy val action: Consumer[RhsContext] = (ctx: RhsContext) => {
      val surface: draco.draketarget.Surface = ctx.get[draco.draketarget.Surface]("$surface")
      val received: java.util.List[draco.draketarget.Surface] = ctx.getRuntime().get("received")
      received.add(surface)
  }

  private lazy val pattern: Consumer[Knowledge] = (knowledge: Knowledge) => {
    knowledge
    .builder()
    .newRule ("draco.generator.SurfaceReceived")
    .forEach (
      "$surface", classOf[draco.draketarget.Surface]
    )

    .execute (action)
    .build()
  }

  lazy val ruleType: RuleType = Rule[SurfaceReceived] (
    _pattern = pattern,
    _action = action
  )
}
