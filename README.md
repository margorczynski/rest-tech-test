# JSON Validation Service
The goal of the application is to provide a web server containing endpoints that will
allow validation of JSON objects against a given JSON Schema (v4).

The uploaded schemas will be persisted in a local SQLite database by default
but the location or even the type of database backend used is configurable if the project is ran with the necessary
JDBC driver in the classpath.

Creation of the DB table is done automatically via a Flyway migration, found in the resources directory.

### Configuration
The application can be configured by modifying the `application.conf` file in the resources directory.

The possible configuration values are as follows:
- database
  - driver - the name of the driver class used for connecting to the DB
  - url - JDBC url to the DB
  - user - username to connect to the DB
  - password - password to connect to the DB
  - thread-pool-size - the size of the JDBC connection thread pool
- server
  - hostname - the hostname to be used by the server
  - port - the port on which the server will listen

### How to run it
The application can be ran by simply executing `sbt run` in it's directory.
For packaging to e.g. an universal package or a Docker image just add the sbt-native-packager plugins as needed.

### Unit Test execution
The testing suite covers all of the service and HTTP route logic along the most common scenarios.
You can run the whole suite with `sbt test`