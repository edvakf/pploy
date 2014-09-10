package models

import java.io.File
import java.nio.charset.CodingErrorAction

import org.joda.time.DateTime
import play.api.Logger
import play.api.Play.current
import play.api.i18n.{Messages, Lang}
import scala.io.{Codec, Source}
import scala.sys.process._

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
  def assertLockedByUser(user: User) = {
    if (!isLockedBy(user)) new LockStatusException(this, user)
  }

  def gainLock(user: User) = {
    lock_ match {
      case None =>
        Lock.save(this, user, new DateTime().plusMinutes(Project.gainMinutes))
      case _ =>
        throw new LockStatusException(this, user)
    }
    Hook.lockGained(name, user.name)
  }

  def extendLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        Lock.save(this, user, lock.endTime.plusMinutes(Project.extendMinutes))
      case _ =>
        throw new LockStatusException(this, user)
    }
    Hook.lockExtended(name, user.name)
  }

  def releaseLock(user: User) = {
    lock_ match {
      case Some(lock) if lock.user == user =>
        lock.delete()
      case _ =>
        throw new LockStatusException(this, user)
    }
    Hook.lockReleased(name, user.name)
  }

  def checkout(ref: String) = {
    Logger.info(repo.checkoutCommand)
    Process(
      Seq("bash", "-c", repo.checkoutCommand + " 2>&1"),
      repo.dir,
      "DEPLOY_COMMIT" -> ref
    )
  }

  def execDeploy(user: User, target: String): ProcessBuilder = {
    Logger.info("bash -c '.deploy/bin/deploy 2>&1'")
    if (target == "production") Hook.deployed(name, user.name)

    Process(
      Seq("bash", "-c", ".deploy/bin/deploy 2>&1"),
      repo.dir,
      "DEPLOY_ENV" -> target,
      "DEPLOY_USER" -> user.name
    ) #| Process(Seq("tee", WorkingDir.logFile(name).toString))
  }

  def readme: Option[String] = {
    val file = new File(repo.dir, ".deploy/config/readme.html")
    if (!file.isFile) { None }
    else {
      val codec = Codec.UTF8
      codec.onMalformedInput(CodingErrorAction.IGNORE)

      val source = Source.fromFile(file)(codec)
      try Some(source.mkString)
      finally source.close()
    }
  }

  def remove() = {
    WorkingDir.removeProjectFiles(this.name)
  }
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
