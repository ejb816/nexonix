package draco

import java.net.URI

trait Test extends Main

object Test extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Test", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Test] = Type[Test] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _sourceName: String,
    _sinkName: String
  ) : Test = new Test {
    override lazy val sourceRoot: URI = classOf[Test].getResource("/").toURI.resolve(java.net.URI.create(s"../../../src/test/${_sourceName}/"))
    override lazy val sinkRoot: URI = classOf[Test].getResource("/").toURI.resolve(java.net.URI.create(s"../../../src/test/${_sinkName}/"))
    override lazy val typeDefinition: TypeDefinition = Test.typeDefinition
  }

  lazy val Null: Test = apply(
    _sourceName = "",
    _sinkName = ""
  )

  lazy val roots: Test = Test ("resources", "scala")
}
