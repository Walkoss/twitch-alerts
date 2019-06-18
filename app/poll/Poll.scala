package poll

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class Poll
(
  id: Option[Int],
  question: String,
  firstChoice: String,
  secondChoice: String
)

object Poll {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[Poll] = Json.format[Poll]
}