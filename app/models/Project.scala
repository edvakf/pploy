package models

import org.joda.time.DateTime
import play.api.Play.current

case class Project(name: String) {
  if (!Project.allNames.contains(name)) {
    throw new IllegalArgumentException("bad project name")
  }

  private lazy val lock_ = Lock.fetch(this) match {
    case Some(l) if l.secondsLeft <= 0 =>
      l.delete()
      None
    case other => identity(other)
  }
  def isLocked = lock_.isDefined

  // This method must be called after `isLocked`.
  // Plus, it's not updated during gainLock, extendLock or releaseLock
  def lock = lock_.get

  def isLockedBy(user: User): Boolean = {
    isLocked && lock.user == user
  }
  def isLockedBy(user: Option[User]): Boolean = {
    user.isDefined && isLockedBy(user.get)
  }

  def gainLock(user: User): Boolean = {
    lock_ match {
      case Some(_) =>
        false
      case None =>
        Lock.save(this, user, new DateTime().plusMinutes(Project.gainMinutes))
        true
    }
  }

  def extendLock(user: User): Boolean = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        Lock.save(this, user, lock.endTime.plusMinutes(Project.extendMinutes))
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

  lazy val gainMinutes = current.configuration.getInt("pploy.lock.gainMinutes").get
  lazy val extendMinutes = current.configuration.getInt("pploy.lock.extendMinutes").get
}