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

	public boolean validateCreateVehicleResponse(int requestId, Response response) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 201) || !ResponseValidator.validateBodyNotNull(requestId, response)) {
			return false;
		}
		String locationHeader = response.header("Location");
		if (locationHeader == null) {
			System.err.printf("Request #%d: missing location header%n", requestId);
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
			System.err.printf("Request #%d: invalid uuid returned: %s%n", requestId, exception);
			System.err.println("Location header was: '" + locationHeader + "'");
			System.err.println("UUID string I tried to decode: '" + uuidString + "'");
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
			throw new RuntimeException("stored vehicles's UUID can not be null");
		}
		return Optional.of(RequestBuilder.getVehicleRequest(base, uuid));
	}

	public boolean validateGetVehicleResponse(int requestId, Response response) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200) || !ResponseValidator.validateBodyNotNull(requestId, response)) {
			return false;
		}
		assert response.body() != null;
		try {
			String body = response.body().string();
			Vehicle receivedVehicle = objectMapper.readValue(body, Vehicle.class);
			int vehicleId = queriedVehicles.remove(requestId);
			Vehicle expectedVehicle = storedVehicles.get(vehicleId);
			if (!expectedVehicle.equals(receivedVehicle)) {
				System.err.printf("Request #%d: invalid vehicle returned:%n  %s received%n  %s expected%n", requestId, receivedVehicle.toString(), expectedVehicle);
				System.err.printf("Request #%d: response body was: %s%n", requestId, body);
				return false;
			}
		} catch (Exception ignored) {
			System.err.printf("Request #%d: failed to parse vehicle%n", requestId);
			return false;
		}
		return true;
	}

	public Optional<Request> searchVehicle(int requestId, String base) {
		if (storedVehicles.isEmpty()) {
			return Optional.empty();
		}
		int vehicleId = requestId % storedVehicles.size();
		queriedVehicles.put(requestId, vehicleId);
		return Optional.of(RequestBuilder.searchVehiclesRequest(base, storedVehicles.get(vehicleId).registration()));
	}

	public boolean validateSearchVehicleResponse(int requestId, Response response) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200) || !ResponseValidator.validateBodyNotNull(requestId, response)) {
			return false;
		}
		assert response.body() != null;
		try {
			String body = response.body().string();
			List<Vehicle> receivedVehicles = objectMapper.readValue(body, new TypeReference<>() {});
			int vehicleId = queriedVehicles.remove(requestId);
			List<Vehicle> expectedVehicles = List.of(storedVehicles.get(vehicleId));
			if (!expectedVehicles.equals(receivedVehicles)) {
				System.err.printf("Request #%d: invalid list of vehicles returned:%n  %s received%n  %s expected%n", requestId, receivedVehicles.toString(), expectedVehicles);
				return false;
			}
		} catch (Exception ignored) {
			System.err.printf("Request #%d: failed to parse vehicle%n", requestId);
			return false;
		}
		return true;
	}

	public boolean validateCountVehiclesResponse(int requestId, Response response, Function<Integer, Boolean> countValidatorFunction) {
		if (!ResponseValidator.validateStatusCode(requestId, response, 200) || !ResponseValidator.validateBodyNotNull(requestId, response)) {
			return false;
		}
		assert response.body() != null;
		try {
			Integer actualCount = Integer.parseInt(response.body().string());
			if (!countValidatorFunction.apply(actualCount)) {
				System.err.printf("Request #%d: count of vehicles too different from expectation: got %d%n", requestId, actualCount);
				return false;
			}
		} catch (Exception ignored) {
			System.err.printf("Request #%d: failed to parse result%n", requestId);
			return false;
		}
		return true;
	}

}
