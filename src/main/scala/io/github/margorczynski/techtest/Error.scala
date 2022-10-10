package io.github.margorczynski.techtest

import cats.data.EitherT
import cats.effect.IO

sealed trait Error

object Error {
  type AppResult[T] = EitherT[IO, Error, T]
}
