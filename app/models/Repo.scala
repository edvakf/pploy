package models

import java.io.File
import org.eclipse.jgit.api._
import org.eclipse.jgit.transport.URIish
import play.api.Logger
import scala.collection.JavaConversions._
import scala.sys.process.{ProcessLogger, Process}

case class Repo(name: String) {
  lazy val dir = WorkingDir.projectDir(name)
  lazy val git = Git.open(dir)

  def checkoutCommand: String = {
    // if checkout_overwrite script exists
    val file = new File(dir, ".deploy/bin/checkout_overwrite")

    if (file.isFile) {
      file.getCanonicalPath
    } else {
      Seq(
        "git fetch --prune",
        "git reset --hard $DEPLOY_COMMIT",
        "git clean -fdx",
        "git submodule sync",
        "git submodule init",
        "git submodule update --recursive"
      ).flatMap { c => Seq(c, "echo " + c) }.mkString(" && ")
    }
  }

  def commits = {
    val commits = git.log.setMaxCount(20).call()
    commits.map { new Commit(git, _) }
  }
}

object Repo {
  def clone(url: String): Repo = {
    val uriish = new URIish(url)
    Process(Seq("git", "clone", url), WorkingDir.projectsDir).!
    Repo(uriish.getHumanishName)
  }
}
