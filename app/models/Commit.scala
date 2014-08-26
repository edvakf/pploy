package models

import java.io.ByteArrayOutputStream

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{DiffEntry, DiffFormatter}
import org.eclipse.jgit.revwalk.RevCommit
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConversions._

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
