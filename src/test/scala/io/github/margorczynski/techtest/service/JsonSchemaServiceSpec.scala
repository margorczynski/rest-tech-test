package io.github.margorczynski.techtest.service

import io.github.margorczynski.techtest.TestJsonSchemaRepository
import munit.CatsEffectSuite
import io.circe._
import io.circe.parser._
import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.service.ServiceError.{JsonSchemaIdMissingError, JsonSchemaIdTakenError, JsonValidationError}

import scala.io.Source

class JsonSchemaServiceSpec extends CatsEffectSuite {

  private val testRepo = new TestJsonSchemaRepository()
  private val testService = new JsonSchemaService(testRepo)

  //Fail fast, no point in handling the Either
  private val Right(testJson) =
    parse(Source.fromResource("service/config_schema.json").mkString)

  private val testSchemaId = "config_schema"

  private val testJsonSchema =
    JsonSchemaModel(testSchemaId, testJson)

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
    val Right(configJson) = parse(Source.fromResource("service/config.json").mkString)

    //Before validation the schema must exist in the storage
    testService.create(testJsonSchema).value.unsafeRunSync()

    testService.validate(configJson, testSchemaId).value.assertEquals(Right(()))
  }

  test("validate fail if schema missing") {
    val Right(configJson) = parse(Source.fromResource("service/config.json").mkString)

    testService.validate(configJson, testSchemaId).value.assertEquals(Left(JsonSchemaIdMissingError(testSchemaId)))
  }

  test("validate fail for empty JSON") {
    val Right(configJson) = parse(Source.fromResource("service/empty.json").mkString)
    val errorMessages = List("object has missing required properties ([\"destination\",\"source\"])")

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService.validate(configJson, testSchemaId).value.assertEquals(Left(JsonValidationError(errorMessages)))
  }

  test("validate fail for wrong type") {
    val Right(configJson) = parse(Source.fromResource("service/config_wrong_type.json").mkString)
    val errorMessages = List("instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])")

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService.validate(configJson, testSchemaId).value.assertEquals(Left(JsonValidationError(errorMessages)))
  }

  test("validate fail for missing field and wrong type") {
    val Right(configJson) = parse(Source.fromResource("service/config_missing_field_wrong_type.json").mkString)
    val errorMessages = List(
      "object has missing required properties ([\"destination\"])",
      "instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])"
    )

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService.validate(configJson, testSchemaId).value.assertEquals(Left(JsonValidationError(errorMessages)))
  }
}