package models
import java.nio.file.{Files, Paths, Path}
import play.api.Logger

import scala.collection.JavaConversions._

object WorkingDir {
  private var dirname_ : String = null

  def dirname = {
    if (dirname_ == null) throw new RuntimeException("setup is not called!")
    dirname_
  }

  lazy val projectsDir = Paths.get(dirname_, "projects")

  def setup(dirname: String) = {
    dirname_ = dirname
    mkdir(Paths.get(dirname))
    mkdir(projectsDir)
  }

  private def mkdir(path: Path): Unit = {
    if (!Files.exists(path)) {
      Logger.info("making directory: " + path.toString)
      Files.createDirectory(path)
    } else if (!Files.isDirectory(path)) {
      throw new RuntimeException("working directory is taken!")
    }
  }

  def projects = {
    // FIXME: handle irregular files in projects dir
    Files.newDirectoryStream(projectsDir).map(_.getFileName.toString)
  }
}
