package router

import play.api.libs.json.{JsArray, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import testhelpers.BaseSpec
import tip.Tip
import user.User

import scala.concurrent.Future

class TipRouterSpec extends BaseSpec {
  "TipRouter" should {

    "list tips" in {
      val request = FakeRequest(GET, "/tips").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val tips: Seq[Tip] = Json.fromJson[Seq[Tip]](contentAsJson(result)).get
      status(result) mustBe OK
      tips.length mustBe 6

      tips.head.id mustBe Some(1)
      tips.head.amount mustBe 2.99
      tips.head.userId mustBe 1
    }

    "list tips and users" in {
      val request = FakeRequest(GET, "/tips/users").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val tipsUsers: JsArray = contentAsJson(result).as[JsArray]
      status(result) mustBe OK
      val tipHead = (tipsUsers.head \ "tip").as[Tip]
      val userHead = (tipsUsers.head \ "user").as[User]

      tipHead.id mustBe Some(1)
      tipHead.amount mustBe 2.99
      tipHead.userId mustBe 1

      userHead.id mustBe Some(1)
      userHead.username mustBe "Walid"
      userHead.isSubscribed mustBe true
      userHead.isBlacklisted mustBe false
    }

    "sum all tips" in {
      val request = FakeRequest(GET, "/tips?aggregate=sum").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val sum: Double = (contentAsJson(result) \ "sum").as[Double]
      status(result) mustBe OK
      sum mustBe 64.98
    }

    "sum all tips by users" in {
      val request = FakeRequest(GET, "/tips?groupby=user_id&aggregate=sum").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val tipsUsers: JsArray = contentAsJson(result).as[JsArray]
      status(result) mustBe OK
      (tipsUsers.head \ "user_id").as[Int] mustBe 1
      (tipsUsers.head \ "sum").as[Double] mustBe 9.98
    }

    "sum all tips for a given user" in {
      val request = FakeRequest(GET, "/tips?aggregate=sum&user_id=2").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val sum: Double = (contentAsJson(result) \ "sum").as[Double]
      status(result) mustBe OK
      sum mustBe 40
    }

    "return an error if user is not found (sum tips)" in {
      val request = FakeRequest(GET, "/tips?aggregate=sum&user_id=12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "create a tip" in {
      val payload = Json.obj("amount" -> 10.99, "user_id" -> 1)
      val request = FakeRequest(POST, "/tips")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val tip: Tip = Json.fromJson[Tip](contentAsJson(result)).get
      status(result) mustBe CREATED

      tip.id mustBe defined
      tip.amount mustBe 10.99
      tip.userId mustBe 1
    }

    "return an error if user is not found (create tip)" in {
      val payload = Json.obj("amount" -> 10.99, "user_id" -> 12)
      val request = FakeRequest(POST, "/tips")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "return an error when payload is not correct" in {
      val payload = Json.obj("incorrect_key" -> 10.99)
      val request = FakeRequest(POST, "/tips")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }

    "delete tip" in {
      val request = FakeRequest(DELETE, "/tips/4").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      status(result) mustBe NO_CONTENT
    }

    "return error when tip is not found (delete)" in {
      val request = FakeRequest(DELETE, "/tips/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "Tip 12 not found"
    }
  }
}
