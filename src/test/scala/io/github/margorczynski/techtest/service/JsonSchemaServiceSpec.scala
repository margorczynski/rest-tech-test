package io.github.margorczynski.techtest.service

import io.circe.parser._
import io.github.margorczynski.techtest.{JsonSchemaServiceTestSuite, TestJsonSchemaRepository}
import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.service.ServiceError.{
  JsonSchemaIdMissingError,
  JsonSchemaIdTakenError,
  JsonValidationError
}
import munit.CatsEffectSuite

import scala.io.Source

class JsonSchemaServiceSpec extends JsonSchemaServiceTestSuite {
  test("create success") {
    testService.create(testJsonSchema).value.assertEquals(Right(()))
  }

  test("create fail when schema ID taken") {
    testService.create(testJsonSchema).value.unsafeRunSync()
    testService
      .create(testJsonSchema)
      .value
      .assertEquals(Left(JsonSchemaIdTakenError(testSchemaId)))
  }

  test("retrieve success") {
    testService.create(testJsonSchema).value.unsafeRunSync()
    testService.retrieve(testSchemaId).value.assertEquals(Right(testJsonSchema))
  }

  test("retrieve fail when JSON Schema missing") {
    testService
      .retrieve(testSchemaId)
      .value
      .assertEquals(Left(JsonSchemaIdMissingError(testSchemaId)))
  }

  test("validate success") {
    // Before validation the schema must exist in the storage
    testService.create(testJsonSchema).value.unsafeRunSync()

    testService.validate(okConfigJson, testSchemaId).value.assertEquals(Right(()))
  }

  test("validate fail if schema missing") {
    testService
      .validate(okConfigJson, testSchemaId)
      .value
      .assertEquals(Left(JsonSchemaIdMissingError(testSchemaId)))
  }

  test("validate fail for empty JSON") {
    val Right(configJson) = parse(Source.fromResource("service/empty.json").mkString)
    val errorMessages =
      List("object has missing required properties ([\"destination\",\"source\"])")

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService
      .validate(configJson, testSchemaId)
      .value
      .assertEquals(Left(JsonValidationError(errorMessages)))
  }

  test("validate fail for wrong type") {
    val Right(configJson) = parse(Source.fromResource("service/config_wrong_type.json").mkString)
    val errorMessages = List(
      "instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])"
    )

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService
      .validate(configJson, testSchemaId)
      .value
      .assertEquals(Left(JsonValidationError(errorMessages)))
  }

  test("validate fail for missing field and wrong type") {
    val Right(configJson) =
      parse(Source.fromResource("service/config_missing_field_wrong_type.json").mkString)
    val errorMessages = List(
      "object has missing required properties ([\"destination\"])",
      "instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])"
    )

    testService.create(testJsonSchema).value.unsafeRunSync()

    testService
      .validate(configJson, testSchemaId)
      .value
      .assertEquals(Left(JsonValidationError(errorMessages)))
  }
}
