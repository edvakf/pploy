import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec {

  "Application" must {
    "should render the index page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get
        status(home) must be(OK)
        contentType(home) must be(Some("text/html"))
        contentAsString(home) must include("Welcome to pploy")
      }
    }
  }

}
