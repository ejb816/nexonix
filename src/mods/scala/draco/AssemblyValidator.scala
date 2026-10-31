package draco

import draco.scalatarget.DefinitionTrees._

import draco.TypeForm.{ValueTypeText, DraconValueTypeText}
import io.circe.Json

/** Validates an [[Assembly]] purely from type definitions — no Pekko, no
  * `ActorContext`, no spawning. This is the property that makes Assembly worth
  * having: the wiring of an actor group is data that can be checked statically.
  *
  * For each [[Binding]] `from.param <- to` the validator loads the member type
  * definitions and confirms:
  *   - `from` and `to` are declared members of the assembly (so is `entry`);
  *   - `from`'s factory declares a construction parameter named `param`;
  *   - that parameter's type is `ActorRef[M]`;
  *   - `M` matches the message type of `to` (the actor aspect's `messageType`) by
  *     IDENTITY — name and package, read through each definition's own resolution
  *     record, so the compact spelling of one side meets the full spelling of the other.
  *
  * `validate` returns the list of problems found; an empty list means the
  * assembly is well-formed. Nothing here touches the actor runtime.
  */
object AssemblyValidator {

  /** The message type a CONSUMER parameter accepts — `M -> ()`, drake's own arrow whose
    * result is the unit type, the empty Objective (2026-10-21; the host leaf `Unit` before)
    * (2026-09-18; the host's `ActorRef[M]` before) — or None if the type is not one. The
    * assembly keeps the refs; a member sees only a function it applies. */
  private def actorRefInner(valueType: Json): Option[String] =
    TypeForm.node(valueType).collect { case ("->", Vector(m, u)) if TypeForm.node(u).contains(("(,)", Vector.empty)) => m.text }

  /** Message type a member receives. The actor ASPECT is the source of truth —
    * actor-ness is aspect presence, so an `Actor[M]` derivation would put a role's
    * parameter into the data inheritance tree. The derivation is read only as a
    * fallback, for definitions not yet migrated (DracoGenerator.actorMessageType makes
    * the same choice, and for the same reason). */
  private def messageType(td: TypeDefinition): Option[String] =
    Some(td.actorAspect.fold(ActorAspect.Null, (a: ActorAspect) => a).messageType)
      .filter(_.name.nonEmpty)
      .map(_.namePath)
      .orElse(
        DracoAspect.parents(td.dracoAspect)
          .find(_.name == "Actor")
          .flatMap(_.typeParameters.headOption).map(_.text))
      .map(_.trim)

  /** A message type's IDENTITY — name and package, parameters free, as definitions are keyed
    * (2026-10-31; compared as text before, the third consumer of the type vocabulary beside the two
    * renderers, as the unit type found it on 2026-10-21). A definition spells a draco name in the
    * COMPACT form since the corpus sweep of 2026-10-21 and the example sweep of 2026-10-31: a name of
    * another package short and recorded in `uses`, an own-package name bare and unrecorded — so a
    * consumer's `Dracon -> ()` and an actor's `messageType draco format dracon Dracon` are one
    * identity, and world Consumer's `World -> ()` is Provider's bare `messageType World`. The
    * definition's own record is the scope, `NameResolution.recorded`, the reading the generator gives
    * a recorded definition; a name the record does not know is completed with the definition's own
    * package, the compact form's convention for a bare name. Nothing is loaded or discovered. */
  private def identity(td: TypeDefinition, text: String): String = {
    val resolved = NameResolution.recorded(td).resolveName(text.trim)
    if (resolved.namePackage.isEmpty) TypeName(resolved.name, td.typeName.namePackage).namePath else resolved.namePath
  }

  def validate(assembly: Assembly): Seq[String] = {
    val memberNames: Set[String] = assembly.members.map(_.namePath).toSet
    val errors = Seq.newBuilder[String]

    def requireMember(tn: TypeName, role: String): Unit =
      if (!memberNames.contains(tn.namePath))
        errors += s"$role '${tn.namePath}' is not a declared member of the assembly"

    if (assembly.entry.name.nonEmpty) requireMember(assembly.entry, "entry")
    else errors += "assembly has no entry member"

    assembly.bindings.foreach { b =>
      requireMember(b.from, "binding source")
      requireMember(b.to, "binding target")

      val fromTd = DracoGenerator.loadType(b.from)
      val toTd   = DracoGenerator.loadType(b.to)

      fromTd.dracoAspect.factory.parameters.find(_.name == b.param) match {
        case None =>
          errors += s"'${b.from.namePath}' declares no construction parameter '${b.param}'"
        case Some(param) =>
          actorRefInner(param.valueType) match {
            case None =>
              errors += s"parameter '${b.param}' of '${b.from.namePath}' is '${param.valueType.text}', not a consumer (M -> ())"
            case Some(refInner) =>
              messageType(toTd).map(identity(toTd, _)) match {
                case None =>
                  errors += s"binding target '${b.to.namePath}' is not an Actor[_] (no message type to satisfy '${b.param}')"
                case Some(msg) if msg != identity(fromTd, refInner) =>
                  errors += s"binding '${b.from.namePath}.${b.param}' expects a consumer of ${identity(fromTd, refInner)} but '${b.to.namePath}' receives $msg"
                case Some(_) => // matched
              }
          }
      }
    }

    errors.result()
  }

  def isValid(assembly: Assembly): Boolean = validate(assembly).isEmpty
}
