package poll

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class PollRouter @Inject()(controller: PollController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.create

    case GET(p"/${int(id)}") =>
      controller.show(id)

    case DELETE(p"/${int(id)}") =>
      controller.delete(id)

    case POST(p"/${int(id)}:vote") =>
      controller.vote(id)

    case GET(p"/${int(id)}:result") =>
      controller.result(id)
  }
}
