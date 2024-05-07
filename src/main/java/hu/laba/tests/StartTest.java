package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.Vehicle;
import hu.laba.VehicleGenerator;
import okhttp3.Request;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StartTest extends AbstractTest {

	public StartTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "StartTest(create, get, search, invalidCreate, invalidGet, invalidSearch)";
	}

	private Request getVehicleTestOrCreate(int requestId) {
		return getVehicleTest(requestId).orElse(createVehicleTest(requestId));
	}

	private Request searchVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, response -> vehicleTracker.validateSearchVehicleResponse(requestId, response, forwardInvalidResponseMessage(requestId, optionalRequest.get(), response)));
			return optionalRequest.get();
		}
	}

	private Request invalidCreateVehicleTest(int requestId) {
		Vehicle vehicle = VehicleGenerator.generateRandom(requestId);
		// No read after this, so why store?
		//vehicleTracker.sentVehicles.put(requestId, vehicle);
		String badSyntaxVehicleString = vehicle.toJsonString().replace("{", "");
		int r = Math.abs(ThreadLocalRandom.current().nextInt()) % 4;
		Request request = RequestBuilder.createVehicleRequest(
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
		validators.put(requestId, response -> {
			int code = response.code();
			if (code / 100 != 4) {
				dumpInvalidResponse(requestId, request, response, "expected 4xx for invalid create vehicle request, got %d".formatted(code));
				return false;
			}
			return true;
		});
		return request;
	}

	private Request invalidGetVehicleTest(int requestId) {
		Request request = RequestBuilder.getVehicleRequest(base, UUID.randomUUID());
		validators.put(requestId, response -> {
			if (response.code() != 404) {
				dumpInvalidResponse(requestId, request, response, "expected 404 for missing vehicle get request, got %d".formatted(response.code()));
				return false;
			}
			return true;
		});
		return request;
	}

	private Request invalidSearchVehicleTest(int requestId) {
		Request request = new Request.Builder()
			.get()
			.url(base + "/kereses")
			.build();
		validators.put(requestId, response -> ResponseValidator.validateStatusCode(requestId, response, 400, forwardInvalidResponseMessage(requestId, request, response)));
		return request;
	}

	@Override
	public Request buildRequest(int requestId) {
		return switch (requestId % 6) {
			case 0 -> createVehicleTest(requestId);
			case 1 -> getVehicleTestOrCreate(requestId);
			case 2 -> searchVehicleTest(requestId);
			case 3 -> invalidCreateVehicleTest(requestId);
			case 4 -> invalidGetVehicleTest(requestId);
			case 5 -> invalidSearchVehicleTest(requestId);
			default -> throw new IllegalStateException("Unexpected value: " + requestId % 4);
		};
	}

}
