package models

import scala.sys.process._

object UnbufferedCommand {
  // libc does not line buffer if output is not a terminal, instead use full buffering.
  // `script` runs a given command in a pseudo terminal.
  // it also redirects stderr to stdout which fits our usage

  // see: http://unix.stackexchange.com/questions/25372/turn-off-buffering-in-pipe

  private lazy val hasLinuxScriptCommand = {
    var isLinux = false
    Process("script --version") ! ProcessLogger(
      line => if (line.indexOf("linux") >= 0) isLinux = true
    )
    isLinux
  }

  def apply(command: String): Seq[String] = {
    if (hasLinuxScriptCommand) {
      Seq("script", "-c", command, "-q", "/dev/null") // linux script
    } else {
      Seq("script", "-q", "/dev/null", command) // bsd script
    }
  }
}
