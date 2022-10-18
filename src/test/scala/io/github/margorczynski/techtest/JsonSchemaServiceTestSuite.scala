package io.github.margorczynski.techtest

import io.github.margorczynski.techtest.model.JsonSchemaModel
import io.github.margorczynski.techtest.service.JsonSchemaService
import munit.CatsEffectSuite
import io.circe.parser._

import scala.io.Source

trait JsonSchemaServiceTestSuite extends CatsEffectSuite {

  private val testRepo = new TestJsonSchemaRepository()

  // Fail fast, no point in handling the Either
  val Right(testJson) =
    parse(Source.fromResource("service/config_schema.json").mkString)

  val Right(okConfigJson) = parse(Source.fromResource("service/config.json").mkString)

  val testService = new JsonSchemaService(testRepo)

  val testSchemaId: String = "config_schema"

  val testJsonSchema: JsonSchemaModel =
    JsonSchemaModel(testSchemaId, testJson)

  override def beforeEach(context: BeforeEach): Unit =
    testRepo.clear()
}
