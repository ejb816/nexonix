package draco.dreams.user

import draco.dreams._
import draco._

trait Response extends DracoType {
  val problems: Seq[draco.Problem]
  def accepted: Boolean = problems.isEmpty
}

object Response extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Response", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[Response] = Type[Response] (typeDefinition)
  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)
}
