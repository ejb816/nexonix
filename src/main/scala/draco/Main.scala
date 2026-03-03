package draco

import java.net.URI

trait Main {
  val sourceRoot: URI
  val sinkRoot: URI
}

object Main extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Main",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("sourceRoot", "URI"),
      Fixed ("sinkRoot", "URI")
    ),
    _factory = Factory (
      "Main",
      _parameters = Seq (
        Parameter ("sourceName", "String", ""),
        Parameter ("sinkName", "String", "")
      )
    )
  )
  lazy val typeInstance: Type[Main] = Type[Main] (typeDefinition)

  def apply (
              _sourceName: String,
              _sinkName: String
            ) : Main = new Main {
    override val sourceRoot: URI = classOf[Main]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/main/${_sourceName}/"))
    override val sinkRoot: URI = classOf[Test]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/main/${_sinkName}/"))
  }
  lazy val roots: Main = Main ("resources", "scala")
}
