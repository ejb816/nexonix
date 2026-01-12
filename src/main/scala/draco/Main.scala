package draco

import java.net.URI

trait Main {
  val sourceRoot: URI
  val sinkRoot: URI
}

object Main {
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
