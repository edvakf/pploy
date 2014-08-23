import play.api._
import play.api.Play.current

import models.{Project, WorkingDir}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    current.configuration.getString("pploy.dir") match {
      case None => throw new RuntimeException("pploy.dir is not set")
      case Some(dir) => WorkingDir.setup(dir)
    }
  }

}
