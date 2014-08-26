package models

import java.io.File
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api._
import org.eclipse.jgit.transport.URIish
import play.api.Logger
import scala.collection.JavaConversions._

case class Repo(name: String) {
  lazy val dir = new File(WorkingDir.projectDir(name).toString)
  lazy val git = Git.open(dir)

  def checkout(ref: String): Unit = {
    Logger.info("git fetch --prune")
    git.fetch.setRemoveDeletedRefs(true).call()

    Logger.info("git reset --hard " + ref)
    git.reset.setMode(ResetType.HARD).setRef(ref).call()

    Logger.info("git clean -fdx")
    git.clean.setCleanDirectories(true).call()

    Logger.info("git submodule sync")
    git.submoduleSync.call()

    Logger.info("git submodule init")
    git.submoduleInit.call()

    // --recursive is probably handled by default
    // http://www.codeaffine.com/2014/04/16/how-to-manage-git-submodules-with-jgit/
    Logger.info("git submodule update --recursive")
    git.submoduleUpdate.call()
  }

  def commits = {
    val commits = git.log.setMaxCount(20).call()
    commits.map { new Commit(git, _) }
  }
}

object Repo {
  def clone(url: String): Repo = {
    val uriish = new URIish(url)
    val repo = Repo(uriish.getHumanishName)
    Logger.info(f"cloning $url to ${repo.dir.toString}")
    Git.cloneRepository.setURI(url).setDirectory(repo.dir).call()
    repo
  }
}
