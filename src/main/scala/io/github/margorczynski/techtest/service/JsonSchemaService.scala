package io.github.margorczynski.techtest.service

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ListProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.Json
import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.repository.JsonSchemaRepository
import io.github.margorczynski.techtest.service.ServiceError._

import scala.jdk.CollectionConverters._
import scala.util.Try

final class JsonSchemaService(jsonSchemaRepository: JsonSchemaRepository) {

  /** Create (insert) the given JSON Schema via the repository. Return an JsonSchemaIdTakenError if
    * the given schema ID is already taken.
    *
    * @param jsonSchema
    *   The JSON Schema to create
    * @return
    *   ServiceResult value with either an Unit on success or the ServiceError
    */
  def create(jsonSchema: JsonSchemaModel): ServiceResult[Unit] = for {
    jsonSchemaO <- EitherT.right[ServiceError](jsonSchemaRepository.retrieve(jsonSchema.schemaId))
    _ <- EitherT.cond[IO](jsonSchemaO.isEmpty, (), JsonSchemaIdTakenError(jsonSchema.schemaId))
    _ <- EitherT.right[ServiceError](jsonSchemaRepository.create(jsonSchema))
  } yield ()

  /** Retrieve (select) the JSON Schema with the given schema ID. If the JSON Schema doesn't exist
    * return an error.
    *
    * @param schemaId
    *   The Schema ID
    * @return
    *   ServiceResult value with either an JsonSchema on success or the ServiceError on failure
    */
  def retrieve(schemaId: String): ServiceResult[JsonSchemaModel] =
    EitherT.fromOptionF(jsonSchemaRepository.retrieve(schemaId), JsonSchemaIdMissingError(schemaId))

  /** Validate a given JSON against a JSON Schema stored with a given Schema ID. If the schema with
    * the given ID is missing or the validation against that schema failed returns an error.
    *
    * @param json
    *   The JSON to be validated
    * @param schemaId
    *   The Schema ID of the JSON Schema used for the validation
    *
    * @return
    *   ServiceResult value with either an Unit on success or the ServiceError on failure
    */
  def validate(json: Json, schemaId: String): ServiceResult[Unit] = for {
    jsonSchema <- retrieve(schemaId)
    cleaned = json.deepDropNullValues
    _ <- EitherT.fromEither[IO](validateJsonAgainstSchema(cleaned, jsonSchema))
  } yield ()

  /** Validates a JSON instance against a JSON Schema. When the validation or parsing encounters an
    * exception a JsonValidationExceptionError with the message. If the schema has been processed
    * and deemed invalid a JsonValidationError will be returned with the error messages.
    *
    * @param json
    *   The JSON to be validated
    * @param jsonSchema
    *   The schema to be used for the validation
    * @return
    *   Either an Unit when successful or a ServiceError containing the reason for the validation
    *   failure
    */
  private def validateJsonAgainstSchema(
      json: Json,
      jsonSchema: JsonSchemaModel
  ): Either[ServiceError, Unit] = Try {
    val jsonSchemaJacksonJson    = JsonLoader.fromString(jsonSchema.schemaJson.toString())
    val validatedJsonJacksonJson = JsonLoader.fromString(json.toString())

    val factory = JsonSchemaFactory.byDefault

    val validatorJsonSchema = factory.getJsonSchema(jsonSchemaJacksonJson)

    // We use the deepCheck = true here to get all the errors found at once
    validatorJsonSchema.validate(validatedJsonJacksonJson, true).asInstanceOf[ListProcessingReport]
  }.toEither
    .leftMap(th => JsonValidationExceptionError(th.getMessage))
    .flatMap(report =>
      Either
        .cond(report.isSuccess, (), JsonValidationError(validationReportToErrorMessageList(report)))
    )

  // Transform the list report to a list of error messages
  // An alternative is to return the full report JSON by simply stopping at ".asJson()" and transforming it to a Circe Json
  private def validationReportToErrorMessageList(listProcessingReport: ListProcessingReport) =
    listProcessingReport.asJson().elements().asScala.toList.map(_.get("message").asText())
}
