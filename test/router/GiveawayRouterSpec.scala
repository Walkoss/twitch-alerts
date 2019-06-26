package router

import giveaway.Giveaway
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import testhelpers.BaseSpec
import user.User

import scala.concurrent.Future

class GiveawayRouterSpec extends BaseSpec {
  "GiveawayRouter" should {

    "list giveaways" in {
      val request = FakeRequest(GET, "/giveaways").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val giveaways: Seq[Giveaway] = Json.fromJson[Seq[Giveaway]](contentAsJson(result)).get
      status(result) mustBe OK
      giveaways.length mustBe 3

      giveaways.head.id mustBe Some(1)
      giveaways.head.name mustBe "Logitech G Pro"
      giveaways.head.isSubscribersOnly mustBe false
    }

    "show giveaway by id" in {
      val request = FakeRequest(GET, "/giveaways/1").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val giveaway: Giveaway = Json.fromJson[Giveaway](contentAsJson(result)).get
      status(result) mustBe OK

      giveaway.id mustBe Some(1)
      giveaway.name mustBe "Logitech G Pro"
      giveaway.isSubscribersOnly mustBe false
    }

    "return error when giveaway is not found (get)" in {
      val request = FakeRequest(GET, "/giveaways/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Giveaway 12 not found"
    }

    "create a giveaway" in {
      val payload = Json.obj("name" -> "OnePlus 7 Pro", "is_subscribers_only" -> true)
      val request = FakeRequest(POST, "/giveaways")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val giveaway: Giveaway = Json.fromJson[Giveaway](contentAsJson(result)).get
      status(result) mustBe CREATED

      giveaway.id mustBe defined
      giveaway.name mustBe "OnePlus 7 Pro"
      giveaway.isSubscribersOnly mustBe true
    }

    "return an error when payload is not correct" in {
      val payload = Json.obj("incorrect_key" -> "Walkoss")
      val request = FakeRequest(POST, "/giveaways")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }

    "draw a giveaway winner" in {
      val request = FakeRequest(POST, "/giveaways/1:draw").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val user: User = Json.fromJson[User](contentAsJson(result)).get
      status(result) mustBe OK
      user.id mustBe defined
    }

    "return an error if giveaway is not found" in {
      val request = FakeRequest(POST, "/giveaways/12:draw").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Either giveaway 12 doesn't exists or doesn't contains registration"
    }

    "return an error if giveaway doesn't contains registration" in {
      val request = FakeRequest(POST, "/giveaways/3:draw").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Either giveaway 3 doesn't exists or doesn't contains registration"
    }

    "return an error if user is not subscribed" in {
      val payload = Json.obj("user_id" -> 2)
      val request = FakeRequest(POST, "/giveaways/2:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe BAD_REQUEST
      message mustBe "User 2 is not a subscriber (giveaway with subscribers only)"
    }

    "register a user for a giveaway" in {
      val payload = Json.obj("user_id" -> 2)
      val request = FakeRequest(POST, "/giveaways/3:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe OK
      message mustBe "User 2 subscribed to giveaway 3"
    }

    "return an error if user is blacklisted" in {
      val payload = Json.obj("user_id" -> 4)
      val request = FakeRequest(POST, "/giveaways/1:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe BAD_REQUEST
      message mustBe "User 4 is blacklisted!"
    }

    "return an error if giveaway is not found (register)" in {
      val payload = Json.obj("user_id" -> 4)
      val request = FakeRequest(POST, "/giveaways/12:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Giveaway 12 not found"
    }

    "return an error if user is not found" in {
      val payload = Json.obj("user_id" -> 12)
      val request = FakeRequest(POST, "/giveaways/1:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "return an error when user has already registered for a giveaway" in {
      val payload = Json.obj("user_id" -> 1)
      val request = FakeRequest(POST, "/giveaways/1:register")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe CONFLICT
      message mustBe "User 1 has already registered"
    }

    "delete giveaway" in {
      val request = FakeRequest(DELETE, "/giveaways/3").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      status(result) mustBe NO_CONTENT
    }

    "return error when giveaway is not found (delete)" in {
      val request = FakeRequest(DELETE, "/giveaways/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Giveaway 12 not found"
    }
  }
}
