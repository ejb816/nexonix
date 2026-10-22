package draco

import io.circe.Json
import org.scalatest.funsuite.AnyFunSuite

class RolePresenceTest extends AnyFunSuite {
  for (heads <- Seq("", "rule\n", "actor\n", "rule\nactor\n")) {
    test(s"role presence survives the carrier: ${heads.trim.replace('\n', '+')}") {
      val td = Drake.parse("type Example\ndomain example Examples\n" + heads)
      val json = TypeDefinition.encoder(td)
      val decoded = TypeDefinition.decoder.decodeJson(json).fold(throw _, identity)
      for ((head, key) <- Seq("rule" -> "ruleAspect", "actor" -> "actorAspect")) {
        val present = heads.contains(head)
        assert(json.hcursor.downField(key).succeeded == present)
        if (present) assert(json.hcursor.downField(key).focus.contains(Json.obj()))
      }
      assert(decoded.ruleAspect.fold(false, _ => true) == heads.contains("rule"))
      assert(decoded.actorAspect.fold(false, _ => true) == heads.contains("actor"))
      assert(Drake.emit(decoded) == Drake.emit(td))
      assert(TypeDefinition.encoder(Drake.parse(Drake.emit(decoded))) == json)
      val generated = DracoGenerator.generate(decoded)
      assert(generated.contains("lazy val ruleType") == heads.contains("rule"))
      assert(generated.contains("def actorType") == heads.contains("actor"))
    }
  }

}
