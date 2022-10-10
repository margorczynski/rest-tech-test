package io.github.margorczynski.techtest

import cats.effect.IO
import cats.implicits._
import io.github.margorczynski.techtest.repository.JsonSchemaRepository
import io.github.margorczynski.techtest.service.JsonSchemaService
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object Routes {

  private def schemaRoutes(jsonSchemaService: JsonSchemaService) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "schema" / schemaId =>
      Ok()

    case GET -> Root / "schema" / schemaId =>
      Ok()
  }

  private def validationRoutes(jsonSchemaService: JsonSchemaService) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "validate" / schemaId =>
      Ok()
  }

  def routes(jsonSchemaService: JsonSchemaService): HttpRoutes[IO] =
    schemaRoutes(jsonSchemaService) <+> validationRoutes(jsonSchemaService)
}
