package poll

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import user.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PollController @Inject()
(pollRepo: PollRepository, val userRepo: UserRepository, val pollVoteRepo: PollVoteRepository, val cc: ControllerComponents)
(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index: Action[AnyContent] = Action.async { implicit request =>
    pollRepo.all.map { polls =>
      Ok(Json.toJson(polls))
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val validationResult = request.body.validate[Poll]

    validationResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        }
      },
      poll => {
        pollRepo.create(poll) map { poll =>
          Created(Json.toJson(poll))
        }
      }
    )
  }

  def show(id: Int): Action[AnyContent] = Action.async { implicit request =>
    pollRepo.show(id).map {
      case Some(poll: Poll) => Ok(Json.toJson(poll))
      case None => NotFound(Json.obj("message" -> s"Poll $id not found"))
    }
  }

  def delete(id: Int): Action[AnyContent] = Action.async { implicit request =>
    pollRepo.delete(id).map {
      case 1 => NoContent
      case 0 => NotFound(Json.obj("message" -> s"Poll $id not found"))
    }
  }

  def vote(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    // TODO: validate body
    val userId = (request.body \ "user_id").as[Int]
    val choice = (request.body \ "choice").as[String].trim

    userRepo.exists(userId) flatMap {
      case true => pollRepo.exists(id) flatMap {
        case true => pollRepo.choiceExists(id, choice) flatMap {
          case true => pollVoteRepo.exists(id, userId) flatMap {
            case true => Future.successful(Conflict(Json.obj("message" -> s"User $userId already voted")))
            case false => pollVoteRepo.create(PollVote(pollId = id, userId = userId, choice = choice)).map(_ => Ok(Json.obj("message" -> s"User $userId voted '$choice' to poll $id")))
          }
          case false => Future.successful(BadRequest(Json.obj("message" -> s"Choice '$choice' is not correct for poll $id")))
        }
        case false => Future.successful(NotFound(Json.obj("message" -> s"Poll $id not found")))
      }
      case false => Future.successful(NotFound(Json.obj("message" -> s"User $userId not found")))
    }
  }

  def result(id: Int): Action[AnyContent] = Action.async { implicit request =>
    implicit val itemWrite: Writes[(String, Int)] = (item: (String, Int)) => Json.obj(
      "choice" -> item._1,
      "total" -> item._2
    )

    pollRepo.exists(id) flatMap {
      case true => pollVoteRepo.result(id).map(result => Ok(Json.toJson(result)))
      case false => Future.successful(NotFound(Json.obj("message" -> s"Poll $id not found")))
    }
  }
}
