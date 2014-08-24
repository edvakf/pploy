package controllers

import play.api._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current
import models._

object Application extends Controller {
  def getCurrentUser(request: Request[AnyContent]) = {
    request.cookies.get("user").map {
      cookie => User(cookie.value)
    }
  }
  def getSinglePostParam(request: Request[AnyContent], param: String) = {
    request.body.asFormUrlEncoded
      .flatMap { _.get(param) }
      .flatMap { _.headOption }
  }

  def index() = Action { implicit request =>
    Ok(views.html.index(None, None))
  }

  def repo() = Action { request =>
    getSinglePostParam(request, "url") match {
      case None =>
        throw new RuntimeException("url not given")
      case Some(url) =>
        try {
          val repo = Repo.clone(url)
          Logger.info(f"created new project ${repo.name}")
          Redirect("/" + repo.name)
        } catch { case e: Exception =>
          Logger.debug(e.getMessage)
          Redirect("/").flashing(
            "message" -> e.getMessage
          )
        }
    }
  }

  def project(project: String) = Action { implicit request =>
    val proj = Project(project)
    val user = getCurrentUser(request)

    Ok(views.html.index(Some(proj), user))
  }

  def lock(project: String) = Action { implicit request =>
    val proj = Project(project)
    var result = Redirect("/"+proj.name)

    getSinglePostParam(request, "user") match {
      case None => result

      case Some(userName) =>
        val user = User(userName)
        result = result.withCookies(Cookie("user", userName, Some(3600*24*7)))

        try {
          getSinglePostParam(request, "operation") match {
            case Some("gain") => proj.gainLock(user)
            case Some("release") => proj.releaseLock(user)
            case Some("extend") => proj.extendLock(user)
            case _ =>
              throw new RuntimeException("operation not specified")
          }
          result

        } catch {
          case e: LockOperationException =>
            result.flashing("message" -> e.getMessage)
        }
    }
  }

  /*
  def unlock(project: String) = Action {
  }

  def checkout(project: String) = Action {
  }

  def deploy(project: String, target: String) = Action {
  }
  */

}