import org.scalatest.{ FunSuite, Matchers }
import play.api.test.TestServer
import play.api.test.Helpers._

class IntegrationSpec extends FunSuite with Matchers {

  test("work from within a browser") {
    val port = 3333
    running(TestServer(port), HTMLUNIT) { browser =>
      browser.goTo("http://localhost:" + port)
      browser.$("h2").getTexts.get(0) should be("Welcome to pploy")
    }
  }

}
