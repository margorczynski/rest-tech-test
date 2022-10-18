package io.github.margorczynski.techtest

import cats.effect.IO
import cats.implicits._
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import io.github.margorczynski.techtest.RouteError.{
  JsonDeserializationError,
  RouteResult,
  RouteServiceError
}
import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.model.JsonSchemaModel._
import io.github.margorczynski.techtest.service.JsonSchemaService
import io.github.margorczynski.techtest.service.ServiceError.JsonSchemaIdMissingError
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object Route {

  private def schemaRoutes(jsonSchemaService: JsonSchemaService) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "schema" / schemaId => {
      val result = for {
        decodedJson <- decodeRequest[Json](req)
        jsonSchemaModel = JsonSchemaModel(schemaId, decodedJson)
        _ <- jsonSchemaService.create(jsonSchemaModel).leftMap[RouteError](RouteServiceError)
      } yield ()

      result.value
        .flatMap {
          case ok @ Right(_) => Created(RouteResponse("uploadSchema", schemaId, ok).asJson)
          case err @ Left(_) => BadRequest(RouteResponse("uploadSchema", schemaId, err).asJson)
        }
    }

    case GET -> Root / "schema" / schemaId =>
      jsonSchemaService
        .retrieve(schemaId)
        .value
        .flatMap {
          case Right(jsonSchemaModel) => Ok(jsonSchemaModel.asJson)
          case err @ Left(JsonSchemaIdMissingError(_)) =>
            NotFound(
              RouteResponse("downloadSchema", schemaId, err.leftMap(RouteServiceError)).asJson
            )
          case err @ Left(_) =>
            BadRequest(
              RouteResponse("downloadSchema", schemaId, err.leftMap(RouteServiceError)).asJson
            )
        }
  }

  private def validationRoutes(jsonSchemaService: JsonSchemaService) = HttpRoutes.of[IO] {
    case req @ POST -> Root / "validate" / schemaId => {
      val result = for {
        decodedJson <- decodeRequest[Json](req)
        _ <- jsonSchemaService
          .validate(decodedJson, schemaId)
          .leftMap[RouteError](RouteServiceError)
      } yield ()

      result.value
        .flatMap {
          case ok @ Right(_) => Ok(RouteResponse("validateDocument", schemaId, ok).asJson)
          case err @ Left(RouteServiceError(JsonSchemaIdMissingError(_))) =>
            NotFound(RouteResponse("validateDocument", schemaId, err).asJson)
          case err @ Left(_) => BadRequest(RouteResponse("validateDocument", schemaId, err).asJson)
        }
    }

  }

  private def decodeRequest[T](req: Request[IO])(implicit
      ed: EntityDecoder[IO, T]
  ): RouteResult[T] =
    req.attemptAs[T].leftMap(JsonDeserializationError)

  case class RouteResponse(action: String, id: String, status: Either[RouteError, _])

  implicit val responseEncoder: Encoder[RouteResponse] = Encoder.instance { response =>
    val baseFields = Seq(
      "action" -> response.action,
      "id"     -> response.id,
      "status" -> response.status.fold(_ => "error", _ => "success")
    )

    val finalFields = response.status match {
      case Right(_)         => baseFields
      case Left(routeError) => baseFields :+ ("message" -> routeError.show)
    }

    Json.obj(
      finalFields.map { case (name, value) => name -> Json.fromString(value) }: _*
    )
  }

  /** Create the routes for the HTTP application by aggregating the schema and validation routes.
    *
    * @param jsonSchemaService
    *   The JSON Schema Service to be used for executing the logic.
    * @return
    *   An aggregate of JSON Schema and Schema validation routes.
    */
  def routes(jsonSchemaService: JsonSchemaService): HttpRoutes[IO] =
    schemaRoutes(jsonSchemaService) <+> validationRoutes(jsonSchemaService)
}
