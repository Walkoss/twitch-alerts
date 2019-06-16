package giveaway

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class Giveaway
(
  id: Option[Int],
  name: String,
  isSubscribersOnly: Boolean,
)

object Giveaway {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[Giveaway] = Json.format[Giveaway]
}