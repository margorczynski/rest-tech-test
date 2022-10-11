package io.github.margorczynski.techtest.config

import cats.effect.IO
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
//Need to import pureconfig.module.ip4s._, IDEA removes
import pureconfig.module.ip4s._

case class Config(
    database: DatabaseConfig,
    server: ServerConfig
)

object Config {

  /** Load a configuration based on a resource file with a given filename
    *
    * @param configFilename
    *   The filename of the config resource
    * @return
    *   IO that will load the configuration on execution
    */
  def load(configFilename: String): IO[Config] = {
    ConfigSource.resources(configFilename).loadF[IO, Config]()
  }
}
