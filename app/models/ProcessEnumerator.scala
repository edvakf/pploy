package models

import java.io.{ PipedOutputStream, PipedInputStream }
import play.api.libs.iteratee.Enumerator
import scala.sys.process._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ProcessEnumerator {
  // wraps a ProcessBuilder with Play's Enumerator
  // and executes the process in Future
  // so that the process' output can be streamed
  def apply(process: ProcessBuilder): Enumerator[Array[Byte]] = {
    val in = new PipedInputStream()
    val out = new PipedOutputStream(in)
    Future(process.#>(out).run())
    Enumerator.fromStream(in)
  }
}
