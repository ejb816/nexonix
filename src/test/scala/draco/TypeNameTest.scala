package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TypeNameTest extends AnyFunSuite {
  test("TypeName") {
    println("draco.TypeName.Null:")
    println(TypeName.Null.asJson.spaces2)
    println(s"""draco.TypeName("TypeName", "Draco", Seq[String]()):""")
    println(TypeName("TypeName", "Draco").asJson.spaces2)
  }
}
