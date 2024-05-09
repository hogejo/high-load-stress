package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import okhttp3.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractTest implements RequestBuilder, ResponseValidator {

	protected final String base;
	protected final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	protected final VehicleTracker vehicleTracker;
	protected final Path dumpDirectory;

	public AbstractTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
		this.dumpDirectory = dumpDirectory;
	}

	@Override
	public abstract String getDescription();

	@Override
	public abstract RequestResponseContext buildRequest(int requestId);

	@Override
	public void validateResponse(RequestResponseContext context) {
		validators.get(context.requestId).accept(context);
		if (!context.isValid()) {
			dumpInvalidResponse(context);
		}
	}

	protected RequestResponseContext createVehicleTest(int requestId) {
		Request request = vehicleTracker.createVehicleRequest(requestId, base, vehicleTracker.createNewRandomVehicle());
		validators.put(requestId, vehicleTracker::validateCreateVehicleResponse);
		return new RequestResponseContext(requestId, request);
	}

	protected Optional<RequestResponseContext> getVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.getVehicleRequest(requestId, base);
		if (optionalRequest.isPresent()) {
			validators.put(requestId, vehicleTracker::validateGetVehicleResponse);
			return Optional.of(new RequestResponseContext(requestId, optionalRequest.get()));
		}
		return Optional.empty();
	}

	protected void dumpInvalidResponse(RequestResponseContext context) {
		if (dumpDirectory != null) {
			System.err.printf("Request #%d is invalid. See dump for details.%n", context.requestId);
			String output = "== Response to request #%d was invalid. Reasons:%n".formatted(context.requestId);
			output += context.getErrorMessages().stream()
				.map(m -> "  " + m + "\n")
				.collect(Collectors.joining());
			// Request
			output += "== %s request went to %s%n".formatted(context.request.method(), context.request.url());
			output += "== Request headers were: %n%s%n".formatted(context.request.headers());
			// Response
			output += "== Response status code was %d%n".formatted(context.getResponse().code());
			output += "== Response headers were: %n%s%n".formatted(context.getResponse().headers());
			String responseBody = context.getResponseBody();
			if (!responseBody.isBlank()) {
				output += "== Response body was:%n%s%n".formatted(responseBody);
			} else {
				output += "== Response body was empty.\n";
			}
			Path outputFilePath = dumpDirectory.resolve("%d.txt".formatted(context.requestId));
			try {
				if (Files.exists(outputFilePath)) {
					System.err.println("Overwriting existing dump file: " + outputFilePath);
				}
				Files.writeString(outputFilePath, output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException exception) {
				System.err.printf("Failed to write dump file to %s: %s%n", outputFilePath, exception);
				System.err.print(output);
			}
		} else {
			System.err.printf("Request #%d is invalid. Enable dumping for details.%n", context.requestId);
		}
	}

}
