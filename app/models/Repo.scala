package models

import java.io._
import org.eclipse.jgit.api._
import org.eclipse.jgit.transport.URIish
import play.api.Play._
import scala.collection.JavaConversions._
import scala.sys.process.Process

case class Repo(name: String) {
  lazy val dir = WorkingDir.projectDir(name)
  lazy val git = Git.open(dir)

  def readmeFile: Option[File] = {
    val file = new File(dir, ".deploy/config/readme.html")
    if (file.isFile) { Some(file) } else { None }
  }

  def deployEnvsFile: Option[File] = {
    val file = new File(dir, ".deploy/config/deploy_envs")
    if (file.isFile) { Some(file) } else { None }
  }

  val deployCommand = ".deploy/bin/deploy"

  def checkoutCommand = {
    val checkoutScript = ".deploy/bin/checkout_overwrite"
    // if checkout_overwrite script exists
    if (new File(dir, checkoutScript).isFile) {
      checkoutScript
    } else {
      "bash -x -c '" + Seq(
        "git fetch --prune",
        "git reset --hard $DEPLOY_COMMIT",
        "git clean -fdx",
        "git submodule sync",
        "git submodule init",
        "git submodule update --recursive"
      ).mkString(" && ") + "'"
    }
  }

  def commits = {
    val logs = Process(Seq("git", "log", "-n", Repo.commitLength.toString) ++ Commit.gitLogOption, dir).!!
    Commit.parse(logs)
  }
}

object Repo {
  val commitLength = current.configuration.getInt("pploy.commits.length").getOrElse(20)

  def clone(url: String): Repo = {
    val uriish = new URIish(url)
    Process(Seq("git", "clone", url, "--depth", Repo.commitLength.toString), WorkingDir.projectsDir).!
    Repo(uriish.getHumanishName)
  }
}
