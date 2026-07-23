package draco

import java.net.URI

trait Main extends DracoType {
  val sourceRoot: URI
  val sinkRoot: URI
}

object Main extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Main", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[Main] = Type[Main] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _sourceName: String,
    _sinkName: String
  ) : Main = new Main {
    override lazy val sourceRoot: URI = classOf[Main].getResource("/").toURI.resolve(java.net.URI.create(s"../../../src/main/${_sourceName}/"))
    override lazy val sinkRoot: URI = classOf[Test].getResource("/").toURI.resolve(java.net.URI.create(s"../../../src/main/${_sinkName}/"))
    override lazy val typeDefinition: TypeDefinition = Main.typeDefinition
  }

  lazy val Null: Main = apply(
    _sourceName = "",
    _sinkName = ""
  )

  lazy val roots: Main = Main ("resources", "scala")
}
