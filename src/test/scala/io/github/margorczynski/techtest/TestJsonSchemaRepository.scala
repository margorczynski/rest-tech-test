package io.github.margorczynski.techtest

import cats.effect.IO
import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.repository.JsonSchemaRepository

final class TestJsonSchemaRepository extends JsonSchemaRepository {

  type SchemaId = String

  private val storageMap = scala.collection.mutable.Map.empty[SchemaId, JsonSchemaModel]

  def create(jsonSchema: JsonSchemaModel): IO[Unit] =
    IO.pure(storageMap += (jsonSchema.schemaId -> jsonSchema))

  def retrieve(schemaId: SchemaId): IO[Option[JsonSchemaModel]] =
    IO.pure(storageMap.get(schemaId))

  def clear(): Unit = storageMap.clear()
}