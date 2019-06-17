package giveaway

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import user.{User, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GiveawayRegistrationRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository, val giveawayRepository: GiveawayRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class GiveawayRegistrationTable(tag: Tag) extends Table[GiveawayRegistration](tag, "giveaway_registration") {
    def giveawayId = column[Int]("giveaway_id")

    def userId = column[Int]("user_id")

    def giveaway = foreignKey("giveaway_fk", userId, giveawayRepository.giveaways)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user = foreignKey("user_fk", userId, userRepository.users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (giveawayId, userId) <> ((GiveawayRegistration.apply _).tupled, GiveawayRegistration.unapply)
  }

  val giveawaysRegistrations = TableQuery[GiveawayRegistrationTable]

  def all: Future[Seq[GiveawayRegistration]] = db.run {
    giveawaysRegistrations.result
  }

  def create(giveaway: GiveawayRegistration): Future[Int] = db.run {
    giveawaysRegistrations += giveaway
  }

  def draw(giveawayId: Int): Future[Option[(GiveawayRegistration, User)]] = db.run {
    val randomFunction = SimpleFunction.nullary[Double]("random")

    val q = for {
      (gar, u) <- giveawaysRegistrations join userRepository.users on (_.userId === _.id)
    } yield (gar, u)

    q.filter(_._1.giveawayId === giveawayId)
      .sortBy(_ => randomFunction)
      .take(1)
      .result.headOption
  }

  def exists(giveawayId: Int, userId: Int): Future[Boolean] = db.run {
    giveawaysRegistrations.filter(gar => gar.giveawayId === giveawayId && gar.userId === userId).exists.result
  }
}