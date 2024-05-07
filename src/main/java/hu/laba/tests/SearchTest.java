package hu.laba.tests;

import okhttp3.Request;

import java.nio.file.Path;
import java.util.Optional;

public class SearchTest extends AbstractTest {

	public SearchTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "SearchVehicles";
	}

	private Request searchOneVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			throw new IllegalStateException("no vehicles to search?");
		} else {
			validators.put(requestId,
				response -> vehicleTracker.validateSearchVehicleResponse(requestId, response,
					forwardInvalidResponseMessage(requestId, optionalRequest.get(), response)
				)
			);
			return optionalRequest.get();
		}
	}

	@Override
	public Request buildRequest(int requestId) {
		return searchOneVehicleTest(requestId);
	}

}
