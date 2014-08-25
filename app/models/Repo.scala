package models

import java.io.{ByteArrayOutputStream, File}
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api._
import org.eclipse.jgit.diff.{DiffEntry, DiffFormatter}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.URIish
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import scala.collection.JavaConversions._
import scala.sys.process._

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

  def commits() = {
    val git = Git.open(dir)
    val commits = git.log().setMaxCount(20).call()
    commits.map { c =>
      new Commit(git, c)
    }
  }

  def deploy(target: String, user: String): ProcessBuilder = {
    Logger.info("bash -c '.deploy/bin/deploy 2>&1'")
    Process(
      Seq("bash", "-c", ".deploy/bin/deploy 2>&1"),
      dir,
      "DEPLOY_ENV" -> target,
      "DEPLOY_USER" -> user
    )
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

class Commit(private val git: Git, private val c: RevCommit) {

  lazy val hash = c.getId.name
  lazy val abbrev = c.getId.abbreviate(7).name
  lazy val datetime = new DateTime(c.getCommitTime.asInstanceOf[Long] * 1000).toString(DateTimeFormat.longDateTime())
  lazy val author = c.getAuthorIdent.getName
  lazy val subject = c.getShortMessage
  lazy val body = c.getFullMessage

  lazy val nameStatus = {
    // https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.pgm/src/org/eclipse/jgit/pgm/Log.java#L347-L354
    // https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.pgm/src/org/eclipse/jgit/pgm/Diff.java#L226-L251
    val a = if (c.getParentCount > 0) c.getParent(0).getTree else null
    val b = c.getTree

    if (a == null) {
      "JGit's bug: diff of the initial commit cannot be retrieved"
    } else {
      val df = new DiffFormatter(new ByteArrayOutputStream())
      df.setRepository(git.getRepository)
      df.scan(a, b).map { ent =>
        ent.getChangeType match {
          case DiffEntry.ChangeType.ADD    => "A\t" + ent.getNewPath
          case DiffEntry.ChangeType.DELETE => "D\t" + ent.getOldPath
          case DiffEntry.ChangeType.MODIFY => "M\t" + ent.getNewPath
          case DiffEntry.ChangeType.COPY   => f"C${ent.getScore}%03d\t${ent.getOldPath}\t${ent.getNewPath}"
          case DiffEntry.ChangeType.RENAME => f"R${ent.getScore}%03d\t${ent.getOldPath}\t${ent.getNewPath}"
        }}
        .mkString("\n")
    }
  }

  lazy val otherRefs = {
    // https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.pgm/src/org/eclipse/jgit/pgm/Log.java#L245-L254
    git.getRepository.getAllRefsByPeeledObjectId.get(c) match {
      case null => Seq()
      case list => list.map { ref => ref.getName }.toSeq
    }
  }
}
