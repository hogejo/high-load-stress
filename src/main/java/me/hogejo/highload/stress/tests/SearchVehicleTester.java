package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestResponseContext;
import okhttp3.Request;

import java.util.Optional;

public class SearchVehicleTester extends AbstractVehicleTester {

	public SearchVehicleTester(Configuration configuration, VehicleTracker vehicleTracker) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "SearchVehicleTester searching for vehicles by registration";
	}

	private RequestResponseContext searchOneVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicleRequest(requestId, configuration.endpoint);
		if (optionalRequest.isEmpty()) {
			throw new IllegalStateException("no vehicles to search");
		} else {
			validators.put(requestId, vehicleTracker::validateSearchVehicleResponse);
			return new RequestResponseContext(scenario, requestId, optionalRequest.get());
		}
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return searchOneVehicleTest(requestId);
	}

}
