val Http4sVersion          = "1.0.0-M35"
val DoobieVersion          = "1.0.0-RC1"
val SqliteVersion          = "3.39.3.0"
val FlywayVersion          = "9.4.0"
val CirceVersion           = "0.14.3"
val PureConfigVersion      = "0.17.1"
val JsonSchemaValidatorVersion = "2.2.14"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.4.3"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.margorczynski",
    name := "tech-test",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
    libraryDependencies ++= Seq(
      //http4s
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      //Doobie
      "org.tpolecat" %% "doobie-core"     % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % DoobieVersion,
      //SQLite
      "org.xerial" % "sqlite-jdbc" % SqliteVersion,
      //Flyway
      "org.flywaydb" % "flyway-core" % FlywayVersion,
      //Circe
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion,
      //PureConfig
      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-ip4s"        % PureConfigVersion,
      //json-schema-validator
      "com.github.java-json-tools" % "json-schema-validator" % JsonSchemaValidatorVersion,
      //Testing
      "org.scalameta"  %% "munit"               % MunitVersion           % Test,
      "org.typelevel"  %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "org.tpolecat"   %% "doobie-munit"       % DoobieVersion          % Test,
      "ch.qos.logback" % "logback-classic"      % LogbackVersion         % Runtime
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )