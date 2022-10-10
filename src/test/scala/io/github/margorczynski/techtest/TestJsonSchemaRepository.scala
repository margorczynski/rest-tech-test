package io.github.margorczynski.techtest

import cats.effect.IO
import io.github.margorczynski.techtest.model.JsonSchema
import io.github.margorczynski.techtest.repository.JsonSchemaRepository

final class TestJsonSchemaRepository extends JsonSchemaRepository {

  type SchemaId = String

  private val storageMap = scala.collection.mutable.Map.empty[SchemaId, JsonSchema]

  def create(jsonSchema: JsonSchema): IO[Unit] =
    IO.pure(storageMap += (jsonSchema.schemaId -> jsonSchema))

  def retrieve(schemaId: SchemaId): IO[Option[JsonSchema]] =
    IO.pure(storageMap.get(schemaId))

  def clear(): Unit = storageMap.clear()
}