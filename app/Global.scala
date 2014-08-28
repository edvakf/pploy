import java.net.URL

import play.api._
import play.api.Play.current
import play.api._
import play.api.mvc._
import play.filters.csrf._
import play.api.mvc.Results._
import scala.concurrent.Future

import models._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application) {
    current.configuration.getString("pploy.dir") match {
      case None => throw new RuntimeException("pploy.dir is not set")
      case Some(dir) => WorkingDir.setup(dir)
    }
  }

  private def backUrl(request: RequestHeader) = {
    request.headers.get("referer") match {
      case Some(url) => new URL(url).getFile
      case None => "/"
    }
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(
      Redirect(backUrl(request))
        .flashing("message" -> ex.getMessage)
    )
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(
      Redirect(backUrl(request))
    )
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(
      Redirect(backUrl(request))
        .flashing("message" -> error)
    )
  }

}
