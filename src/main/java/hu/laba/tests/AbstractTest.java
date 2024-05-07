package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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
	public abstract Request buildRequest(int requestId);

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

	protected Request createVehicleTest(int requestId) {
		Request request = vehicleTracker.createVehicle(requestId, base, vehicleTracker.createNewRandomVehicle());
		validators.put(requestId,
			response -> vehicleTracker.validateCreateVehicleResponse(requestId, response,
				forwardInvalidResponseMessage(requestId, request, response)
			)
		);
		return request;
	}

	protected Optional<Request> getVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.getVehicle(requestId, base);
		optionalRequest.ifPresent(
			request -> validators.put(requestId,
				response -> vehicleTracker.validateGetVehicleResponse(requestId, response,
					forwardInvalidResponseMessage(requestId, request, response)
				)
			)
		);
		return optionalRequest;
	}

	protected void dumpInvalidResponse(int requestId, Request request, Response response, String responseBody, String message) {
		String output = "Response to request #%d was invalid: %s%n".formatted(requestId, message);
		output += "Request went to %s%n".formatted(request.url());
		output += "Request headers were: %s%n".formatted(response.headers());
		output += "Response status code was %d%n".formatted(response.code());
		output += "Response headers were: %s%n".formatted(response.headers());
		if (responseBody == null && response.body() != null) {
			try {
				responseBody = response.body().string();
			} catch (IOException exception) {
				System.err.println("Exception while trying to read response body: " + exception.getMessage());
			}
		}
		if (responseBody != null) {
			output += "Response body was:%n%s%n".formatted(responseBody);
		} else {
			output += "Response body was empty.\n";
		}
		if (dumpDirectory != null) {
			System.err.printf("Request #%d is invalid. See dump for details.%n", requestId);
			Path outputFilePath = dumpDirectory.resolve("%d.txt".formatted(requestId));
			try {
				if (Files.exists(outputFilePath)) {
					System.err.println("Overwriting existing dump file: " + outputFilePath);
				}
				Files.writeString(outputFilePath, output, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				System.err.println("Failed to write dump file to " + outputFilePath);
				System.err.print(output);
			}
		} else {
			System.err.print(output);
		}
	}

	protected BiConsumer<String, String> forwardInvalidResponseMessage(int requestId, Request request, Response response) {
		return (message, body) -> dumpInvalidResponse(requestId, request, response, body, message);
	}

}
