package giveaway

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class GiveawayRegistration
(
  giveawayId: Int,
  userId: Int,
)

object GiveawayRegistration {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[GiveawayRegistration] = Json.format[GiveawayRegistration]
}