package models

import java.io.File
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api._
import org.eclipse.jgit.transport.URIish
import play.api.Logger

case class Repo(name: String) {
  lazy val dir = new File(new File(WorkingDir.projectsDir.toString), name)

  def checkout(ref: String): Unit = {
    val git = Git.open(dir)
    Logger.info("git fetch --prune")
    git.fetch().setRemoveDeletedRefs(true).call()

    Logger.info("git reset --hard " + ref)
    git.reset().setMode(ResetType.HARD).setRef(ref).call()

    Logger.info("git clean -fdx")
    git.clean().setCleanDirectories(true).call()

    Logger.info("git submodule sync")
    git.submoduleSync().call()

    Logger.info("git submodule init")
    git.submoduleInit().call()

    // --recursive is probably handled by default
    // http://www.codeaffine.com/2014/04/16/how-to-manage-git-submodules-with-jgit/
    Logger.info("git submodule update --recursive")
    git.submoduleUpdate().call()
  }
}

object Repo {
  def clone(url: String): Repo = {
    val name = new URIish(url).getHumanishName
    val repo = Repo(name)
    Logger.info(f"cloning $url to ${repo.dir.toString}")
    val git = Git.cloneRepository.setURI(url).setDirectory(repo.dir).call()
    repo
  }
}