package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import hu.laba.Vehicle;
import hu.laba.VehicleGenerator;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class StartTest implements RequestBuilder, ResponseValidator {

	private final String base;
	private final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	private final VehicleTracker vehicleTracker;

	public StartTest(String base, VehicleTracker vehicleTracker) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
	}

	@Override
	public String getDescription() {
		return "StartTest(create, get, search, invalidCreate, invalidGet, invalidSearch)";
	}

	private Request createVehicleTest(int requestId) {
		validators.put(requestId, response -> vehicleTracker.validateCreateVehicleResponse(requestId, response));
		return vehicleTracker.createVehicle(requestId, base, vehicleTracker.createNewRandomVehicle());
	}

	private Request getVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.getVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, response -> vehicleTracker.validateGetVehicleResponse(requestId, response));
			return optionalRequest.get();
		}
	}

	private Request searchVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, response -> vehicleTracker.validateSearchVehicleResponse(requestId, response));
			return optionalRequest.get();
		}
	}

	private Request invalidCreateVehicleTest(int requestId) {
		validators.put(requestId, response -> {
			int code = response.code();
			if (code / 100 != 4) {
				System.err.printf("Request#%d: expected 4xx for invalid create vehicle request, got %d%n", requestId, code);
				return false;
			}
			return true;
		});
		Vehicle vehicle = VehicleGenerator.generateRandom(requestId);
		// No read after this, so why store?
		//vehicleTracker.sentVehicles.put(requestId, vehicle);
		String badSyntaxVehicleString = vehicle.toJsonString().replace("{", "");
		int r = Math.abs(ThreadLocalRandom.current().nextInt()) % 4;
		return RequestBuilder.createVehicleRequest(
			base,
			switch (r) {
				case 0 -> badSyntaxVehicleString;
				case 1 ->
					new Vehicle(vehicle.uuid(), "", vehicle.owner(), vehicle.validity(), vehicle.data()).toCreateJsonString();
				case 2 ->
					new Vehicle(vehicle.uuid(), vehicle.registration(), vehicle.owner(), vehicle.validity(), null).toCreateJsonString();
				case 3 ->
					vehicleTracker.storedVehicles.isEmpty() ? badSyntaxVehicleString : vehicleTracker.storedVehicles.get(requestId % vehicleTracker.storedVehicles.size()).toCreateJsonString();
				default -> throw new IllegalStateException("Unexpected value: " + r);
			}
		);
	}

	private Request invalidGetVehicleTest(int requestId) {
		validators.put(requestId, response -> {
			if (response.code() != 404) {
				System.err.printf("Request #%d: expected 404 for missing vehicle get request, got %d%n", requestId, response.code());
				return false;
			}
			return true;
		});
		return RequestBuilder.getVehicleRequest(base, UUID.randomUUID());
	}

	private Request invalidSearchVehicleTest(int requestId) {
		validators.put(requestId, response -> {
			if (response.code() != 400) {
				System.err.printf("Request #%d: expected 400 for bad search request, got %d%n", requestId, response.code());
				return false;
			}
			return true;
		});
		return new Request.Builder()
			.get()
			.url(base + "/kereses")
			.build();
	}

	@Override
	public Request buildRequest(int requestId) {
		return switch (requestId % 6) {
			case 0 -> createVehicleTest(requestId);
			case 1 -> getVehicleTest(requestId);
			case 2 -> searchVehicleTest(requestId);
			case 3 -> invalidCreateVehicleTest(requestId);
			case 4 -> invalidGetVehicleTest(requestId);
			case 5 -> invalidSearchVehicleTest(requestId);
			default -> throw new IllegalStateException("Unexpected value: " + requestId % 4);
		};
	}

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

}
