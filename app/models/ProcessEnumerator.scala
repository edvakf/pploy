package models

import java.io.{ BufferedReader, InputStreamReader, PipedOutputStream, PipedInputStream }
import java.nio.charset.StandardCharsets
import play.api.libs.iteratee.Enumerator
import scala.sys.process._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ProcessEnumerator {
  // wraps a ProcessBuilder with Play's Enumerator
  // and executes the process in Future
  // so that the process' output can be streamed
  def apply(process: ProcessBuilder): Enumerator[String] = {
    val in = new PipedInputStream()
    val out = new PipedOutputStream(in)
    Future(process.#>(out).run())

    val reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))

    // there is Enumerator.fromString which makes Enumerator[Array[Byte]]
    // but we prefer Enumerator[String] so that WebSocket uses TextFrame
    Enumerator.fromCallback1[String](_ => Future {
      reader.readLine match {
        case line: String => Some(line + "\n")
        case _ => None
      }
    }, { () => // oncomplete
      in.close()
    })
  }
}
