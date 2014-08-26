package models

import org.joda.time.DateTime
import play.api.Play.current
import play.api.i18n.{Messages, Lang}

case class Project(name: String) {
  if (!Project.allNames.contains(name)) {
    throw new IllegalArgumentException("bad project name")
  }

  lazy val repo = Repo(name)

  private lazy val lock_ = Lock.fetch(this) match {
    case Some(l) if l.secondsLeft <= 0 =>
      l.delete()
      None
    case other => other
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
      case _ =>
        throw new LockStatusException(this, user)
    }
  }

  def extendLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        Lock.save(this, user, lock.endTime.plusMinutes(Project.extendMinutes))
      case _ =>
        throw new LockStatusException(this, user)
    }
  }

  def releaseLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        lock.delete()
      case _ =>
        throw new LockStatusException(this, user)
    }
  }

  def checkout(ref: String) = repo.checkout(ref)
}

object Project {
  def allNames = WorkingDir.projects.toSeq

  lazy val gainMinutes = current.configuration.getInt("pploy.lock.gainMinutes").get
  lazy val extendMinutes = current.configuration.getInt("pploy.lock.extendMinutes").get

  def apply(repo: Repo): Project = Project(repo.name)
}

class LockStatusException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause) {

  def this(project: Project, user: User) = this(
    if (project.isLocked) "lock.operation.taken" else "lock.operation.expired"
  )

  def getMessage(implicit lang: Lang) = Messages(super.getMessage)(lang)
}
