package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class DracoTest extends AnyFunSuite {
  test("Draco") {
    val typeDefinition = Draco.draco.typeDefinition
    assert(typeDefinition != TypeDefinition.Null)
    println(typeDefinition.asJson.spaces2)
  }
}
