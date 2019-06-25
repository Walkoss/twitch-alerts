package user

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()
(repo: UserRepository, val cc: ControllerComponents)
(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index(is_subscribed: Option[Boolean], is_blacklisted: Option[Boolean]): Action[AnyContent] = Action.async { implicit request =>
    repo.all(is_subscribed, is_blacklisted).map { users =>
      Ok(Json.toJson(users))
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val userResult = request.body.validate[User]

    userResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        }
      },
      user => {
        repo.usernameExists(user.username.trim) flatMap {
          case true => Future.successful(Conflict(Json.obj("message" -> s"${user.username.trim} is already taken")))
          case false => repo.create(user).map(user => Created(Json.toJson(user)))
        }
      }
    )
  }

  def delete(id: Int): Action[AnyContent] = Action.async { implicit request =>
    repo.delete(id).map {
      case 1 => NoContent
      case 0 => NotFound(Json.obj("message" -> s"User $id not found"))
    }
  }

  def show(id: Int): Action[AnyContent] = Action.async { implicit request =>
    repo.show(id).map {
      case Some(user: User) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj("message" -> s"User $id not found"))
    }
  }

  def update(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    // TODO: validate body
    val isSubscribed = (request.body \ "is_subscribed").asOpt[Boolean]
    val isBlacklisted = (request.body \ "is_blacklisted").asOpt[Boolean]

    repo.update(id, isSubscribed, isBlacklisted).map {
      // TODO: change this to return user object updated
      case 1 => Ok(Json.obj("message" -> s"User $id updated"))
      case 0 => NotFound(Json.obj("message" -> s"User $id not found"))
    }
  }
}
