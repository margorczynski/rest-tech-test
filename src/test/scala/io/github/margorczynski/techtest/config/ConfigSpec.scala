package io.github.margorczynski.techtest.config

import com.comcast.ip4s.IpLiteralSyntax
import munit.CatsEffectSuite
import pureconfig.error.ConfigReaderException

class ConfigSpec extends CatsEffectSuite {

  test("valid-config") {

    val expected = Config(
      DatabaseConfig(
        "org.sqlite.JDBC",
        "jdbc:sqlite:tech_test.db",
        "",
        "",
        32
      ),
      ServerConfig(
        host"0.0.0.0",
        port"8080"
      )
    )

    Config.load("config/valid_config.conf").assertEquals(expected)
  }

  test("missing-config") {
    Config.load("config/some_config.conf").intercept[ConfigReaderException[Config]]
  }

  test("invalid-config-missing-field") {
    Config.load("config/invalid_config_missing_field.conf").intercept[ConfigReaderException[Config]]
  }

  test("invalid-config-wrong-hostname") {
    Config.load("config/invalid_config_hostname.conf").intercept[ConfigReaderException[Config]]
  }
}
