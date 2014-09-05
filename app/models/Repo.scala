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

  private def gitProc(args: String*) = {
    Logger.info("git " + args.mkString(" "))
    Process("git" +: args, dir)
  }

  def checkout(ref: String): Unit = {
    var proc = gitProc("fetch", "--prune") #&&
      gitProc("reset", "--hard", ref) #&&
      gitProc("clean", "-fdx") #&&
      gitProc("submodule", "sync") #&&
      gitProc("submodule", "init") #&&
      gitProc("submodule", "update", "--recursive")

    // チェックアウトフックスクリプトがあれば実行する
    val file = new File(dir, ".deploy/bin/hook_checkout")
    if (file.isFile) {
      Logger.info(".deploy/bin/hook_checkout 2>&1")
      proc = proc #&& Process(Seq("bash", "-c", file.getName + " 2>&1"), dir)
    }

    val result = proc.run(ProcessLogger(
      line => { Logger.info("stdout: " + line) },
      line => { Logger.info("stderr: " + line) }
    ))

    if (result.exitValue() != 0) throw new RuntimeException("checkout failed")
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
