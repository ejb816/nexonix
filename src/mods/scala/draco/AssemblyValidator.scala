package draco

/** Validates an [[Assembly]] purely from type definitions — no Pekko, no
  * `ActorContext`, no spawning. This is the property that makes Assembly worth
  * having: the wiring of an actor group is data that can be checked statically.
  *
  * For each [[Binding]] `from.param <- to` the validator loads the member type
  * definitions and confirms:
  *   - `from` and `to` are declared members of the assembly (so is `entry`);
  *   - `from`'s factory declares a construction parameter named `param`;
  *   - that parameter's type is `ActorRef[M]`;
  *   - `M` matches the message type of `to` (the actuator of its `Actor[M]`
  *     derivation).
  *
  * `validate` returns the list of problems found; an empty list means the
  * assembly is well-formed. Nothing here touches the actor runtime.
  */
object AssemblyValidator {

  /** Inner type of an `ActorRef[M]` value type, or None if the type is not an
    * actor reference. */
  private def actorRefInner(valueType: String): Option[String] = {
    val trimmed = valueType.trim
    if (trimmed.startsWith("ActorRef[") && trimmed.endsWith("]"))
      Some(trimmed.stripPrefix("ActorRef[").stripSuffix("]").trim)
    else None
  }

  /** Message type a member receives — the actual of its `Actor[M]` derivation. */
  private def messageType(td: TypeDefinition): Option[String] =
    td.dracoAspect.derivation
      .find(_.name == "Actor")
      .flatMap(_.typeParameters.headOption)
      .map(_.trim)

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

      val fromTd = Generator.loadType(b.from)
      val toTd   = Generator.loadType(b.to)

      fromTd.dracoAspect.factory.parameters.find(_.name == b.param) match {
        case None =>
          errors += s"'${b.from.namePath}' declares no construction parameter '${b.param}'"
        case Some(param) =>
          actorRefInner(param.valueType) match {
            case None =>
              errors += s"parameter '${b.param}' of '${b.from.namePath}' is '${param.valueType}', not an ActorRef"
            case Some(refInner) =>
              messageType(toTd) match {
                case None =>
                  errors += s"binding target '${b.to.namePath}' is not an Actor[_] (no message type to satisfy '${b.param}')"
                case Some(msg) if msg != refInner =>
                  errors += s"binding '${b.from.namePath}.${b.param}' expects ActorRef[$refInner] but '${b.to.namePath}' receives $msg"
                case Some(_) => // matched
              }
          }
      }
    }

    errors.result()
  }

  def isValid(assembly: Assembly): Boolean = validate(assembly).isEmpty
}
