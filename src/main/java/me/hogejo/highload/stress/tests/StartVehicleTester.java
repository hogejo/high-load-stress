package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestBuilder;
import me.hogejo.highload.stress.RequestResponseContext;
import me.hogejo.highload.stress.ResponseValidator;
import me.hogejo.highload.stress.Vehicle;
import me.hogejo.highload.stress.VehicleGenerator;
import okhttp3.Request;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StartVehicleTester extends AbstractVehicleTester {

	public StartVehicleTester(Configuration configuration, VehicleTracker vehicleTracker) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "StartVehicleTester testing create, get, search APIs including invalid requests";
	}

	private RequestResponseContext getVehicleTestOrCreate(int requestId) {
		return getVehicleTest(requestId).orElseGet(() -> createVehicleTest(requestId));
	}

	private RequestResponseContext searchVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicleRequest(requestId, configuration.endpoint);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, vehicleTracker::validateSearchVehicleResponse);
			return new RequestResponseContext(scenario, requestId, "search vehicle", optionalRequest.get());
		}
	}

	private RequestResponseContext invalidCreateVehicleTest(int requestId) {
		Vehicle vehicle = VehicleGenerator.generateRandom(requestId);
		// No read after this, so why store?
		//vehicleTracker.sentVehicles.put(requestId, vehicle);
		String badSyntaxVehicleString = vehicle.toJsonString().replace("{", "");
		int r = Math.abs(ThreadLocalRandom.current().nextInt()) % 4;
		String description;
		Request request = RequestBuilder.createVehicleRequest(
			requestId,
			configuration.endpoint,
			switch (r) {
				case 0 -> {
					description = "bad syntax create vehicle";
					yield badSyntaxVehicleString;
				}
				case 1 -> {
					description = "missing registration create vehicle";
					yield new Vehicle(vehicle.uuid(), "", vehicle.owner(), vehicle.validity(), vehicle.data()).toCreateJsonString();
				}
				case 2 -> {
					description = "create vehicle with null data";
					yield new Vehicle(vehicle.uuid(), vehicle.registration(), vehicle.owner(), vehicle.validity(), null).toCreateJsonString();
				}
				case 3 -> {
					if (vehicleTracker.storedVehicles.isEmpty()) {
						description = "bad syntax create vehicle";
						yield badSyntaxVehicleString;
					}
					description = "create an existing vehicle again";
					yield vehicleTracker.storedVehicles.get(requestId % vehicleTracker.storedVehicles.size()).toCreateJsonString();
				}
				default -> throw new IllegalStateException("Unexpected value: " + r);
			}
		);
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 4));
		return new RequestResponseContext(scenario, requestId, description, request);
	}

	private RequestResponseContext invalidGetVehicleTest(int requestId) {
		Request request = RequestBuilder.getVehicleRequest(requestId, configuration.endpoint, UUID.randomUUID());
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 404));
		return new RequestResponseContext(scenario, requestId, "get random UUID", request);
	}

	private RequestResponseContext invalidSearchVehicleTest(int requestId) {
		Request request = new Request.Builder()
			.get()
			.url(configuration.endpoint + "/kereses")
			.header(RequestBuilder.X_REQUEST_ID_HEADER, String.valueOf(requestId))
			.build();
		validators.put(requestId, context -> ResponseValidator.validateStatusCode(context, 400));
		return new RequestResponseContext(scenario, requestId, "search vehicles with missing keyword", request);
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
