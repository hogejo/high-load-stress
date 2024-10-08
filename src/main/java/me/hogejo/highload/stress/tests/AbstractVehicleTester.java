package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestResponseContext;
import okhttp3.Request;

import java.util.Optional;

public abstract class AbstractVehicleTester extends AbstractTester {

	protected final VehicleTracker vehicleTracker;

	public AbstractVehicleTester(Configuration configuration, VehicleTracker vehicleTracker) {
		super(configuration);
		this.vehicleTracker = vehicleTracker;
	}

	protected RequestResponseContext createVehicleTest(int requestId) {
		Request request = vehicleTracker.createVehicleRequest(requestId, configuration.endpoint, vehicleTracker.createNewRandomVehicle());
		validators.put(requestId, vehicleTracker::validateCreateVehicleResponse);
		return new RequestResponseContext(scenario, requestId, "create vehicle", request);
	}

	protected Optional<RequestResponseContext> getVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.getVehicleRequest(requestId, configuration.endpoint);
		if (optionalRequest.isPresent()) {
			validators.put(requestId, vehicleTracker::validateGetVehicleResponse);
			return Optional.of(new RequestResponseContext(scenario, requestId, "get vehicle", optionalRequest.get()));
		}
		return Optional.empty();
	}

}
