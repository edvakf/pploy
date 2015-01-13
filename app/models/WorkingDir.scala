package models

import java.io.File

import play.api.Logger
import play.api.Play.current
import scala.collection.JavaConversions._

object WorkingDir {

  val dirname = current.configuration.getString("pploy.dir").getOrElse(sys.error("pploy.dir is not set"))

  val projectsDir = new File(dirname, "projects")

  val logsDir = new File(dirname, "logs")

  def setup(): Unit = {
    mkdir(new File(dirname))
    mkdir(projectsDir)
    mkdir(logsDir)
  }

  def projectDir(project: String) = new File(projectsDir, project)

  def logFile(project: String) = new File(logsDir, project + ".log")

  private def mkdir(dir: File): Unit = {
    if (!dir.exists()) {
      Logger.info("making directory: " + dir.toString)
      dir.mkdirs()
    } else if (!dir.isDirectory) {
      throw new RuntimeException("working directory is taken!")
    }
  }

  def projects = {
    // FIXME: handle irregular files in projects dir
    projectsDir.listFiles().map { _.getName }.sorted
  }

  def removeProjectFiles(project: String) = {
    delete(projectDir(project))
    delete(logFile(project))
  }

  private def delete(f: File): Unit = {
    if (f.exists) {
      if (f.isDirectory) {
        f.listFiles.map(delete)
      }
      if (!f.delete())
        throw new RuntimeException("Failed to delete file: " + f)
    }
  }
}
