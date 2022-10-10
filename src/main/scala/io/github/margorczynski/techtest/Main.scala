package io.github.margorczynski.techtest

import cats.effect.{ExitCode, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    Server.stream.compile.drain.as(ExitCode.Success)
}
