package io.github.margorczynski.techtest.repository

import cats.effect.IO
import doobie.ConnectionIO
import io.github.margorczynski.techtest.model.JsonSchema
import doobie.implicits._
import doobie._
import doobie.util.transactor.Transactor
import doobie.Meta
import io.circe.Json
import io.circe._
import io.circe.parser._
import DbJsonSchemaRepository._

final class DbJsonSchemaRepository(transactor: Transactor[IO]) extends JsonSchemaRepository {

  def create(jsonSchema: JsonSchema): IO[Unit] =
    createQuery(jsonSchema)
      .run
      .transact(transactor)
      .void

  def retrieve(schemaId: String): IO[Option[JsonSchema]] =
    retrieveQuery(schemaId)
      .option
      .transact(transactor)
}

object DbJsonSchemaRepository {

  private[repository] def createQuery(jsonSchema: JsonSchema) =
    sql"""INSERT INTO
         json_schemas (
         schema_id,
         schema_json) values ($jsonSchema)"""
      .update

  private[repository] def retrieveQuery(schemaId: String): Query0[JsonSchema] =
    sql"""
         SELECT *
         FROM json_schemas
         WHERE schema_id = $schemaId
       """
      .query[JsonSchema]

  //Evidence to write/read the type by Doobie - .get here will throw which is the expected behaviour
  implicit val jsonMeta: Meta[Json] = Meta[String].timap(str => parse(str).toOption.get)(_.toString)
}