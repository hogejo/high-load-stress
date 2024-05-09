package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;
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

	private RequestResponseContext getVehicleTestOrCreate(int requestId) {
		return getVehicleTest(requestId).orElseGet(() -> createVehicleTest(requestId));
	}

	private RequestResponseContext searchVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicleRequest(requestId, base);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, vehicleTracker::validateSearchVehicleResponse);
			return new RequestResponseContext(requestId, optionalRequest.get());
		}
	}

	private RequestResponseContext invalidCreateVehicleTest(int requestId) {
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
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 4));
		return new RequestResponseContext(requestId, request);
	}

	private RequestResponseContext invalidGetVehicleTest(int requestId) {
		Request request = RequestBuilder.getVehicleRequest(base, UUID.randomUUID());
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 404));
		return new RequestResponseContext(requestId, request);
	}

	private RequestResponseContext invalidSearchVehicleTest(int requestId) {
		Request request = new Request.Builder()
			.get()
			.url(base + "/kereses")
			.build();
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 400));
		return new RequestResponseContext(requestId, request);
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		int i = requestId % 6;
		return switch (i) {
			case 0 -> createVehicleTest(requestId);
			case 1 -> getVehicleTestOrCreate(requestId);
			case 2 -> searchVehicleTest(requestId);
			case 3 -> invalidCreateVehicleTest(requestId);
			case 4 -> invalidGetVehicleTest(requestId);
			case 5 -> invalidSearchVehicleTest(requestId);
			default -> throw new IllegalStateException("Unexpected value: " + i);
		};
	}

}
