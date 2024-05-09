package hu.laba.tests;

import hu.laba.RequestResponseContext;
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

	private RequestResponseContext searchOneVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchOneVehicleRequest(requestId, base);
		if (optionalRequest.isEmpty()) {
			throw new IllegalStateException("no vehicles to search?");
		} else {
			validators.put(requestId, vehicleTracker::validateSearchVehicleResponse);
			return new RequestResponseContext(requestId, optionalRequest.get());
		}
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return searchOneVehicleTest(requestId);
	}

}
