
package generated.draco

import draco._

trait Test extends Main 

object Test extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("Test", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[Test] = Type[Test] (typeDefinition)

  def apply (
    _sourceName: String,
    _sinkName: String
  ) : Test = new Test {
    override val sourceName: String = _sourceName
    override val sinkName: String = _sinkName
    override lazy val typeInstance: DracoType = Test.typeInstance
    override lazy val typeDefinition: TypeDefinition = Test.typeDefinition
  }

  lazy val Null: Test = apply(
    _sourceName = "",
    _sinkName = ""
  )


}
