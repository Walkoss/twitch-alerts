package router

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import poll.Poll
import testhelpers.BaseSpec

import scala.concurrent.Future

class PollRouterSpec extends BaseSpec {
  "PollRouter" should {

    "list polls" in {
      val request = FakeRequest(GET, "/polls").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val polls: Seq[Poll] = Json.fromJson[Seq[Poll]](contentAsJson(result)).get
      status(result) mustBe OK
      polls.length mustBe 2

      polls.head.id mustBe Some(1)
      polls.head.question mustBe "Should I play fortnite?"
      polls.head.firstChoice mustBe "yes"
      polls.head.secondChoice mustBe "no"

      polls.last.id mustBe Some(2)
      polls.last.question mustBe "CarlJR or Bren?"
      polls.last.firstChoice mustBe "CarlJR"
      polls.last.secondChoice mustBe "Bren"
    }

    "show poll by id" in {
      val request = FakeRequest(GET, "/polls/1").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val poll: Poll = Json.fromJson[Poll](contentAsJson(result)).get
      status(result) mustBe OK

      poll.id mustBe Some(1)
      poll.question mustBe "Should I play fortnite?"
      poll.firstChoice mustBe "yes"
      poll.secondChoice mustBe "no"
    }

    "return error when poll is not found (get)" in {
      val request = FakeRequest(GET, "/polls/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Poll 12 not found"
    }

    "create a poll" in {
      val payload = Json.obj("question" -> "Choose your game", "first_choice" -> "League of Legends", "second_choice" -> "Teamfight Tactics")
      val request = FakeRequest(POST, "/polls")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val poll: Poll = Json.fromJson[Poll](contentAsJson(result)).get
      status(result) mustBe CREATED

      poll.id mustBe defined
      poll.question mustBe "Choose your game"
      poll.firstChoice mustBe "League of Legends"
      poll.secondChoice mustBe "Teamfight Tactics"
    }

    "return an error when payload is not correct" in {
      val payload = Json.obj("incorrect_key" -> "Walkoss")
      val request = FakeRequest(POST, "/polls")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }

    "vote a poll" in {
      val payload = Json.obj("user_id" -> 1, "choice" -> "yes")
      val request = FakeRequest(POST, "/polls/1:vote")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe OK
      message mustBe "User 1 voted 'yes' to poll 1"
    }

    "return error when poll is not found (vote)" in {
      val payload = Json.obj("user_id" -> 1, "choice" -> "yes")
      val request = FakeRequest(POST, "/polls/12:vote")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Poll 12 not found"
    }

    "return error when user is not found (vote)" in {
      val payload = Json.obj("user_id" -> 12, "choice" -> "yes")
      val request = FakeRequest(POST, "/polls/1:vote")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "return error when user has already voted" in {
      val payload = Json.obj("user_id" -> 1, "choice" -> "yes")
      val request = FakeRequest(POST, "/polls/1:vote")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe CONFLICT
      message mustBe "User 1 already voted"
    }

    "return error when choice is invalid" in {
      val payload = Json.obj("user_id" -> 1, "choice" -> "unknown_choice")
      val request = FakeRequest(POST, "/polls/1:vote")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe BAD_REQUEST
      message mustBe "Choice 'unknown_choice' is not correct for poll 1"
    }
  }
}
