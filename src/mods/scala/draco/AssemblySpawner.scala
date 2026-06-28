package draco

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.ActorContext

/** The single Pekko-touching consumer of an [[Assembly]]. Everything draco knows
  * about an actor group is data ([[Assembly]] / [[Binding]], addressed by
  * [[TypeName]] identity, never by `ActorRef`); this object is where that data
  * meets a live `ActorContext` and becomes running actors.
  *
  * It is fully generic over the topology: it topologically sorts the members so
  * each is spawned only after the members it depends on, resolves each member's
  * construction refs in factory-parameter order, and returns the entry ref.
  *
  * The one thing it cannot do generically is call a member's typed
  * `actorType(...)` — that signature is per-type. The caller supplies, per member
  * `namePath`, a [[Constructor]] adapter that takes the resolved dependency refs
  * (in factory-parameter order) and yields the member's `Behavior`. This adapter
  * is the irreducible typed bridge from identity-addressed data to a constructor;
  * it is the wiring glue, not topology — the topology stays in the [[Assembly]].
  */
object AssemblySpawner {

  /** Per-member adapter: resolved dependency refs (factory-parameter order) ->
    * the member's behavior. Typically `refs => Member.actorType(refs(0)...)`. */
  type Constructor = Seq[ActorRef[Any]] => Behavior[Any]

  def spawn[T](assembly: Assembly, constructors: Map[String, Constructor])
              (ctx: ActorContext[_]): ActorRef[T] = {

    val bindingsFrom: Map[String, Seq[Binding]] =
      assembly.bindings.groupBy(_.from.namePath)

    val spawned = scala.collection.mutable.Map.empty[String, ActorRef[Any]]

    spawnOrder(assembly, bindingsFrom).foreach { member =>
      val ctor = constructors.getOrElse(
        member.namePath,
        throw new IllegalArgumentException(s"no constructor supplied for member '${member.namePath}'")
      )

      // Resolve this member's dependency refs in the order its factory declares them.
      val paramOrder = Generator.loadType(member).dracoAspect.factory.parameters.map(_.name)
      val toByParam  = bindingsFrom.getOrElse(member.namePath, Seq.empty)
        .map(b => b.param -> b.to.namePath).toMap
      val refs: Seq[ActorRef[Any]] =
        paramOrder.flatMap(p => toByParam.get(p).map(spawned))

      // Name by namePath (unique across members) so two members with the same
      // simple name — e.g. world.Consumer and terrestrial.Consumer — don't collide
      // as sibling actor names under the guardian.
      spawned += member.namePath -> ctx.spawn(ctor(refs), member.namePath.replace('.', '_'))
    }

    spawned(assembly.entry.namePath).asInstanceOf[ActorRef[T]]
  }

  /** Members ordered so every member follows the members it depends on. */
  private def spawnOrder(assembly: Assembly, bindingsFrom: Map[String, Seq[Binding]]): Seq[TypeName] = {
    val byPath  = assembly.members.map(m => m.namePath -> m).toMap
    val ordered = scala.collection.mutable.LinkedHashMap.empty[String, TypeName]
    def visit(member: TypeName): Unit =
      if (!ordered.contains(member.namePath)) {
        bindingsFrom.getOrElse(member.namePath, Seq.empty)
          .foreach(b => byPath.get(b.to.namePath).foreach(visit))
        ordered += member.namePath -> member
      }
    assembly.members.foreach(visit)
    ordered.values.toSeq
  }
}
