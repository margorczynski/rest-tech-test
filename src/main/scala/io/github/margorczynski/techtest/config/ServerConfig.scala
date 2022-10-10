package io.github.margorczynski.techtest.config

import com.comcast.ip4s.{Hostname, Port}

case class ServerConfig(
    hostname: Hostname,
    port: Port
)
