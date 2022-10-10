package io.github.margorczynski.techtest.config

case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    threadPoolSize: Int
)
