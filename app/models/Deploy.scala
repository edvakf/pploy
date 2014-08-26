package models

import java.io.File

import scala.sys.process._
import play.api.Logger

class Deploy(val repo: Repo) {
  lazy val logfile = new File(new File(WorkingDir.logsDir.toString), repo.name + ".log")

  def execute(target: String, user: User): ProcessBuilder = {
    Logger.info("bash -c '.deploy/bin/deploy 2>&1'")
    Process(
      Seq("bash", "-c", ".deploy/bin/deploy 2>&1"),
      repo.dir,
      "DEPLOY_ENV" -> target,
      "DEPLOY_USER" -> user.name
    ) #| Process(Seq("tee", logfile.getAbsolutePath))
  }
}
