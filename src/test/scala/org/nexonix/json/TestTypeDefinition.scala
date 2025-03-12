package org.nexonix.json

import draco.domain.{TypeDefinition, TypeName}
import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite

class TestTypeDefinition extends AnyFunSuite {
  test("Type Definition") {
    val td: TypeDefinition = TypeDefinition(
      _typeName = TypeName(
        _name = "TypeDefinition",
        _namePackage = Seq("draco", "domain")
      )
    )
    println(td.asJson)
  }
}
