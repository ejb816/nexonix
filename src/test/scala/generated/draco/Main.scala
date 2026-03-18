
package generated.draco

import draco._
import java.net.URI

trait Main  {
  val sourceRoot: URI
  val sinkRoot: URI
}

object Main extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Main", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Main] = Type[Main] (typeDefinition)

  def apply (
    _sourceName: String,
    _sinkName: String
  ) : Main = new Main {
    override val sourceName: String = _sourceName
    override val sinkName: String = _sinkName
    override lazy val typeInstance: DracoType = Main.typeInstance
    override lazy val typeDefinition: TypeDefinition = Main.typeDefinition
  }

  lazy val Null: Main = apply(
    _sourceName = "",
    _sinkName = ""
  )


}
