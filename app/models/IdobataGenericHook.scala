package models

import play.api.libs.ws.WS
import play.api.Play._

case class IdobataGenericHook(endpoint: String) {
  def postHTML(html: String): Unit = {
    WS.url(endpoint).post(Map("format" -> Seq("html"), "source" -> Seq(html)))
  }
}
