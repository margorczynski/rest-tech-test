package io.github.margorczynski.techtest.service

import cats.Show
import cats.data.EitherT
import cats.effect.IO

sealed trait ServiceError extends Serializable with Product

object ServiceError {

  // Represents the result of a Service computation
  type ServiceResult[T] = EitherT[IO, ServiceError, T]

  case class JsonSchemaIdTakenError(schemaId: String)                         extends ServiceError
  case class JsonSchemaIdMissingError(missingSchemaId: String)                extends ServiceError
  case class JsonValidationExceptionError(validationExceptionMessage: String) extends ServiceError
  case class JsonValidationError(validationErrorMessages: List[String])       extends ServiceError

  implicit val showServiceError: Show[ServiceError] = Show.show {
    case JsonSchemaIdTakenError(schemaId) => s"Schema with ID: $schemaId already exists"
    case JsonSchemaIdMissingError(missingSchemaId) =>
      s"Schema with ID $missingSchemaId doesn't exist"
    case JsonValidationExceptionError(validationExceptionMessage) =>
      s"Exception during validation: $validationExceptionMessage"
    case JsonValidationError(validationErrorMessages) => validationErrorMessages.mkString(", ")
  }
}
