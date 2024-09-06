# high-load-stress

Official stress test suite for "High Load Software Architectures" online class.

## Running

You can run the application from the flat jar file or with Docker.

For the jar file, you must have Java 21 or later installed:

```bash
java -jar high-load-stress-8.3.1.jar
```

For Docker, you do not need to have Java installed. Just load and run the image:

```bash
docker load -i high-load-stress-8.3.1.tar
docker run -P high-load-stress:8.3.1 --listScenarios
```

## Usage

You can run the default/official test suite against `localhost:8080` with no arguments.

You can enable dumping of failed responses with `--dump`.

You can run custom test scenarios with the `--scenario` or `--scenarios` options.
List the available scenarios with `--listScenarios`.

Use `--help` for more usage information.
