package giveaway

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import user.{User, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GiveawayController @Inject()
(gaRepo: GiveawayRepository, garRepo: GiveawayRegistrationRepository, userRepo: UserRepository, val cc: ControllerComponents)
(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index: Action[AnyContent] = Action.async { implicit request =>
    gaRepo.all.map { tips =>
      Ok(Json.toJson(tips))
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val giveawayResult = request.body.validate[Giveaway]

    giveawayResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        }
      },
      giveaway => {
        gaRepo.create(giveaway).map { giveaway =>
          Created(Json.toJson(giveaway))
        }
      }
    )
  }

  def delete(id: Int): Action[AnyContent] = Action.async { implicit request =>
    gaRepo.delete(id).map {
      case 1 => NoContent
      case 0 => NotFound(Json.obj("message" -> s"Giveaway $id not found"))
    }
  }

  def show(id: Int): Action[AnyContent] = Action.async { implicit request =>
    gaRepo.show(id).map {
      case Some(giveaway: Giveaway) => Ok(Json.toJson(giveaway))
      case None => NotFound(Json.obj("message" -> s"Giveaway $id not found"))
    }
  }

  def register(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    // TODO: validate body
    val userId = (request.body \ "user_id").as[Int]

    userRepo.show(userId) flatMap {
      case Some(u: User) => gaRepo.show(id) flatMap {
        case Some(g: Giveaway) => (g.isSubscribersOnly, u.isSubscribed, u.isBlacklisted) match {
          case (true, false, false) => Future.successful(BadRequest(Json.obj("message" -> s"User $userId is not a subscriber (giveaway with subscribers only)")))
          case (_, _, true) => Future.successful(BadRequest(Json.obj("message" -> s"User $userId is blacklisted!")))
          case (_, _, _) => garRepo.create(GiveawayRegistration(giveawayId = id, userId = userId)).map(_ => Created(Json.obj("message" -> s"User $userId subscribed to giveaway $id")))
        }
        case None => Future.successful(NotFound(Json.obj("message" -> s"Giveaway $id not found")))
      }
      case None => Future.successful(NotFound(Json.obj("message" -> s"User $userId not found")))
    }
  }

  def draw(id: Int): Action[AnyContent] = Action.async { implicit request =>
    garRepo.draw(id).map {
      case Some((_: GiveawayRegistration, user: User)) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj("message" -> s"Either giveaway $id doesn't exists or doesn't contains registration"))
    }
  }
}
