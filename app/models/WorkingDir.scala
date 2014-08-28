package models

import java.io.File

import play.api.Logger

import scala.collection.JavaConversions._

object WorkingDir {
  private var dirname_ : String = null

  def dirname = {
    if (dirname_ == null) throw new RuntimeException("setup is not called!")
    dirname_
  }

  lazy val projectsDir = new File(dirname_, "projects")
  lazy val logsDir = new File(dirname_, "logs")

  def projectDir(project: String) = new File(projectsDir, project)
  def logFile(project: String) = new File(logsDir, project + ".log")

  def setup(dirname: String) = {
    dirname_ = dirname
    mkdir(new File(dirname))
    mkdir(projectsDir)
    mkdir(logsDir)
  }

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
