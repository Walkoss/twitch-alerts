package user

import play.api.libs.json._
import play.api.libs.json.JsonNaming.SnakeCase

case class User
(
  id: Option[Int],
  username: String,
  isSubscribed: Boolean = false,
  isBlacklisted: Boolean = false
)

object User {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[User] = Json.format[User]
}