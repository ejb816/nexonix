package draco

import java.net.URI

trait Generated extends Main

object Generated extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Generated",
      _namePackage = Seq ("draco")
    ),
    _derivation = Seq (
      TypeName ("Main", _namePackage = Seq ("draco"))
    ),
    _factory = Factory (
      "Generated",
      _parameters = Seq (
        Parameter ("sourceName", "String", ""),
        Parameter ("sinkName", "String", "")
      )
    )
  )
  lazy val typeInstance: Type[Generated] = Type[Generated] (typeDefinition)

  def apply (
              _sourceName: String,
              _sinkName: String
            ) : Generated = new Generated {
    override val sourceRoot: URI = classOf[Generated]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/generated/${_sourceName}/"))
    override val sinkRoot: URI = classOf[Generated]
      .getResource("/")
      .toURI
      .resolve(URI.create(s"../../../src/generated/${_sinkName}/"))
  }
  lazy val roots: Generated = Generated("resources", "scala")
}
