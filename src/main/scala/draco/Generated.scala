package draco

import draco.format.dracon.Dracon

import java.net.URI

trait Generated extends Main

object Generated extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Generated",
      _namePackage = Seq ("draco")
    ),
    _dracoAspect = DracoAspect (
      _derivation = Seq (TypeName.write (TypeName ("Main", _namePackage = Seq ("draco")))),
      _factory = Factory (
        Dracon.string ("Generated"),
        _parameters = Seq (
          Parameter ("sourceName", Dracon.string ("String"), Dracon.Null),
          Parameter ("sinkName", Dracon.string ("String"), Dracon.Null)
        )
      )
    )
  )
  lazy val dracoType: Type[Generated] = Type[Generated] (typeDefinition)

  def apply (
              _sourceName: String,
              _sinkName: String
            ) : Generated = new Generated {
    override val typeDefinition: TypeDefinition = Generated.typeDefinition
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
