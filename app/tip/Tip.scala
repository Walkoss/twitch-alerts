package tip

import play.api.libs.json._
import play.api.libs.json.JsonNaming.SnakeCase

case class Tip
(
  id: Option[Int],
  amount: Double,
  user_id: Int,
)

object Tip {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[Tip] = Json.format[Tip]
}