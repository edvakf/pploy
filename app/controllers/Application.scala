package controllers

import java.io.File
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.iteratee.Iteratee
import play.api.mvc._
import models._

import scala.io.Source
import scala.sys.process.Process

object Application extends Controller {
  def getCurrentUser(request: RequestHeader) = {
    request.cookies.get("user").map {
      cookie => User(cookie.value)
    }
  }

  val createProjectForm = Form("url" -> text)

  def index() = Action { implicit request =>
    Ok(views.html.index())
  }

  def create() = Action { implicit request =>
    val url = createProjectForm.bindFromRequest.get
    val proj = Project(Repo.clone(url))
    Redirect(routes.Application.project(proj.name))
  }

  def project(project: String) = Action { implicit request =>
    val proj = Project(project)
    val user = getCurrentUser(request)
    val useWebSocket = current.configuration.getBoolean("pploy.preference.websocket").getOrElse(false)

    Ok(views.html.project(proj, user, useWebSocket))
  }

  def lockUser(project: String) = Action { implicit request =>
    val proj = Project(project)
    if (proj.isLocked) Ok(proj.lock.user.name)
    else Ok("")
  }

  val lockOperationForm = Form(tuple("user" -> nonEmptyText, "operation" -> text))

  def lock(project: String) = Action { implicit request =>
    val proj = Project(project)

    lockOperationForm.bindFromRequest.fold(
      errors => Redirect(routes.Application.project(proj.name)),
      f => {
        val (userName, operation) = f
        val user = User(userName)
        operation match {
          case "gain" => proj.gainLock(user)
          case "release" => proj.releaseLock(user)
          case "extend" => proj.extendLock(user)
          case _ =>
            throw new RuntimeException("operation not specified")
        }
        Redirect(routes.Application.project(proj.name))
          .withCookies(Cookie("user", userName, Some(3600 * 24 * 7)))
      }
    )
  }

  val checkoutForm = Form("ref" -> nonEmptyText)

  def checkout(project: String) = Action { implicit request =>
    val proj = Project(project)
    val user = getCurrentUser(request)
      .getOrElse(throw new RuntimeException("user not selected"))
    proj.assertLockedByUser(user)

    checkoutForm.bindFromRequest.fold(
      errors => BadRequest,
      ref => {
        Ok.chunked(ProcessEnumerator(proj.checkoutProcess(ref)))
          .withHeaders("Content-Type" -> "text/plain; charset=utf-8", "X-Content-Type-Options" -> "nosniff")
      }
    )
  }

  def checkoutWS(project: String, ref: String) = WebSocket.using[String] { request =>
    val proj = Project(project)
    val user = getCurrentUser(request)
      .getOrElse(throw new RuntimeException("user not selected"))

    val in = Iteratee.ignore[String]
    val out = ProcessEnumerator(proj.checkoutProcess(ref))
    (in, out)
  }

  val deployForm = Form("target" -> text)

  def deploy(project: String) = Action { implicit request =>
    val proj = Project(project)
    val user = getCurrentUser(request)
      .getOrElse(throw new RuntimeException("user not selected"))
    proj.assertLockedByUser(user)

    deployForm.bindFromRequest.fold(
      errors => BadRequest,
      target => {
        Ok.chunked(ProcessEnumerator(proj.deployProcess(user, target)))
          .withHeaders("Content-Type" -> "text/plain; charset=utf-8", "X-Content-Type-Options" -> "nosniff")
      }
    )
  }

  def deployWS(project: String, target: String) = WebSocket.using[String] { request =>
    val proj = Project(project)
    val user = getCurrentUser(request)
      .getOrElse(throw new RuntimeException("user not selected"))

    val in = Iteratee.ignore[String]
    val out = ProcessEnumerator(proj.deployProcess(user, target))
    (in, out)
  }

  def commits(project: String) = Action { implicit request =>
    val proj = Project(project)
    Ok(views.html.commits(proj.repo.commits))
  }

  def logs(project: String, full: Long) = Action { implicit request =>
    val proj = Project(project)
    val file = new File(WorkingDir.logFile(proj.name).toString)
    if (file.isFile) {
      if (full != 1) {
        val limit = current.configuration.getInt("pploy.commitlogs.lines").getOrElse(1000)

        val lines = Source.fromFile(file.getCanonicalPath).getLines().take(limit).toList
        if (lines.length >= limit) {
          Ok(lines.mkString("\n") + "\n*** LOG FILE TOO LONG. PLEASE SEE THE FULL LOG. ***")
            .withHeaders("Content-Type" -> "text/plain; charset=utf-8", "X-Content-Type-Options" -> "nosniff")
        } else {
          Ok(lines.mkString("\n"))
            .withHeaders("Content-Type" -> "text/plain; charset=utf-8", "X-Content-Type-Options" -> "nosniff")
        }
      } else {
        Ok.sendFile(content = file, inline = true)
          .withHeaders("Content-Type" -> "text/plain; charset=utf-8", "X-Content-Type-Options" -> "nosniff")
      }
    } else {
      NotFound
    }
  }

  def remove(project: String) = Action { implicit request =>
    Project(project).remove()
    Redirect(routes.Application.index())
  }
}