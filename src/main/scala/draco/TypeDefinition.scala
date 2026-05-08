package draco

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

sealed trait TypeDefinition extends Aspects {
  val typeName: TypeName

  // Convenience accessors — read from the corresponding aspect
  def superDomain: TypeName = dracoAspect.superDomain
  def modules: Seq[TypeName] = dracoAspect.modules
  def extensible: TypeName = dracoAspect.extensible
  def derivation: Seq[TypeName] = dracoAspect.derivation
  def elements: Seq[TypeElement] = dracoAspect.elements
  def factory: Factory = dracoAspect.factory
  def globalElements: Seq[BodyElement] = dracoAspect.globalElements
  def source: TypeName = dracoAspect.source
  def target: TypeName = dracoAspect.target
  def elementTypeNames: Seq[String] = domainAspect.elementTypeNames
  def variables: Seq[Variable] = ruleAspect.variables
  def conditions: Seq[Condition] = ruleAspect.conditions
  def values: Seq[Value] = ruleAspect.values
  def pattern: Pattern = ruleAspect.pattern
  def action: Action = ruleAspect.action
  def messageAction: Action = actorAspect.messageAction
  def signalAction: Action = actorAspect.signalAction
}

object TypeDefinition extends App {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "TypeDefinition",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Fixed ("typeName", "TypeName"),
      Fixed ("dracoAspect",  "DracoAspect"),
      Fixed ("domainAspect", "DomainAspect"),
      Fixed ("ruleAspect",   "RuleAspect"),
      Fixed ("actorAspect",  "ActorAspect")
    ),
    _factory = Factory (
      "TypeDefinition",
      _parameters = Seq (
        Parameter ("typeName",     "TypeName",     ""),
        Parameter ("dracoAspect",  "DracoAspect",  "DracoAspect.Null"),
        Parameter ("domainAspect", "DomainAspect", "DomainAspect.Null"),
        Parameter ("ruleAspect",   "RuleAspect",   "RuleAspect.Null"),
        Parameter ("actorAspect",  "ActorAspect",  "ActorAspect.Null")
      )
    )
  )
  lazy val dracoType: Type[TypeDefinition] = Type[TypeDefinition] (typeDefinition)

  /** Aspect-based factory — canonical going forward.
    * Aspect overrides are `lazy val` so aspect construction is deferred until
    * first access. This avoids initialization cycles when bootstrap types like
    * `Pattern` use `TypeDefinition.apply` inside their own `typeDefinition`
    * lazy-val computation. */
  def fromAspects (
                    _typeName: TypeName,
                    _dracoAspect:  => DracoAspect  = DracoAspect.Null,
                    _domainAspect: => DomainAspect = DomainAspect.Null,
                    _ruleAspect:   => RuleAspect   = RuleAspect.Null,
                    _actorAspect:  => ActorAspect  = ActorAspect.Null
                  ) : TypeDefinition = new TypeDefinition {
    override val typeName: TypeName = _typeName
    override lazy val dracoAspect:  DracoAspect  = { val d = _dracoAspect;  if (d != null) d else DracoAspect.Null }
    override lazy val domainAspect: DomainAspect = { val d = _domainAspect; if (d != null) d else DomainAspect.Null }
    override lazy val ruleAspect:   RuleAspect   = { val r = _ruleAspect;   if (r != null) r else RuleAspect.Null }
    override lazy val actorAspect:  ActorAspect  = { val a = _actorAspect;  if (a != null) a else ActorAspect.Null }
  }

  /** Legacy flat-args factory — bundles flat fields into aspect blocks internally.
    * Preserved for backward compatibility with existing callers; new code should
    * prefer `fromAspects`. Aspect construction is deferred via by-name params and
    * lazy aspect overrides; null-valued field defaults are passed through to the
    * aspect, whose own `lazy val` accessors promote them to the appropriate Null
    * sentinel on first access. */
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
            ) : TypeDefinition = fromAspects (
    _typeName = _typeName,
    _dracoAspect = DracoAspect (
      _superDomain    = _superDomain,
      _modules        = _modules,
      _extensible     = _extensible,
      _derivation     = _derivation,
      _elements       = _elements,
      _factory        = _factory,
      _globalElements = _globalElements,
      _source         = _source,
      _target         = _target
    ),
    _domainAspect = DomainAspect (_elementTypeNames),
    _ruleAspect = RuleAspect (
      _variables  = _variables,
      _conditions = _conditions,
      _values     = _values,
      _pattern    = _pattern,
      _action     = _action
    ),
    _actorAspect = ActorAspect (
      _messageAction = _messageAction,
      _signalAction  = _signalAction
    )
  )

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
      fromAspects (_typeName, dracoAspect, domainAspect, ruleAspect, actorAspect)
    }
  }

  lazy val Null: TypeDefinition = TypeDefinition (TypeName.Null)
}
