package io.github.margorczynski.techtest.service

import cats.data.EitherT
import cats.effect.IO
import io.circe.Json
import io.github.margorczynski.techtest.model.JsonSchema

sealed trait ServiceError

object ServiceError {

  //Represents the result of a Service computation
  type ServiceResult[T] = EitherT[IO, ServiceError, T]

  case class JsonSchemaIdTakenError(schemaId: String) extends ServiceError
  case class JsonSchemaIdMissingError(missingSchemaId: String) extends ServiceError
  case class JsonValidationError(json: Json, jsonSchema: JsonSchema) extends ServiceError
}