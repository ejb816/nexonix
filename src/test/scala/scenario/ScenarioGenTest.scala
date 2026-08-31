package scenario

import draco._
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** The forest scenario as a CORPUS, gated the way `src/main` is gated.
 *
 *  The scenario used to be `.drake` and nothing else, and an earlier version of this
 *  test manufactured the missing two thirds in temp space: parse drake in memory,
 *  write JSON to a scratch classpath root so `TypeLoader` could resolve names, compile
 *  to a scratch directory, delete both. Nothing was generated that outlived the test,
 *  which meant nothing was generated at all. The scenario is now a full trio —
 *  `X.drake` beside `X.json` under `src/test/resources/scenario`, and the projection
 *  committed under `src/test/scala/scenario` — with the package of every artifact
 *  taken from the type's own `TypeName`, so the three agree by construction.
 *
 *  What that buys is that this file gets SMALLER. Because the generated Scala is a
 *  real source tree, sbt compiles it, so compilation needs no gate of its own — the
 *  same transitive argument `DracoGenTest` rests on. And because the scenario types
 *  are on the classpath like any others, the execution gate below calls them
 *  directly: no reflection, no child classloader, no private `KnowledgeService`.
 *
 *  Four gates:
 *
 *  1. PROJECTS — `Generator.generate` from each `.json` reproduces the committed
 *     `.scala`. This is `DracoGenTest`'s gate, and like it, it asserts.
 *  2. MEANS IT — the rules an actor's domain chain OWNS against the ones its projected
 *     source actually ACCEPTS. An actor whose `Knowledge` accepts nothing compiles,
 *     runs, and matches nothing, with no error anywhere to show for it, so this is the
 *     failure gate 1 cannot see.
 *  3. IS CONSISTENT — every member of a transform domain is one leaf conversion:
 *     deriving a type in `target`, taking a factory parameter typed in `source`. With
 *     the direction declared on the domain aspect, that convention is checkable.
 *  4. RUNS — fire the rules for real. A projection that type-checks and does nothing
 *     is the failure mode every gate above this one misses. */
class ScenarioGenTest extends AnyFunSuite with PersistentTestLog {

  private val resourceRoot = Paths.get("src/test/resources")
  private val sourceRoot   = Paths.get("src/test/scala")
  private val scenarioRoot = resourceRoot.resolve("scenario")

  private def definitions: Seq[TypeDefinition] =
    if (!Files.isDirectory(scenarioRoot)) Seq.empty
    else Using.resource(Files.walk(scenarioRoot)) { s =>
      s.iterator.asScala
        .filter(p => Files.isRegularFile(p) && p.toString.endsWith(".json"))
        .toList.sorted
        .flatMap(p => io.circe.parser.parse(new String(Files.readAllBytes(p))).flatMap(_.as[TypeDefinition]).toOption)
    }

  /** The projected source for a type, at the path its own TypeName names. */
  private def scalaPath(tn: TypeName): Path =
    sourceRoot.resolve(tn.namePackage.mkString("/")).resolve(tn.name + ".scala")

  private def normalize(source: String): String =
    source.replace("\r\n", "\n").split('\n').map(_.replaceAll("\\s+$", "")).filter(_.nonEmpty).mkString("\n")

  // ---- Gate 1 ----

  test("every scenario definition projects to the Scala committed beside it") {
    val defs = definitions
    assume(defs.nonEmpty, s"no scenario definitions under $scenarioRoot")

    val drift = defs.flatMap { td =>
      val path = scalaPath(td.typeName)
      if (!Files.isRegularFile(path)) Some(s"${td.typeName.namePath}: no projected source at $path")
      else {
        val generated = normalize(Generator.generate(td))
        val onDisk    = normalize(new String(Files.readAllBytes(path)))
        if (generated == onDisk) None
        else {
          log.info(s"\n--- ${td.typeName.namePath}: committed ---\n$onDisk")
          log.info(s"\n--- ${td.typeName.namePath}: generated ---\n$generated")
          Some(s"${td.typeName.namePath}: generated source differs from $path")
        }
      }
    }
    log.info(s"projected ${defs.size} definitions, ${drift.size} differ")
    assert(drift.isEmpty, drift.mkString("\n  - "))
  }

  // ---- Gate 2 ----

  /** The domains an actor's rules may come from: its own, then up the super chain. A
    * super-domain OWNS rules its members' actors have to see — a transform rule belongs
    * to the root spanning both sides, not to either side. */
  private def domainChain(d: TypeDefinition, seen: Set[String] = Set.empty): Seq[TypeDefinition] =
    if (d.typeName.name.isEmpty || seen.contains(d.typeName.namePath)) Seq.empty
    else d +: (if (d.dracoAspect.superDomain.name.isEmpty) Seq.empty
               else domainChain(TypeLoader.loadType(d.dracoAspect.superDomain), seen + d.typeName.namePath))

  private def rulesIn(domain: TypeDefinition): Seq[String] =
    domain.domainAspect.elementTypeNames
      .map(n => TypeLoader.loadType(TypeName(n, _namePackage = domain.typeName.namePackage)))
      .filter(m => !RuleAspect.isEmpty(m.ruleAspect))
      .map(_.typeName.name)

  test("an actor accepts every rule its domain chain owns") {
    val actors = definitions.filter(td => !ActorAspect.isEmpty(td.actorAspect))
    assume(actors.nonEmpty, "no actors in the scenario")

    val shortfall = actors.map { td =>
      val chain = domainChain(TypeLoader.loadType(td.domainAspect.typeName))
      val owned = chain.flatMap(rulesIn)
      // Count what the PROJECTED SOURCE wires in, not what the definitions say. The
      // definitions carried `super` all along; for a while nothing downstream did
      // anything with it, and a gate reading the definitions would have reported that
      // as healthy.
      val accepted = Using.resource(scala.io.Source.fromFile(scalaPath(td.typeName).toFile))(
        _.getLines().count(_.contains(".ruleType.pattern.accept(")))
      chain.foreach { d =>
        val label = if (d.typeName.namePath == td.domainAspect.typeName.namePath) "own domain" else "super"
        log.info(f"  ${td.typeName.namePath}%-28s $label%-10s ${d.typeName.namePath}%-24s owns=${rulesIn(d).size} ${rulesIn(d).mkString("[", ", ", "]")}")
      }
      log.info(f"  ${""}%-28s ${"ACCEPTED"}%-10s in projected source      $accepted of ${owned.size}")
      (td.typeName.namePath, owned.size - accepted)
    }.filter(_._2 != 0)

    assert(shortfall.isEmpty,
      "an actor's Knowledge does not accept every rule its domain chain owns: " +
        shortfall.map { case (n, k) => s"$n misses $k" }.mkString(", "))
  }

  // ---- Gate 3 ----

  test("every member of a transform domain is one leaf conversion") {
    val transforms = definitions.filter(td =>
      td.domainAspect.source.name.nonEmpty && td.domainAspect.target.name.nonEmpty)
    assume(transforms.nonEmpty, "no transform domains in the scenario")

    val offShape = transforms.flatMap { dom =>
      val src = dom.domainAspect.source.namePath
      val tgt = dom.domainAspect.target.namePath
      log.info(s"  ${dom.typeName.namePath}  $src -> $tgt")
      dom.domainAspect.elementTypeNames.flatMap { n =>
        val member  = TypeLoader.loadType(TypeName(n, _namePackage = dom.typeName.namePackage))
        val derives = member.dracoAspect.derivation.filter(_.name != "DracoType")
        val params  = member.dracoAspect.factory.parameters
        // Deliberately NOT required: that a member derive the transform domain itself.
        // `Potency` IS a birch Micromolar and LIVES IN Ash_Birch — containment is the
        // domain pointer, not an inheritance edge. Everywhere else the two coincide;
        // here they come apart, and that is the shape rather than a gap.
        val toTarget   = derives.exists(d => TypeLoader.loadType(d).domainAspect.typeName.namePath == tgt)
        val fromSource = params.exists(p =>
          TypeLoader.loadType(TypeName(p.valueType.split('.').last,
            _namePackage = p.valueType.split('.').init.toSeq)).domainAspect.typeName.namePath == src)
        log.info(f"    $n%-24s derives=${derives.map(_.namePath).mkString(",")}%-32s params=${params.map(_.valueType).mkString(",")}%-30s " +
          (if (toTarget && fromSource) "OK" else "OFF SHAPE"))
        if (toTarget && fromSource) None
        else Some(s"${dom.typeName.namePath}.$n: " + ((toTarget, fromSource) match {
          case (false, true) => s"derives nothing in target $tgt"
          case (true, false) => s"takes no parameter from source $src"
          case _             => s"neither derives from $tgt nor takes from $src"
        }))
      }
    }
    assert(offShape.isEmpty, offShape.mkString("\n  - "))
  }

  // ---- Gate 4 ----

  test("the forest runs: an ash alarm crosses to birch, attenuated") {
    import scenario.ash.{AshJasmonate, Compound, Micromolar}

    val received  = new java.util.ArrayList[scenario.birch.BirchJasmonate]()
    val knowledge = Rule.knowledgeService.newKnowledge("scenarioForest")
    // The same two rules the actor's Knowledge accepts, in the same order.
    scenario.forest.AshBirchAlarm.ruleType.pattern.accept(knowledge)
    scenario.forest.BirchAlarmReceived.ruleType.pattern.accept(knowledge)

    // A CHAIN — one rule's output is the other's input — so the DEFAULT activation mode
    // is the right one and its fire cycle walks the generations. CONTINUOUS is for
    // independent siblings over one fixed fact set.
    val session = knowledge.newStatefulSession()
    try {
      session.set("received", received)
      // Above the 0.5 threshold and below it: the condition has to be doing work, or a
      // rule that fired unconditionally would pass this gate just as well.
      session.insert(Seq(
        AshJasmonate(Micromolar(0.8), Compound("jasmonic-acid")),
        AshJasmonate(Micromolar(0.3), Compound("too-faint"))): _*)
      session.fire()
    } finally session.close()

    val crossed = received.asScala.toSeq
    crossed.foreach(j => log.info(f"  crossed: potency=${j.potency.value}%.3f uM  compound=${j.compound.value}"))
    console.info(s"the forest runs: ${crossed.size} of 2 ash alarms crossed to birch")

    assert(crossed.size == 1, s"expected exactly the above-threshold alarm to cross, got ${crossed.size}")
    // 0.62 lives nowhere but Potency's factory body, so the number is the evidence that
    // the conversion formula survived projection.
    assert(math.abs(crossed.head.potency.value - 0.8 * 0.62) < 1e-9,
      s"expected 0.8 * 0.62, got ${crossed.head.potency.value}")
    assert(crossed.head.compound.value == "jasmonic-acid")
  }
}
