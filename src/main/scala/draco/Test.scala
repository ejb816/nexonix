package draco

import java.net.URI

trait Test extends Main

object Test extends App {
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
