# high-load-stress

Official stress test suite for "High Load Software Architectures" online class.

## Running

You can run the application from the flat jar file or with Docker.

### Java 21

For the jar file, you must have Java 21 or later installed:

```bash
java -jar high-load-stress-8.6.0.jar
```

### Docker

For Docker, you do not need to have Java installed. Just load and run the image:

```bash
docker load -i high-load-stress-8.6.0.tar
docker run high-load-stress:8.6.0 --listScenarios
```

### Docker networking

With Docker, you might need extra arguments to connect to your desired endpoint:

If the service you are testing also runs in Docker, the tester should be able to connect to it:

```bash
docker run high-load-stress:8.6.0 --endpoint other-container-address:8080
```

If the service you are testing runs on the host, you need to use host networking:

```bash
docker run --network host high-load-stress:8.6.0 --endpoint localhost:8080
```

## Usage

You can run the default/official test suite against `localhost:8080` with no arguments.

You can enable dumping of failed responses with `--dump`.

You can run custom test scenarios with the `--scenario` or `--scenarios` options.
List the available scenarios with `--listScenarios`.

Use `--help` for more usage information.
