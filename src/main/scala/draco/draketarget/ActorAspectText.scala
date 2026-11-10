package draco.draketarget

import draco._

trait ActorAspectText extends Surface

object ActorAspectText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ActorAspectText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[ActorAspectText] = Type[ActorAspectText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _messageReference: => String = "",
    _start: => Seq[String] = Seq.empty,
    _message: => Seq[String] = Seq.empty,
    _signal: => Seq[String] = Seq.empty,
    _present: => Boolean = true
  ) : ActorAspectText = new ActorAspectText {
    lazy val typeLines: Seq[String] = (if (_messageReference.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  messageType " ++ _messageReference))
    lazy val startLines: Seq[String] = (if (_start.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  start") ++ _start)
    lazy val messageLines: Seq[String] = (if (_message.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  message") ++ _message)
    lazy val signalLines: Seq[String] = (if (_signal.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(Seq.empty, Seq("  signal") ++ _signal)
    override lazy val value: String = (if (_present) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse((Seq("actor") ++ typeLines ++ startLines ++ messageLines ++ signalLines).mkString("\n"), "")
    override lazy val typeDefinition: TypeDefinition = ActorAspectText.typeDefinition
  }

  lazy val Null: ActorAspectText = apply()


}
