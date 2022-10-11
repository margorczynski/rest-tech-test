package io.github.margorczynski.techtest.model

import io.circe.{Encoder, Json}

case class JsonSchemaModel(schemaId: String, schemaJson: Json)

object JsonSchemaModel {

  implicit val jsonSchemaModelEncoder: Encoder[JsonSchemaModel] = Encoder.instance(_.schemaJson)
}
