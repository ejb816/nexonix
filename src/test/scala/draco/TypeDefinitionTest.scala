package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TypeDefinitionTest extends AnyFunSuite {
  test("TypeDefinition") {
    println(TypeDefinition.Null.asJson.spaces2)
  }

}
