package io.github.margorczynski.techtest.repository

import cats.effect.IO
import doobie.ConnectionIO
import io.github.margorczynski.techtest.model.JsonSchemaModel
import doobie.implicits._
import doobie._
import doobie.util.transactor.Transactor
import doobie.Meta
import io.circe.Json
import io.circe._
import io.circe.parser._
import DbJsonSchemaRepository._

/**
  * The Doobie RDBMS repository implementation.
  * We don't handle here the exceptions (e.g. PKey collision - Schema ID) as we expect the logic/service layer should
  * never let that happen - if so then it should be treated as a bug in the upper layer.
  * Thanks to this we don't depend on the clunky DB exception model and we don't have to map it.
  *
  * @param transactor Transactor to be used for executing the queries
  */
final class DbJsonSchemaRepository(transactor: Transactor[IO]) extends JsonSchemaRepository {

  def create(jsonSchema: JsonSchemaModel): IO[Unit] =
    createQuery(jsonSchema)
      .run
      .transact(transactor)
      .void

  def retrieve(schemaId: String): IO[Option[JsonSchemaModel]] =
    retrieveQuery(schemaId)
      .option
      .transact(transactor)
}

object DbJsonSchemaRepository {

  private[repository] def createQuery(jsonSchema: JsonSchemaModel) =
    sql"""INSERT INTO
         json_schemas (
         schema_id,
         schema_json) values ($jsonSchema)"""
      .update

  private[repository] def retrieveQuery(schemaId: String): Query0[JsonSchemaModel] =
    sql"""
         SELECT *
         FROM json_schemas
         WHERE schema_id = $schemaId
       """
      .query[JsonSchemaModel]

  //Evidence to write/read the type by Doobie - .get here will throw which is the expected behaviour
  implicit val jsonMeta: Meta[Json] = Meta[String].timap(str => parse(str).toOption.get)(_.toString)
}