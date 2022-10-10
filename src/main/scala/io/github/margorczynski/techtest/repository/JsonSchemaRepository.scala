package io.github.margorczynski.techtest.repository

import cats.effect.IO
import io.github.margorczynski.techtest.model.JsonSchema

/**
  * Trait to abstract over the storage for the JsonSchema.
  * This abstraction is needed for testing as using any RDBMS specific functions (e.g. json type in PG) makes it
  * unviable to test it with in-mem SQLite for example
  */
trait JsonSchemaRepository {
  def create(jsonSchema: JsonSchema): IO[Unit]
  def retrieve(schemaId: String): IO[Option[JsonSchema]]
}