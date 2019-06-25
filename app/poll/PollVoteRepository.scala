package poll

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import user.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PollVoteRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val userRepository: UserRepository, val pollRepository: PollRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PollVoteTable(tag: Tag) extends Table[PollVote](tag, "poll_vote") {
    def pollId = column[Int]("poll_id")

    def userId = column[Int]("user_id")

    def choice = column[String]("choice")

    def poll = foreignKey("poll_fk", userId, pollRepository.polls)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user = foreignKey("user_fk", userId, userRepository.users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (pollId, userId, choice) <> ((PollVote.apply _).tupled, PollVote.unapply)
  }

  val pollVotes = TableQuery[PollVoteTable]

  def all: Future[Seq[PollVote]] = db.run {
    pollVotes.result
  }

  def create(pollVote: PollVote): Future[Int] = db.run {
    pollVotes += pollVote
  }

  def exists(pollId: Int, userId: Int): Future[Boolean] = db.run {
    pollVotes.filter(pv => pv.pollId === pollId && pv.userId === userId).exists.result
  }

  def result(pollId: Int): Future[Seq[(String, Int)]] = db.run {
    pollVotes.filter(pv => pv.pollId === pollId).groupBy(_.choice).map {
      case (pv, results) => pv -> results.length
    }.result
  }
}