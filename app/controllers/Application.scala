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

  def index() = Action { request =>
    val lang = Lang.preferred(request.acceptLanguages)
    // FIXME: somehow lang is not passed as implicit
    Ok(views.html.index(None, None)(lang))
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

  def project(project: String) = Action { request =>
    val proj = Project(project)
    val user = getCurrentUser(request)

    val lang = Lang.preferred(request.acceptLanguages)
    // FIXME: somehow lang is not passed as implicit
    Ok(views.html.index(Some(proj), user)(lang))
  }

  def lock(project: String) = Action { request =>
    val proj = Project(project)

    getSinglePostParam(request, "user") match {
      case None =>
        Redirect("/"+proj.name)

      case Some(userName) =>
        val user = User(userName)

        getSinglePostParam(request, "operation") match {
          case Some("gain") => proj.gainLock(user)
          case Some("release") => proj.releaseLock(user)
          case Some("extend") => proj.extendLock(user)
          case _ =>
            throw new RuntimeException("operation not specified")
        }

        // don't fail if lock operation couldn't succeed (unless program error)
        Redirect("/"+proj.name).withCookies(
          Cookie("user", userName, Some(3600*24*7))
        )
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