package models

import java.io.File
import org.eclipse.jgit.api._
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.URIish
import play.api.Logger

case class Repo(name: String)

object Repo {
  def clone(url: String): Repo = {
    val dirname = new URIish(url).getHumanishName
    val dir = new File(new File(WorkingDir.projectsDir.toString), dirname)
    Logger.info(f"cloning $url to ${dir.toString}")
    val git = Git.cloneRepository.setURI(url).setDirectory(dir).call()
    Repo(dirname)
  }
}