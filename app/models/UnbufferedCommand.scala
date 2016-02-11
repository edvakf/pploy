package models

import scala.sys.process._

object UnbufferedCommand {
  // libc does not line buffer if output is not a terminal, instead use full buffering.
  // `script` runs a given command in a pseudo terminal.
  // it also redirects stderr to stdout which fits our usage

  // see: http://unix.stackexchange.com/questions/25372/turn-off-buffering-in-pipe

  private lazy val hasStdbufCommand = {
    0 == Process("which stdbuf").!
  }

  def apply(command: String): Seq[String] = {
    if (hasStdbufCommand) {
      Seq("bash", "-c", "stdbuf -oL -eL " + command + " 2>&1") // Linux
    } else {
      Seq("script", "-q", "/dev/null", "bash", "-c", command) // Mac OS X
    }
  }
}
