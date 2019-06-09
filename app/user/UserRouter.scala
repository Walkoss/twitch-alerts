package user

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UserRouter @Inject()(controller: UserController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/"
      ? q_?"is_subscribed=${bool(is_subscribed)}"
      & q_?"is_blacklisted=${bool(is_blacklisted)}") =>
      controller.index(is_subscribed, is_blacklisted)

    case POST(p"/") =>
      controller.create

    case GET(p"/${int(id)}") =>
      controller.show(id)

    case DELETE(p"/${int(id)}") =>
      controller.delete(id)
  }
}
