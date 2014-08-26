import play.api._
import play.api.Play.current
import play.api._
import play.api.mvc._
import play.filters.csrf._

import models.{Project, WorkingDir}

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application) {
    current.configuration.getString("pploy.dir") match {
      case None => throw new RuntimeException("pploy.dir is not set")
      case Some(dir) => WorkingDir.setup(dir)
    }
  }

}
