package giveaway

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GiveawayRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class GiveawayTable(tag: Tag) extends Table[Giveaway](tag, "giveaway") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def isSubscribersOnly = column[Boolean]("is_subscribers_only", O.Default(false))

    def * = (id.?, name, isSubscribersOnly) <> ((Giveaway.apply _).tupled, Giveaway.unapply)
  }

  val giveaways = TableQuery[GiveawayTable]

  def all: Future[Seq[Giveaway]] = db.run {
    giveaways.result
  }

  def delete(id: Int): Future[Int] = db.run {
    giveaways.filter(_.id === id).delete
  }

  def create(giveaway: Giveaway): Future[Giveaway] = db.run {
    (giveaways returning giveaways.map(_.id) into ((giveaway, id) => giveaway.copy(id = Some(id)))) += giveaway
  }

  def show(id: Int): Future[Option[Giveaway]] = db.run {
    giveaways.filter(_.id === id).result.headOption
  }
}