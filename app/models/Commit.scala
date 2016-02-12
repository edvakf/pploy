package models

import org.joda.time.DateTime
import org.joda.time.format.{ ISODateTimeFormat, DateTimeFormat }

case class Commit(hash: String, dateTime: DateTime, author: String, otherRefs: Seq[String], subject: String, body: String, nameStatus: String) {
  def dateTimeString = dateTime.toString(DateTimeFormat.longDateTime())
}

object Commit {
  private val delim1 = "1PPLOY1YOLPP1"
  private val delim2 = "2PPLOY2YOLPP2"

  val gitLogOption = Seq(
    "--decorate=full", // prefix refs with refs/heads/, refs/remotes/origin/ and so on
    "--name-status", // show list of file diffs
    "-m", // show file diffs for merge commit
    "--first-parent", // -m shows file diffs from each parent. --first-parent make it from the first parent
    s"--pretty=format:${delim1}%H${delim2}%ci${delim2}%an${delim2}%d${delim2}%s${delim2}%b${delim2}"
  )

  def parse(logs: String): Seq[Commit] = {
    logs.split(delim1).drop(1).map { log =>
      log.split(delim2).toSeq match {
        case Seq(hash, isoLikeDate, author, refs, subject, body, nameStatus) =>
          val re = """^ \((.*?)\)$""".r // git 1.7 doesn't have `%D` format, which is the same as `%d` without the parens
          val otherRefs: Seq[String] = refs match {
            case re(foo) =>
              foo.split(", ")
                .flatMap(_.split(" -> ")) // master becomes "HEAD -> refs/head/master"
                .filterNot(_ == "") // filter out the case when refs is an empty string
            case _ =>
              Seq()
          }

          Commit(
            hash,
            // newer version of git has `%cI` format which can be parsed with `ISODateTimeFormat.dateTimeNoMillis`, but git 1.7 doesn't...
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z").parseDateTime(isoLikeDate),
            author,
            otherRefs,
            subject,
            body.trim,
            nameStatus.trim
          )
        case _ =>
          throw new RuntimeException(s"failed to parse commit log: ${log}")
      }
    }
  }
}