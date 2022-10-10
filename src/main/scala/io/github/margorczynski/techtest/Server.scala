package io.github.margorczynski.techtest

import cats.effect.{Async, IO, Resource}
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import fs2.Stream
import io.github.margorczynski.techtest.config.Config
import org.flywaydb.core.Flyway
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Server {

  private val httpApp =
    Routes.routes.orNotFound

  // We add the http4s logging middleware for debug, take care in case of sensitive data like passwords!
  private val httpAppWithLogging =
    Logger.httpApp[IO](true, true)(httpApp)

  /** Use Flyway to migrate the database based on the given transactor. We don't map the exception
    * here to the AppError as there's no point - there is no way to recover and should fail fast.
    *
    * @param transactor
    *   The transactor for the database
    * @return
    */
  private def migrateDatabase(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        Flyway.configure().dataSource(dataSource).load().migrate()
      }
    }
  }

  /** Create the resources need by the app - the config instance and transactor to the DB.
    *
    * @param configFilename
    *   Name of the config resource - defaults to application.conf
    * @return
    *   The resources instance
    */
  private def createResources(
      configFilename: String = "application.conf"
  ): Resource[IO, Resources] = {
    for {
      config <- Resource.eval(Config.load(configFilename))
      ec     <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- HikariTransactor.newHikariTransactor[IO](
        config.database.driver,
        config.database.url,
        config.database.user,
        config.database.password,
        ec
      )
    } yield Resources(config, transactor)
  }

  def stream: Stream[IO, Nothing] = {
    for {
      resources <- Stream.resource(createResources())
      _         <- Stream.eval(migrateDatabase(resources.transactor))
      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[IO]
          .withHost(resources.config.server.hostname)
          .withPort(resources.config.server.port)
          .withHttpApp(httpAppWithLogging)
          .build >>
          Resource.eval(Async[IO].never)
      )
    } yield exitCode
  }.drain

  case class Resources(config: Config, transactor: HikariTransactor[IO])
}
