package user

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def username = column[String]("username")

    def isSubscribed = column[Boolean]("is_subscribed", O.Default(false))

    def isBlacklisted = column[Boolean]("is_blacklisted", O.Default(false))

    def * = (id.?, username, isSubscribed, isBlacklisted) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UserTable]

  def all(isSubscribed: Option[Boolean], isBlacklisted: Option[Boolean]): Future[Seq[User]] = db.run {
    users
      .filter { user =>
        isSubscribed.fold(true.bind)(user.isSubscribed === _)
      }
      .filter { user =>
        isBlacklisted.fold(true.bind)(user.isBlacklisted === _)
      }
      .result
  }

  def delete(id: Int): Future[Int] = db.run {
    users.filter(_.id === id).delete
  }

  def create(user: User): Future[User] = db.run {
    (users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += user
  }

  def exists(id: Int): Future[Boolean] = db.run {
    users.filter(_.id === id).exists.result
  }

  def usernameExists(username: String): Future[Boolean] = db.run {
    users.filter(_.username.toLowerCase === username.toLowerCase).exists.result
  }

  def show(id: Int): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def update(id: Int, isSubscribed: Option[Boolean], isBlacklisted: Option[Boolean]): Future[Int] = db.run {
    (isSubscribed, isBlacklisted) match {
      case (Some(x), Some(y)) =>
        val q = for {u <- users if u.id === id} yield (u.isSubscribed, u.isBlacklisted)
        q.update((x, y))
      case (Some(x), _) =>
        val q = for {u <- users if u.id === id} yield u.isSubscribed
        q.update(x)
      case (_, Some(y)) =>
        val q = for {u <- users if u.id === id} yield u.isBlacklisted
        q.update(y)
      case (None, None) =>
        return Future.successful(0)
    }
  }
}