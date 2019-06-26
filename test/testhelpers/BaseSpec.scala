package testhelpers

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolutions, SimpleEvolutionsReader, ThisClassLoaderEvolutionsReader}
import play.api.inject.Injector

abstract class BaseSpec extends PlaySpec with BeforeAndAfterAll with GuiceOneAppPerSuite {
  lazy val injector: Injector = app.injector

  lazy val databaseApi: DBApi = injector.instanceOf[DBApi]

  override def beforeAll(): Unit = {
    super.beforeAll()
    Evolutions.applyEvolutions(databaseApi.database("default"))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
  }
}