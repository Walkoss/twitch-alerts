package router

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import testhelpers.BaseSpec
import user.User

import scala.concurrent.Future

class UserRouterSpec extends BaseSpec {
  "UserRouter" should {

    "list users" in {
      val request = FakeRequest(GET, "/users").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val users: Seq[User] = Json.fromJson[Seq[User]](contentAsJson(result)).get
      status(result) mustBe OK
      users.length mustBe 4

      users.head.id mustBe Some(1)
      users.head.username mustBe "Walid"
      users.head.isBlacklisted mustBe false
      users.head.isSubscribed mustBe true

      users(1).id mustBe Some(2)
      users(1).username mustBe "Pierre"
      users(1).isBlacklisted mustBe false
      users(1).isSubscribed mustBe false

      users(2).id mustBe Some(3)
      users(2).username mustBe "Alexis"
      users(2).isBlacklisted mustBe true
      users(2).isSubscribed mustBe false

      users.last.id mustBe Some(4)
      users.last.username mustBe "Christophe"
      users.last.isBlacklisted mustBe true
      users.last.isSubscribed mustBe false
    }

    "list subscribers" in {
      val request = FakeRequest(GET, "/users?is_subscribed=1").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val users: Seq[User] = Json.fromJson[Seq[User]](contentAsJson(result)).get
      status(result) mustBe OK
      users.length mustBe 1

      users.head.id mustBe Some(1)
      users.head.username mustBe "Walid"
      users.head.isBlacklisted mustBe false
      users.head.isSubscribed mustBe true
    }

    "list blacklisted users" in {
      val request = FakeRequest(GET, "/users?is_blacklisted=1").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val users: Seq[User] = Json.fromJson[Seq[User]](contentAsJson(result)).get
      status(result) mustBe OK
      users.length mustBe 2

      users.head.id mustBe Some(3)
      users.head.username mustBe "Alexis"
      users.head.isBlacklisted mustBe true
      users.head.isSubscribed mustBe false
    }

    "show user by id" in {
      val request = FakeRequest(GET, "/users/1").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get

      val user: User = Json.fromJson[User](contentAsJson(result)).get
      status(result) mustBe OK

      user.id mustBe Some(1)
      user.username mustBe "Walid"
      user.isBlacklisted mustBe false
      user.isSubscribed mustBe true
    }

    "return error when user is not found (get)" in {
      val request = FakeRequest(GET, "/users/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "delete user" in {
      val request = FakeRequest(DELETE, "/users/4").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      status(result) mustBe NO_CONTENT
    }

    "return error when user is not found (delete)" in {
      val request = FakeRequest(DELETE, "/users/12").withHeaders(HOST -> "localhost:9000")
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }

    "create a user" in {
      val payload = Json.obj("username" -> "Walkoss", "is_subscribed" -> true, "is_blacklisted" -> false)
      val request = FakeRequest(POST, "/users")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val user: User = Json.fromJson[User](contentAsJson(result)).get
      status(result) mustBe CREATED

      user.id mustBe defined
      user.username mustBe "Walkoss"
      user.isBlacklisted mustBe false
      user.isSubscribed mustBe true
    }

    "return an error when payload is not correct" in {
      val payload = Json.obj("incorrect_key" -> "Walkoss")
      val request = FakeRequest(POST, "/users")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }

    "return an error when username is already taken" in {
      val payload = Json.obj("username" -> "walkoss", "is_subscribed" -> true, "is_blacklisted" -> false)
      val request = FakeRequest(POST, "/users")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe CONFLICT
      message mustBe "walkoss is already taken"
    }

    "update a user" in {
      val payload = Json.obj("is_subscribed" -> false, "is_blacklisted" -> true)
      val request = FakeRequest(PATCH, "/users/5")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val user: User = Json.fromJson[User](contentAsJson(result)).get
      status(result) mustBe OK

      user.id mustBe defined
      user.username mustBe "Walkoss"
      user.isBlacklisted mustBe true
      user.isSubscribed mustBe false
    }

    "return an error when user is not found (update)" in {
      val payload = Json.obj("is_subscribed" -> false, "is_blacklisted" -> true)
      val request = FakeRequest(PATCH, "/users/12")
        .withHeaders(HOST -> "localhost:9000")
        .withJsonBody(payload)
      val result: Future[Result] = route(app, request).get
      val message = (contentAsJson(result) \ "message").as[String]
      status(result) mustBe NOT_FOUND
      message mustBe "User 12 not found"
    }
  }
}
