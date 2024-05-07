package hu.laba.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.Vehicle;
import hu.laba.VehicleGenerator;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class VehicleTracker {

	private final AtomicInteger nextVehicleId = new AtomicInteger(0);
	public final Map<Integer, Vehicle> sentVehicles = new ConcurrentHashMap<>();
	public final Map<Integer, Integer> queriedVehicles = new ConcurrentHashMap<>();
	public final List<Vehicle> storedVehicles = new CopyOnWriteArrayList<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public Vehicle createNewRandomVehicle() {
		Vehicle vehicle = VehicleGenerator.generateRandom(nextVehicleId.getAndIncrement());
		while (sentVehicles.containsValue(vehicle) || storedVehicles.contains(vehicle)) {
			vehicle = VehicleGenerator.generateRandom(nextVehicleId.getAndIncrement());
		}
		return vehicle;
	}

	public Request createVehicle(int requestId, String base, Vehicle vehicle) {
		sentVehicles.put(requestId, vehicle);
		return RequestBuilder.createVehicleRequest(base, vehicle.toCreateJsonString());
	}

	public boolean validateCreateVehicleResponse(int requestId, Response response, Consumer<String> messageConsumer) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 201, messageConsumer)
			|| !ResponseValidator.validateBodyNotNull(requestId, response, messageConsumer)) {
			return false;
		}
		String locationHeader = response.header("Location");
		if (locationHeader == null) {
			messageConsumer.accept("missing location header");
			return false;
		}
		String uuidString = locationHeader.replaceAll("^.*/jarmuvek/", "");
		try {
			UUID uuid = UUID.fromString(uuidString);
			Vehicle sentVehicle = sentVehicles.get(requestId);
			Vehicle storedVehicle = new Vehicle(uuid, sentVehicle.registration(), sentVehicle.owner(), sentVehicle.validity(), sentVehicle.data());
			storedVehicles.add(storedVehicle);
			sentVehicles.remove(requestId);
		} catch (Exception exception) {
			messageConsumer.accept("invalid uuid (%s) returned. Check Location header. UUID string I tried to decode: %s".formatted(exception.getMessage(), uuidString));
			return false;
		}
		return true;
	}

	public Optional<Request> getVehicle(int requestId, String base) {
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

	public boolean validateGetVehicleResponse(int requestId, Response response, Consumer<String> messageConsumer) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200, messageConsumer)
			|| !ResponseValidator.validateBodyNotNull(requestId, response, messageConsumer)) {
			return false;
		}
		assert response.body() != null;
		try {
			String body = response.body().string();
			Vehicle receivedVehicle = objectMapper.readValue(body, Vehicle.class);
			int vehicleId = queriedVehicles.remove(requestId);
			Vehicle expectedVehicle = storedVehicles.get(vehicleId);
			if (!expectedVehicle.equals(receivedVehicle)) {
				messageConsumer.accept("invalid vehicle returned: RECEIVED: %s, EXPECTED: %s".formatted(receivedVehicle, expectedVehicle));
				return false;
			}
		} catch (Exception exception) {
			messageConsumer.accept("failed to parse vehicle: %s".formatted(exception.getMessage()));
			return false;
		}
		return true;
	}

	public Optional<Request> searchOneVehicle(int requestId, String base) {
		if (storedVehicles.isEmpty()) {
			return Optional.empty();
		}
		int vehicleId = requestId % storedVehicles.size();
		queriedVehicles.put(requestId, vehicleId);
		return Optional.of(RequestBuilder.searchVehiclesRequest(base, storedVehicles.get(vehicleId).registration()));
	}

	public boolean validateSearchVehicleResponse(int requestId, Response response, Consumer<String> messageConsumer) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200, messageConsumer)
			|| !ResponseValidator.validateBodyNotNull(requestId, response, messageConsumer)) {
			return false;
		}
		assert response.body() != null;
		try {
			String body = response.body().string();
			List<Vehicle> receivedVehicles = objectMapper.readValue(body, new TypeReference<>() {});
			int vehicleId = queriedVehicles.remove(requestId);
			List<Vehicle> expectedVehicles = List.of(storedVehicles.get(vehicleId));
			if (!expectedVehicles.equals(receivedVehicles)) {
				messageConsumer.accept("invalid list of vehicles returned: RECEIVED: %s, EXPECTED: %s".formatted(receivedVehicles, expectedVehicles));
				return false;
			}
		} catch (Exception exception) {
			messageConsumer.accept("failed to parse vehicle: %s".formatted(exception.getMessage()));
			return false;
		}
		return true;
	}

	public boolean validateCountVehiclesResponse(int requestId, Response response, Function<Integer, Boolean> countValidatorFunction, Consumer<String> messageConsumer) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200, messageConsumer)
			|| !ResponseValidator.validateBodyNotNull(requestId, response, messageConsumer)) {
			return false;
		}
		assert response.body() != null;
		try {
			Integer actualCount = Integer.parseInt(response.body().string());
			if (!countValidatorFunction.apply(actualCount)) {
				messageConsumer.accept("count of vehicles too different from expectation: got %d".formatted(actualCount));
				return false;
			}
		} catch (Exception exception) {
			messageConsumer.accept("failed to parse result: %s".formatted(exception.getMessage()));
			return false;
		}
		return true;
	}

}
