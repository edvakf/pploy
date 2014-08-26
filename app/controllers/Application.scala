package controllers

import play.api._
import play.api.i18n._
import play.api.mvc._
import play.api.Play.current
import models._
import playcli.CLI

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

  def create() = Action { request =>
    getSinglePostParam(request, "url") match {
      case None =>
        throw new RuntimeException("url not given")
      case Some(url) =>
        try {
          val proj = Project(Repo.clone(url))
          Redirect("/" + proj.name)
        } catch { case e: Exception =>
          Redirect("/").flashing("message" -> e.getMessage)
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

        } catch { case e: LockStatusException =>
          result.flashing("message" -> e.getMessage)
        }
    }
  }

  def checkout(project: String) = Action { implicit request =>
    val proj = Project(project)
    val userOption = getCurrentUser(request)
    if (userOption.isEmpty) throw new RuntimeException("user not selected")
    val user = userOption.get

    if (!proj.isLockedBy(user)) throw new LockStatusException(proj, user)

    val refOption = getSinglePostParam(request, "ref")
    if (refOption.isEmpty) throw new RuntimeException("ref is empty")

    try {
      proj.checkout(refOption.get)
      Redirect("/" + proj.name)
    } catch { case e: Exception =>
      Redirect("/" + proj.name).flashing("message" -> e.getMessage)
    }
  }

  def commits(project: String) = Action { implicit request =>
    val proj = Project(project)
    Ok(views.html.commits(proj.repo.commits))
  }

  def deploy(project: String) = Action { implicit request =>
    val proj = Project(project)
    val userOption = getCurrentUser(request)
    if (userOption.isEmpty) throw new RuntimeException("user not selected")
    val user = userOption.get

    if (!proj.isLockedBy(user)) throw new LockStatusException(proj, user)

    val targetOption = getSinglePostParam(request, "target")
    if (targetOption.isEmpty) throw new RuntimeException("target is empty")

    Ok.chunked(CLI.enumerate(new Deploy(proj.repo).execute(targetOption.get, user)))
      .withHeaders("Content-Type" -> "text/plain")
  }

  def logs(project: String) = Action { implicit request =>
    val proj = Project(project)
    val file = new Deploy(proj.repo).logfile
    if (file.isFile) {
      Ok.sendFile(content = file, inline = true)
    } else {
      Ok("")
    }
  }

}