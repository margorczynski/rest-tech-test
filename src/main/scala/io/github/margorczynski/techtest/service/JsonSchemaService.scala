package io.github.margorczynski.techtest.service

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.github.margorczynski.techtest.model.JsonSchema
import io.github.margorczynski.techtest.repository.JsonSchemaRepository
import io.github.margorczynski.techtest.service.ServiceError._

final class JsonSchemaService(jsonSchemaRepository: JsonSchemaRepository) {

  /**
    * Create (insert) the given JSON Schema via the repository.
    * Return an JsonSchemaIdTakenError if the given schema ID is already taken.
    *
    * @param jsonSchema The JSON Schema to create
    * @return ServiceResult value with either an Unit on success or the ServiceError
    */
  def create(jsonSchema: JsonSchema): ServiceResult[Unit] = for {
    jsonSchemaO <- EitherT.right[ServiceError](jsonSchemaRepository.retrieve(jsonSchema.schemaId))
    _ <- EitherT.cond[IO](jsonSchemaO.isEmpty, (), JsonSchemaIdTakenError(jsonSchema.schemaId))
    _ <- EitherT.right[ServiceError](jsonSchemaRepository.create(jsonSchema))
  } yield ()

  /**
    * Retrieve (select) the JSON Schema with the given schema ID.
    * If the JSON Schema doesn't exist return an error.
    *
    * @param schemaId The Schema ID
    * @return ServiceResult value with either an JsonSchema on success or the ServiceError
    */
  def retrieve(schemaId: String): ServiceResult[JsonSchema] =
    EitherT.fromOptionF(jsonSchemaRepository.retrieve(schemaId), JsonSchemaIdMissingError(schemaId))

  def validateAgainstSchema(json: Json, schemaId: String): ServiceResult[Unit] = for {
    jsonSchema <- retrieve(schemaId)
    //TODO: Remove fields where value = null
    //TODO: Validate the json against the jsonSchema
  } yield ()
}