package io.github.margorczynski.techtest

import cats.Show
import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import io.github.margorczynski.techtest.service.ServiceError
import org.http4s.DecodeFailure

sealed trait RouteError

object RouteError {

  type RouteResult[T] = EitherT[IO, RouteError, T]

  case class RouteServiceError(serviceError: ServiceError)          extends RouteError
  case class JsonDeserializationError(decodeFailure: DecodeFailure) extends RouteError

  implicit val routeErrorShow: Show[RouteError] = Show.show {
    case RouteServiceError(serviceError)         => serviceError.show
    case JsonDeserializationError(decodeFailure) => decodeFailure.message
  }
}
