package poll

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class PollVote
(
  pollId: Int,
  userId: Int,
  choice: String
)

object PollVote {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val userFormat: Format[PollVote] = Json.format[PollVote]
}