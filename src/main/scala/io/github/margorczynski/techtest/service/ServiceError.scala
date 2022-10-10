package io.github.margorczynski.techtest.service

import cats.data.EitherT
import cats.effect.IO

sealed trait ServiceError extends Serializable with Product

object ServiceError {

  //Represents the result of a Service computation
  type ServiceResult[T] = EitherT[IO, ServiceError, T]

  case class JsonSchemaIdTakenError(schemaId: String) extends ServiceError
  case class JsonSchemaIdMissingError(missingSchemaId: String) extends ServiceError
  case class JsonValidationExceptionError(validationExceptionMessage: String) extends ServiceError
  case class JsonValidationError(validationErrorMessages: List[String]) extends ServiceError
}