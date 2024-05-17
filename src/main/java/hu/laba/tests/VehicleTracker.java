package hu.laba.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;
import hu.laba.ResponseValidator;
import hu.laba.Vehicle;
import hu.laba.VehicleGenerator;
import okhttp3.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Singleton
public class VehicleTracker {

	private final AtomicInteger nextVehicleId = new AtomicInteger(0);
	public final Map<Integer, Vehicle> sentVehicles = new ConcurrentHashMap<>();
	public final Map<Integer, Integer> queriedVehicles = new ConcurrentHashMap<>();
	public final List<Vehicle> storedVehicles = new CopyOnWriteArrayList<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Inject
	public VehicleTracker() {}

	public Vehicle createNewRandomVehicle() {
		Vehicle vehicle = VehicleGenerator.generateRandom(nextVehicleId.getAndIncrement());
		while (sentVehicles.containsValue(vehicle) || storedVehicles.contains(vehicle)) {
			vehicle = VehicleGenerator.generateRandom(nextVehicleId.getAndIncrement());
		}
		return vehicle;
	}

	public Request createVehicleRequest(int requestId, String base, Vehicle vehicle) {
		sentVehicles.put(requestId, vehicle);
		return RequestBuilder.createVehicleRequest(base, vehicle.toCreateJsonString());
	}

	public void validateCreateVehicleResponse(RequestResponseContext context) {
		ResponseValidator.validateStatusCode(context, 201);
		String locationHeader = context.getResponse().header("Location");
		if (locationHeader == null) {
			context.addErrorMessage("missing location header");
			return;
		}
		String uuidString = locationHeader.replaceAll("^.*/jarmuvek/", "");
		try {
			UUID uuid = UUID.fromString(uuidString);
			Vehicle sentVehicle = sentVehicles.get(context.requestId);
			Vehicle storedVehicle = new Vehicle(uuid, sentVehicle.registration(), sentVehicle.owner(), sentVehicle.validity(), sentVehicle.data());
			storedVehicles.add(storedVehicle);
			sentVehicles.remove(context.requestId);
		} catch (Exception exception) {
			context.addErrorMessage(
				"invalid uuid (%s) returned. Check Location header. UUID string I tried to decode: %s"
					.formatted(exception.getMessage(), uuidString)
			);
		}
	}

	public Optional<Request> getVehicleRequest(int requestId, String base) {
		if (storedVehicles.isEmpty()) {
			return Optional.empty();
		}
		int vehicleId = requestId % storedVehicles.size();
		queriedVehicles.put(requestId, vehicleId);
		UUID uuid = storedVehicles.get(vehicleId).uuid();
		if (uuid == null) {
			throw new IllegalStateException("stored vehicles's UUID can not be null");
		}
		return Optional.of(RequestBuilder.getVehicleRequest(base, uuid));
	}

	public void validateGetVehicleResponse(RequestResponseContext context) {
		ResponseValidator.validateStatusCode(context, 200);
		ResponseValidator.validateBodyNotBlank(context);
		if (context.getResponseBody().isBlank()) {
			return;
		}
		try {
			String body = context.getResponseBody();
			Vehicle receivedVehicle = objectMapper.readValue(body, Vehicle.class);
			int vehicleId = queriedVehicles.remove(context.requestId);
			Vehicle expectedVehicle = storedVehicles.get(vehicleId);
			if (!expectedVehicle.equals(receivedVehicle)) {
				context.addErrorMessage(
					"invalid vehicle returned: RECEIVED: %s, EXPECTED: %s"
						.formatted(receivedVehicle.toString(), expectedVehicle.toString())
				);
			}
		} catch (Exception exception) {
			context.addErrorMessage("failed to parse vehicle: %s".formatted(exception.getMessage()));
		}
	}

	public Optional<Request> searchOneVehicleRequest(int requestId, String base) {
		if (storedVehicles.isEmpty()) {
			return Optional.empty();
		}
		int vehicleId = requestId % storedVehicles.size();
		queriedVehicles.put(requestId, vehicleId);
		return Optional.of(RequestBuilder.searchVehiclesRequest(base, storedVehicles.get(vehicleId).registration()));
	}

	public List<Vehicle> readListOfVehicles(RequestResponseContext context) {
		ResponseValidator.validateBodyNotBlank(context);
		if (context.getResponseBody().isBlank()) {
			context.addErrorMessage("missing list of vehicles");
			return null;
		}
		try {
			String body = context.getResponseBody();
			return objectMapper.readValue(body, new TypeReference<>() {});
		} catch (Exception exception) {
			context.addErrorMessage("failed to parse vehicles: %s".formatted(exception.getMessage()));
			return null;
		}
	}

	public void validateSearchVehicleResponse(RequestResponseContext context) {
		ResponseValidator.validateStatusCode(context, 200);
		List<Vehicle> receivedVehicles = readListOfVehicles(context);
		if (receivedVehicles == null) {
			return;
		}
		int vehicleId = queriedVehicles.remove(context.requestId);
		List<Vehicle> expectedVehicles = List.of(storedVehicles.get(vehicleId));
		if (!expectedVehicles.equals(receivedVehicles)) {
			context.addErrorMessage(
				"invalid list of vehicles returned: RECEIVED: %s, EXPECTED: %s"
					.formatted(receivedVehicles.toString(), expectedVehicles.toString())
			);
		}
	}

	public void validateCountVehiclesResponse(RequestResponseContext context, Function<Integer, Boolean> countValidatorFunction) {
		ResponseValidator.validateStatusCode(context, 200);
		ResponseValidator.validateBodyNotBlank(context);
		if (context.getResponseBody().isBlank()) {
			return;
		}
		try {
			String body = context.getResponseBody();
			Integer actualCount = Integer.parseInt(body);
			if (!countValidatorFunction.apply(actualCount)) {
				context.addErrorMessage("count of vehicles too different from expectation: got %d".formatted(actualCount));
			}
		} catch (Exception exception) {
			context.addErrorMessage("failed to parse result: %s".formatted(exception.getMessage()));
		}
	}

}
