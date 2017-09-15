package models

import play.api.Play._
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{ Lang, Messages }
import play.api.libs.json._
import play.api.libs.ws.WS

object Hook {
  val idobata = current.configuration.getString("pploy.idobata.endpoint").map {
    url => new IdobataGenericHook(url)
  }
  val slack = current.configuration.getString("pploy.slack.endpoint").map {
    url => new SlackIncomingWebHook(url)
  }
  implicit val lang = Lang.availables.head

  def lockGained(projectName: String, userName: String) = {
    idobata.foreach { _.post(Messages("hook.lock.gained", projectName, userName)) }
    slack.foreach { _.post(Messages("hook.lock.gained", projectName, userName)) }
  }

  def lockReleased(projectName: String, userName: String) = {
    idobata.foreach { _.post(Messages("hook.lock.released", projectName, userName)) }
    slack.foreach { _.post(Messages("hook.lock.released", projectName, userName)) }
  }

  def lockExtended(projectName: String, userName: String) = {
    idobata.foreach { _.post(Messages("hook.lock.extended", projectName, userName)) }
    slack.foreach { _.post(Messages("hook.lock.extended", projectName, userName)) }
  }

  def deployed(projectName: String, userName: String, target: String) = {
    idobata.foreach { _.post(Messages("hook.deployed", projectName, userName, target)) }
    slack.foreach { _.post(Messages("hook.deployed", projectName, userName, target)) }
  }

}

class IdobataGenericHook(endpoint: String) {
  def post(html: String): Unit = {
    WS.url(endpoint).post(Map("format" -> Seq("html"), "source" -> Seq(html)))
  }
}

class SlackIncomingWebHook(endpoint: String) {
  def post(text: String): Unit = {
    val payload = Json.obj(
      "text" -> text
    )
    WS.url(endpoint).post(payload)
  }
}
