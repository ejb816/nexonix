package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition extends Aspects {
  val typeName: TypeName
}
object TypeDefinition extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition.Null 
  lazy val dracoType: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)
  def apply (
              _typeName: TypeName,
              _superDomain: TypeName = TypeName.Null,
              _modules: Seq[TypeName] = Seq.empty,
              _extensible: TypeName = TypeName.Null,
              _derivation: Seq[TypeName] = Seq.empty,
              _elements: Seq[TypeElement] = Seq.empty,
              _factory: Factory = Factory.Null,
              _globalElements: Seq[BodyElement] = Seq.empty,
              _elementTypeNames: Seq[String] = Seq.empty,
              _source: TypeName = TypeName.Null,
              _target: TypeName = TypeName.Null,
              _variables: Seq[Variable] = Seq.empty,
              _conditions: Seq[Condition] = Seq.empty,
              _values: Seq[Value] = Seq.empty,
              _pattern: Pattern = null,
              _action: Action = null,
              _messageAction: Action = null,
              _signalAction: Action = null
            ) : TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = _typeName
    override val dracoAspect: DracoAspect = DracoAspect (
      _superDomain,
      _modules,
      _extensible,
      _derivation,
      _elements,
      _factory,
      _globalElements,
      _source,
      _target
    )
    override val domainAspect: DomainAspect = DomainAspect (
      _elementTypeNames
    )
    override val ruleAspect: RuleAspect = RuleAspect (
      _variables,
      _conditions,
      _values,
      _pattern,
      _action
    )
    override val actorAspect: ActorAspect = ActorAspect (
      _messageAction,
      _signalAction
    )
  }

  // Encode a TypeDefinition — emits the aspect-block form, omitting empty aspects.
  lazy implicit val encoder: Encoder[TypeDefinition] = Encoder.instance { td =>
    val fields = Seq(
      Some("typeName" -> td.typeName.asJson),
      if (!DracoAspect.isEmpty(td.dracoAspect))   Some("dracoAspect"  -> td.dracoAspect.asJson)  else None,
      if (!DomainAspect.isEmpty(td.domainAspect)) Some("domainAspect" -> td.domainAspect.asJson) else None,
      if (!RuleAspect.isEmpty(td.ruleAspect))     Some("ruleAspect"   -> td.ruleAspect.asJson)   else None,
      if (!ActorAspect.isEmpty(td.actorAspect))   Some("actorAspect"  -> td.actorAspect.asJson)  else None
    ).flatten

    Json.obj(fields: _*)
  }

  // Decode a TypeDefinition — accepts both the new aspect-block shape and the
  // legacy flat-field shape. If an aspect block is present, it is used as-is;
  // otherwise the corresponding flat fields are gathered into a synthesized aspect.
  lazy implicit val decoder: Decoder[TypeDefinition] = Decoder.instance { cursor =>
    for {
      _typeName       <- cursor.downField("typeName").as[TypeName]
      _dracoOpt       <- cursor.downField("dracoAspect").as[Option[DracoAspect]]
      _domainOpt      <- cursor.downField("domainAspect").as[Option[DomainAspect]]
      _ruleOpt        <- cursor.downField("ruleAspect").as[Option[RuleAspect]]
      _actorOpt       <- cursor.downField("actorAspect").as[Option[ActorAspect]]
      // Legacy flat-field fallbacks
      _superDomain    <- cursor.downField("superDomain").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _modules        <- cursor.downField("modules").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _extensible     <- cursor.downField("extensible").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _derivation     <- cursor.downField("derivation").as[Option[Seq[TypeName]]].map(_.getOrElse(Seq.empty))
      _elements       <- cursor.downField("elements").as[Option[Seq[TypeElement]]].map(_.getOrElse(Seq.empty))
      _factory        <- cursor.downField("factory").as[Option[Factory]].map(_.getOrElse(Factory.Null))
      _globalElements <- cursor.downField("globalElements").as[Option[Seq[BodyElement]]].map(_.getOrElse(Seq.empty))
      _elementTypeNames <- cursor.downField("elementTypeNames").as[Option[Seq[String]]].map(_.getOrElse(Seq.empty))
      _source         <- cursor.downField("source").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _target         <- cursor.downField("target").as[Option[TypeName]].map(_.getOrElse(TypeName.Null))
      _variables      <- cursor.downField("variables").as[Option[Seq[Variable]]].map(_.getOrElse(Seq.empty))
      _conditions     <- cursor.downField("conditions").as[Option[Seq[Condition]]].map(_.getOrElse(Seq.empty))
      _values         <- cursor.downField("values").as[Option[Seq[Value]]].map(_.getOrElse(Seq.empty))
      _pattern        <- cursor.downField("pattern").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
      _action         <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
      _messageAction  <- cursor.downField("messageAction").as[Option[Action]].map(_.getOrElse(Action.Null))
      _signalAction   <- cursor.downField("signalAction").as[Option[Action]].map(_.getOrElse(Action.Null))
    } yield {
      val dracoAspect = _dracoOpt.getOrElse (DracoAspect (
        _superDomain    = _superDomain,
        _modules        = _modules,
        _extensible     = _extensible,
        _derivation     = _derivation,
        _elements       = _elements,
        _factory        = _factory,
        _globalElements = _globalElements,
        _source         = _source,
        _target         = _target
      ))
      val domainAspect = _domainOpt.getOrElse (DomainAspect (_elementTypeNames))
      val ruleAspect = _ruleOpt.getOrElse (RuleAspect (
        _variables  = _variables,
        _conditions = _conditions,
        _values     = _values,
        _pattern    = _pattern,
        _action     = _action
      ))
      val actorAspect = _actorOpt.getOrElse (ActorAspect (
        _messageAction = _messageAction,
        _signalAction  = _signalAction
      ))
      apply (
        _typeName,
        dracoAspect.superDomain,
        dracoAspect.modules,
        dracoAspect.extensible,
        dracoAspect.derivation,
        dracoAspect.elements,
        dracoAspect.factory,
        dracoAspect.globalElements,
        domainAspect.elementTypeNames,
        dracoAspect.source,
        dracoAspect.target,
        ruleAspect.variables,
        ruleAspect.conditions,
        ruleAspect.values,
        ruleAspect.pattern,
        ruleAspect.action,
        actorAspect.messageAction,
        actorAspect.signalAction
      )
    }
  }

  lazy val Null: TypeDefinition = TypeDefinition (TypeName.Null)
}
