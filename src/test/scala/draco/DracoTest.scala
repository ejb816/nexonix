package draco

import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class DracoTest extends AnyFunSuite {
  test("Draco") {
    val typeDefinition: TypeDefinition = Draco.draco.domain.typeDefinition
    println(typeDefinition.asJson.spaces2)
    println(TypeDefinition.Null.asJson.spaces2)

    assert(typeDefinition != TypeDefinition.Null)
  }
}
