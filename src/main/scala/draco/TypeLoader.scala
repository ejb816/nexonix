package draco

import draco.drake.Absent
import draco.drake.Presence
import draco.generator.carrier.DefinitionPath

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps
import java.net.URL

trait TypeLoader extends DracoType

object TypeLoader extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeLoader", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeLoader] = Type[TypeLoader] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def readDefinition(url: URL): Presence[TypeDefinition] = ({ val s = scala.io.Source.fromURL(url); try io.circe.parser.parse(s.mkString).flatMap(_.as[TypeDefinition]).toOption finally s.close() } match { case Some(v) => draco.drake.Present(v); case None => Absent() })
  def loadFromResource(_resourcePath: => String): Presence[TypeDefinition] = {
    lazy val resourcePath: String = _resourcePath
    DefinitionPath.default.source(resourcePath).fold(Absent(), readDefinition)
  }
  def tryLoad(_typeName: => TypeName): Presence[TypeDefinition] = {
    lazy val typeName: TypeName = _typeName
    loadFromResource(typeName.resourcePath)
  }
  def rooted(td: TypeDefinition): TypeDefinition = (if (DracoAspect.parents(td.dracoAspect).nonEmpty || td.typeName.name == "DracoType" && td.typeName.namePackage == Seq("draco")) draco.drake.Present[Unit](()) else Absent[Unit]()).ifThenElse(td, TypeDefinition(_typeName = td.typeName, _uses = td.uses, _dracoAspect = DracoAspect(_typeParameters = td.dracoAspect.typeParameters, _superDomain = td.dracoAspect.superDomain, _modules = td.dracoAspect.modules, _extensible = td.dracoAspect.extensible, _derivation = td.dracoAspect.derivation :+ TypeName.write(TypeName("DracoType", _namePackage = Seq("draco"))), _elements = td.dracoAspect.elements, _factory = td.dracoAspect.factory, _globalElements = td.dracoAspect.globalElements), _domainAspect = td.domainAspect, _ruleAspect = td.ruleAspect, _actorAspect = td.actorAspect, _codecAspect = td.codecAspect))
  def loadType(_typeName: => TypeName): TypeDefinition = {
    lazy val typeName: TypeName = _typeName
    tryLoad(typeName).fold(TypeDefinition(typeName), rooted)
  }
  def isStub(_typeName: => TypeName): Boolean = {
    lazy val typeName: TypeName = _typeName
    lazy val td: TypeDefinition = loadType(typeName)
    DracoAspect.isEmpty(td.dracoAspect) && DomainAspect.isEmpty(td.domainAspect) && td.ruleAspect.fold(true, aspect => false) && td.actorAspect.fold(true, aspect => false)
  }
}
