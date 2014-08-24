package models

import org.joda.time.DateTime
import play.api.Play.current
import play.api.i18n.{Messages, Lang}

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

  def gainLock(user: User) = {
    lock_ match {
      case None =>
        Lock.save(this, user, new DateTime().plusMinutes(Project.gainMinutes))
      case otherwise =>
        throw new LockOperationException(otherwise)
    }
  }

  def extendLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        Lock.save(this, user, lock.endTime.plusMinutes(Project.extendMinutes))
      case otherwise =>
        throw new LockOperationException(otherwise)
    }
  }

  def releaseLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        lock.delete()
      case otherwise =>
        throw new LockOperationException(otherwise)
    }
  }
}

object Project {
  def allNames = WorkingDir.projects.toSeq

  lazy val gainMinutes = current.configuration.getInt("pploy.lock.gainMinutes").get
  lazy val extendMinutes = current.configuration.getInt("pploy.lock.extendMinutes").get
}

class LockOperationException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause) {

  def this(lock: Option[Lock]) = this(lock match {
    case None => "lock.operation.expired"
    case Some(_) => "lock.operation.taken"
  })

  def getMessage(implicit lang: Lang) = Messages(super.getMessage)(lang)
}
