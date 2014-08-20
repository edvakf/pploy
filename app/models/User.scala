package models

import play.api.Logger
import play.api.Play.current

case class User(name: String) {
  if (!User.allNames.contains(name)) {
    throw new IllegalArgumentException("bad user name")
  }
}

object User {
  val allNames = current.configuration
    .getStringSeq("pploy.users")
    .get
    .asInstanceOf[Seq[String]] // IntelliJ thinks getStringSeq returns Option[Seq[Any]]
}