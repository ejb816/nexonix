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

  for ((name, heads) <- Seq(
    "EmptyPlainProjection" -> "",
    "EmptyRuleProjection" -> "rule\n",
    "EmptyActorProjection" -> "actor\n",
    "EmptyBothProjection" -> "rule\nactor\n")) {
    test(s"empty role projection compiles and its declared roles are usable: $name") {
      val parsed = Drake.parse(s"type $name\ndomain draco Draco\n" + heads)
      val td = TypeDefinition.decoder.decodeJson(TypeDefinition.encoder(parsed)).fold(throw _, identity)
      val source = DracoGenerator.generate(td)
      val compiled = DracoGenerator.compile(source, s"$name.scala")
      assert(compiled.isRight, compiled.left.toOption.toSeq.flatten.mkString("\n"))
      val companion = DracoGenerator.loadClass(compiled.toOption.get, s"draco.$name$$")
      val module = companion.getField("MODULE$").get(null)
      if (heads.contains("rule")) {
        val rule = companion.getMethod("ruleType").invoke(module).asInstanceOf[RuleType]
        val knowledge = Rule.knowledgeService.newKnowledge(name)
        rule.pattern(knowledge)
        val session = knowledge.newStatefulSession()
        try session.fire() finally session.close()
      }
      if (heads.contains("actor")) {
        val actor = companion.getMethod("actorType").invoke(module)
          .asInstanceOf[org.apache.pekko.actor.typed.Behavior[Any]]
        val kit = org.apache.pekko.actor.testkit.typed.scaladsl.BehaviorTestKit(actor)
        kit.run("unhandled payload")
        assert(kit.isAlive)
        assert(kit.retrieveAllEffects().isEmpty)
        kit.run(42)
        assert(kit.isAlive)
        assert(kit.retrieveAllEffects().isEmpty)
      }
    }
  }
}
