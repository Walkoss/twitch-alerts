package tip

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import user.{User, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TipRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class TipTable(tag: Tag) extends Table[Tip](tag, "tip") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def amount = column[Double]("amount")

    def userId = column[Int]("user_id")

    def user = foreignKey("user_fk", userId, userRepository.users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, amount, userId) <> ((Tip.apply _).tupled, Tip.unapply)
  }

  val tips = TableQuery[TipTable]

  def all: Future[Seq[Tip]] = db.run {
    tips.result
  }

  def delete(id: Int): Future[Int] = db.run {
    tips.filter(_.id === id).delete
  }

  def create(tip: Tip): Future[Tip] = db.run {
    (tips returning tips.map(_.id) into ((tip, id) => tip.copy(id = Some(id)))) += tip
  }

  def sumAllTips: Future[Option[Double]] = db.run {
    tips.map(_.amount).sum.result
  }

  def sumAllTipsByUsers: Future[Seq[(Int, Option[Double])]] = db.run {
    tips.groupBy(_.userId).map {
      case (userId, group) => (userId, group.map(_.amount).sum)
    }.result
  }

  def sumAllTipsByUser(userId: Int): Future[Option[Double]] = db.run {
    tips.filter(_.userId === userId).map(_.amount).sum.result
  }

  def getUsers: Future[Seq[(Tip, User)]] = db.run {
    val q = for {
      (t, u) <- tips join userRepository.users on (_.userId === _.id)
    } yield (t, u)
    q.result
  }
}