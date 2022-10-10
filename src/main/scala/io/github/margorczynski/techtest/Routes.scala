package io.github.margorczynski.techtest

import cats.effect.IO
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.LongVar
import org.http4s.dsl.io.{POST, _}

object Routes {

  private val schemaRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "schema" / LongVar(schemaId) =>
      Ok()

    case GET -> Root / "schema" / LongVar(schemaId) =>
      Ok()
  }

  private val validationRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "validate" / LongVar(schemaId) =>
      Ok()
  }

  val routes: HttpRoutes[IO] =
    schemaRoutes <+> validationRoutes
}
