package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TypeNameTest extends AnyFunSuite {
  test("TypeName") {
    println("draco.TypeName.Null:")
    println(TypeName.Null.asJson.spaces2)
    val typeTypeName: TypeName = TypeName ("TypeName")
    println(typeTypeName.asJson.spaces2)
  }
}
