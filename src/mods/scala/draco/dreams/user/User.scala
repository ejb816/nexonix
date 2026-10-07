package draco.dreams.user

import draco.dreams._
import draco._

trait User extends DracoType

object User extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("User", _namePackage = Seq ("draco", "dreams", "user")))
  lazy val dracoType: Type[User] = Type[User] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[User] = Domain[User] (typeDefinition)
}
