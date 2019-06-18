package poll

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PollRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PollTable(tag: Tag) extends Table[Poll](tag, "poll") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def question = column[String]("question")

    def firstChoice = column[String]("first_choice")

    def secondChoice = column[String]("second_choice")

    def * = (id.?, question, firstChoice, secondChoice) <> ((Poll.apply _).tupled, Poll.unapply)
  }

  val polls = TableQuery[PollTable]

  def all: Future[Seq[Poll]] = db.run {
    polls.result
  }

  def create(poll: Poll): Future[Poll] = db.run {
    (polls returning polls.map(_.id) into ((poll, id) => poll.copy(id = Some(id)))) += poll
  }

  def show(id: Int): Future[Option[Poll]] = db.run {
    polls.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Int] = db.run {
    polls.filter(_.id === id).delete
  }

  def exists(id: Int): Future[Boolean] = db.run {
    polls.filter(_.id === id).exists.result
  }

  def choiceExists(id: Int, choice: String): Future[Boolean] = db.run {
    polls.filter(p => p.id === id && (p.firstChoice === choice || p.secondChoice === choice)).exists.result
  }
}