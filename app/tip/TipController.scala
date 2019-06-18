package tip

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import user.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TipController @Inject()
(repo: TipRepository, userRepo: UserRepository, val cc: ControllerComponents)
(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index: Action[AnyContent] = Action.async { implicit request =>
    repo.all.map { tips =>
      Ok(Json.toJson(tips))
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val tipValidationResult = request.body.validate[Tip]

    tipValidationResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        }
      },
      tip => {
        userRepo.exists(tip.userId) flatMap {
          case true => repo.create(tip).map(tip => Ok(Json.toJson(tip)))
          case false => Future.successful(NotFound(Json.obj("message" -> s"User ${tip.userId} not found")))
        }
      }
    )
  }

  def delete(id: Int): Action[AnyContent] = Action.async { implicit request =>
    repo.delete(id).map {
      case 1 => NoContent
      case 0 => NotFound(Json.obj("message" -> s"Tip $id not found"))
    }
  }

  def aggregateTips(aggregate: String, groupBy: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    groupBy match {
      case Some(_) => repo.sumAllTipsByUsers.map { result =>
        result.map {
          case (userId, sum) => Json.obj("user_id" -> userId, "sum" -> sum)
        }
      }.map(result => Ok(Json.toJson(result)))
      case None => repo.sumAllTips.map {
        case None => Ok(Json.obj("sum" -> 0))
        case Some(sum) => Ok(Json.obj("sum" -> sum))
      }
    }
  }

  def aggregateTipsByUser(aggregate: String, userId: Int): Action[AnyContent] = Action.async { implicit request =>
    repo.sumAllTipsByUser(userId).map {
      case Some(value) => Ok(Json.obj("sum" -> value))
      case None => NotFound(Json.obj("message" -> s"User $userId not found"))
    }
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    repo.getUsers.map { result =>
      result.map {
        case (tip, user) => Json.obj("tip" -> tip, "user" -> user)
      }
    }.map(result => Ok(Json.toJson(result)))
  }
}
