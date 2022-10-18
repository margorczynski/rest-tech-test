package io.github.margorczynski.techtest

import cats.effect.IO
import io.circe.Json
import io.circe.parser.parse
import org.http4s.{Entity, Method, Request}
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import io.circe.syntax.{EncoderOps, KeyOps}
import io.github.margorczynski.techtest.Route.RouteResponse
import io.github.margorczynski.techtest.RouteError.JsonDeserializationError

import scala.io.Source

class RouteSpec extends JsonSchemaServiceTestSuite {

  private val testRoutes = Route.routes(testService).orNotFound

  private def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ) = {

    assertIO(actual.map(_.status), expectedStatus)
    assertIO(actual.flatMap(_.body.compile.toVector.map(_.isEmpty)), expectedBody.isEmpty)

    expectedBody match {
      case Some(payload) =>
        assertIO(actual.flatMap(_.as[A]), payload)
      case None =>
        assertIO(actual.flatMap(_.body.compile.toVector.map(_.isEmpty)), true)
    }
  }

  private def generateOkJson(action: String, id: String) = Json.obj(
    "action" := action,
    "id"     := id,
    "status" := "success"
  )

  private def generateErrorJson(action: String, id: String, message: String) = Json.obj(
    "action"  := action,
    "id"      := id,
    "status"  := "error",
    "message" := message
  )

  private val notSerializableString = "abcd123 {}"

  test("create success") {
    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(testJson)

    val response = testRoutes.run(request)

    val expectedBody = generateOkJson("uploadSchema", testSchemaId)

    check(
      response,
      Status.Created,
      Some(expectedBody)
    )
  }

  test("create fail if isn't JSON serializable payload") {

    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(notSerializableString)

    val response = testRoutes.run(request)

    val expectedBody =
      generateErrorJson("uploadSchema", testSchemaId, "Malformed message body: Invalid JSON")

    check(
      response,
      Status.BadRequest,
      Some(expectedBody)
    )
  }

  test("download success") {

    val createRequest =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(testJson)
    val request =
      Request(method = Method.GET, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))

    val expectedBody = testJson

    // Create the test schema
    testRoutes.run(createRequest).unsafeRunSync()

    val response = testRoutes.run(request)

    check(
      response,
      Status.Ok,
      Some(expectedBody)
    )
  }

  test("download fail if JSON Schema missing") {

    val request =
      Request(method = Method.GET, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))

    val expectedBody =
      generateErrorJson(
        "downloadSchema",
        testSchemaId,
        s"Schema with ID $testSchemaId doesn't exist"
      )

    val response = testRoutes.run(request)

    check(
      response,
      Status.NotFound,
      Some(expectedBody)
    )
  }

  test("validate success") {
    val createRequest =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(testJson)
    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/validate/$testSchemaId"))
        .withEntity(okConfigJson)

    val expectedBody = generateOkJson("validateDocument", testSchemaId)

    testRoutes.run(createRequest).unsafeRunSync()

    val response = testRoutes.run(request)

    check(
      response,
      Status.Ok,
      Some(expectedBody)
    )
  }

  test("validate fail if JSON Schema missing") {
    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/validate/$testSchemaId"))
        .withEntity(okConfigJson)

    val expectedBody =
      generateErrorJson(
        "validateDocument",
        testSchemaId,
        s"Schema with ID $testSchemaId doesn't exist"
      )

    val response = testRoutes.run(request)

    check(
      response,
      Status.NotFound,
      Some(expectedBody)
    )
  }

  test("validate fail if isn't JSON serializable payload") {
    val createRequest =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(testJson)
    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/validate/$testSchemaId"))
        .withEntity(notSerializableString)

    val expectedBody =
      generateErrorJson("validateDocument", testSchemaId, "Malformed message body: Invalid JSON")

    testRoutes.run(createRequest).unsafeRunSync()

    val response = testRoutes.run(request)

    check(
      response,
      Status.BadRequest,
      Some(expectedBody)
    )
  }

  test("validate fail if doesn't conform to JSON Schema") {
    val Right(badConfigJson) = parse(Source.fromResource("service/config_wrong_type.json").mkString)

    val createRequest =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/schema/$testSchemaId"))
        .withEntity(testJson)
    val request =
      Request(method = Method.POST, uri = Uri.unsafeFromString(s"/validate/$testSchemaId"))
        .withEntity(badConfigJson)

    val errorMessage =
      "instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])"
    val expectedBody =
      generateErrorJson("validateDocument", testSchemaId, errorMessage)

    testRoutes.run(createRequest).unsafeRunSync()

    val response = testRoutes.run(request)

    check(
      response,
      Status.BadRequest,
      Some(expectedBody)
    )
  }
}
