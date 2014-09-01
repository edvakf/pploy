package models

import play.api.Play._
import play.api.i18n.{Lang, Messages}

object Hook {
  val idobata = current.configuration.getString("pploy.idobata.endpoint").map {
    url => IdobataGenericHook(url)
  }
  implicit val lang = Lang.availables.head

  def lockGained(projectName: String, userName: String) = {
    idobata.map { _.postHTML(Messages("hook.lock.gained", projectName, userName)) }
  }

  def lockReleased(projectName: String, userName: String) = {
    idobata.map { _.postHTML(Messages("hook.lock.released", projectName, userName)) }
  }

  def lockExtended(projectName: String, userName: String) = {
    idobata.map { _.postHTML(Messages("hook.lock.extended", projectName, userName)) }
  }

  def deployed(projectName: String, userName: String) = {
    idobata.map { _.postHTML(Messages("hook.deployed", projectName, userName)) }
  }

}
