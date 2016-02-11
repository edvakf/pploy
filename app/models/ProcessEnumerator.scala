package models

import play.api.libs.iteratee.Enumerator
import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global

object ProcessEnumerator {
  // wraps a ProcessBuilder with Play's Enumerator
  // and executes the process in Future
  // so that the process' output can be streamed
  def apply(process: ProcessBuilder): Enumerator[String] = {
    Enumerator.enumerate[String](
      process.lineStream_!(ProcessLogger(line => ())).map { line => line + "\n" }
    )
  }
}
