package draco

import java.net.URI

trait Test extends Main

object Test extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Test",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("Main", _namePackage = Seq ("draco"))
    ),
    _factory = Factory (
      "Test",
      _parameters = Seq (
        Parameter ("sourceName", "String", ""),
        Parameter ("sinkName", "String", "")
      )
    )
  )
  lazy val typeInstance: Type[Test] = Type[Test] (typeDefinition)

  def apply (
              _sourceName: String,
              _sinkName: String
            ) : Test = new Test {
    override val sourceRoot: URI = classOf[Test]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/test/${_sourceName}/"))
    override val sinkRoot: URI = classOf[Test]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/test/${_sinkName}/"))
  }
  lazy val roots: Test = Test("resources","scala")
}
