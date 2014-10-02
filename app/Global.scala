import java.net.URL

import play.api.Play.current
import play.api._
import play.api.mvc._
import play.filters.csrf._
import play.api.mvc.Results._
import scala.concurrent.Future
import org.apache.commons.lang3.exception.ExceptionUtils
import models._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  lazy val isDev = Play.isDev(current)

  override def onStart(app: Application) {
    current.configuration.getString("pploy.dir") match {
      case None => throw new RuntimeException("pploy.dir is not set")
      case Some(dir) => WorkingDir.setup(dir)
    }
  }

  private def backUrl(request: RequestHeader) = {
    request.headers.get("referer") match {
      case Some(url) => new URL(url).getFile
      case None => controllers.routes.Application.index().toString()
    }
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    if (isDev) super.onError(request, ex)
    else {
      Logger.info(ex.getMessage)
      Logger.info(ExceptionUtils.getStackTrace(ex))
      Future.successful(
        Redirect(backUrl(request))
          .flashing("message" -> ex.getMessage)
      )
    }
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    if (isDev) super.onHandlerNotFound(request)
    else {
      Future.successful(
        Redirect(backUrl(request))
      )
    }
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    if (isDev) super.onBadRequest(request, error)
    else {
      Future.successful(
        Redirect(backUrl(request))
          .flashing("message" -> error)
      )
    }
  }
}
