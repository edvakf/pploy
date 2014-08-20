package models

import org.joda.time.DateTime
import play.api.Play.current

case class Project(name: String) {
  lazy val gainMinutes = current.configuration.getInt("pploy.lock.gainMinutes").get
  lazy val extendMinutes = current.configuration.getInt("pploy.lock.extendMinutes").get

  private lazy val lock_ = Lock.fetch(this)
  def isLocked = lock_.isDefined

  // This method must be called after `isLocked`.
  // Plus, it's not updated during gainLock, extendLock or releaseLock, so
  // can't be trusted
  def lock = lock_.get

  def gainLock(user: User): Boolean = {
    lock_ match {
      case Some(_) =>
        false
      case None =>
        Lock.save(this, user, new DateTime().plusMinutes(gainMinutes))
        true
    }
  }

  def extendLock(user: User): Boolean = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        Lock.save(this, user, lock.endTime.plusMinutes(extendMinutes))
        true
      case Some(_) | None =>
        false
    }
  }

  def releaseLock(user: User): Boolean = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        lock.delete()
        true
      case Some(_) | None =>
        false
    }
  }
}

object Project {
  val allNames = current.configuration
    .getStringSeq("pploy.projects")
    .get
    .asInstanceOf[Seq[String]] // IntelliJ thinks getStringSeq returns Option[Seq[Any]]

  def apply(name: Option[String]): Project = {
    name match {
      case None => Project(Project.allNames.head)
      case Some(x) if Project.allNames.contains(x) => Project(x)
      case _ => throw new IllegalArgumentException("bad project name")
    }
  }
}