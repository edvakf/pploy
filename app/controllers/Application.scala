package controllers

import play.api.i18n._
import play.api.mvc._
import models.Project
import models.User
import play.api.Play.current

object Application extends Controller {
  def getCurrentUser(request: Request[AnyContent]) = {
    request.cookies.get("user").map {
      cookie => User(cookie.value)
    }
  }
  def getSinglePostParam(request: Request[AnyContent], param: String) = {
    request.body.asFormUrlEncoded
      .map{_.get(param)}.flatten
      .map{_.headOption}.flatten
  }

  def index(project: String) = Action { request =>
    val proj = Project(Option(project))
    val user = getCurrentUser(request)

    val lang = Lang.preferred(request.acceptLanguages)
    // FIXME: somehow lang is not passed as implicit
    Ok(views.html.index(proj, user)(lang))
  }

  def lock(project: String) = Action { request =>
    val proj = Project(project)

    getSinglePostParam(request, "operation") match {
      case Some("gain") =>
        getSinglePostParam(request, "user").map {
          user => proj.gainLock(User(user))
        }
      case Some("release") =>
        getCurrentUser(request).map {proj.releaseLock}
      case Some("extend") =>
        getCurrentUser(request).map {proj.extendLock}
      case _ => throw new RuntimeException("operation not specified")
    }
    // don't fail if lock operation couldn't succeed (unless program error)
    Redirect("/"+proj.name)
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