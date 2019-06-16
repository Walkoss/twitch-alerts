package tip

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class TipRouter @Inject()(controller: TipController) extends SimpleRouter {
  override def routes: Routes = {
    case POST(p"/") =>
      controller.create

    case DELETE(p"/${int(id)}") =>
      controller.delete(id)

    case GET(p"/" ? q"aggregate=$aggregate" & q"user_id=${int(userId)}") =>
      controller.aggregateTipsByUser(aggregate, userId)

    case GET(p"/" ? q"aggregate=$aggregate" & q_o"groupby=$groupBy") =>
      controller.aggregateTips(aggregate, groupBy)

    case GET(p"/") =>
      controller.index

    case GET(p"/users") =>
      controller.getUsers
  }
}
