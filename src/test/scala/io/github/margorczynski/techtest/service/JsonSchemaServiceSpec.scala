package io.github.margorczynski.techtest.service

import io.github.margorczynski.techtest.TestJsonSchemaRepository
import munit.CatsEffectSuite
import io.circe._
import io.circe.parser._
import io.github.margorczynski.techtest.model.JsonSchema
import io.github.margorczynski.techtest.service.ServiceError.{JsonSchemaIdMissingError, JsonSchemaIdTakenError}

import scala.io.Source

class JsonSchemaServiceSpec extends CatsEffectSuite {

  private val testRepo = new TestJsonSchemaRepository()
  private val testService = new JsonSchemaService(testRepo)

  //Fail fast, no point in handling the Either
  private val Right(testJson) =
    parse(Source.fromResource("config_schema.json").mkString)

  private val testSchemaId = "config_schema"

  private val testJsonSchema =
    JsonSchema(testSchemaId, testJson)

  override def beforeEach(context: BeforeEach): Unit =
    testRepo.clear()

  test("create success") {
    testService.create(testJsonSchema).value.assertEquals(Right(()))
  }

  test("create fail when schema ID taken") {
    testService.create(testJsonSchema).value.unsafeRunSync()
    testService.create(testJsonSchema).value.assertEquals(Left(JsonSchemaIdTakenError(testSchemaId)))
  }

  test("retrieve success") {
    testService.create(testJsonSchema).value.unsafeRunSync()
    testService.retrieve(testSchemaId).value.assertEquals(Right(testJsonSchema))
  }

  test("retrieve fail when JSON Schema missing") {
    testService.retrieve(testSchemaId).value.assertEquals(Left(JsonSchemaIdMissingError(testSchemaId)))
  }

  test("validate success") {

  }
}