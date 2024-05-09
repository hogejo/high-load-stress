package hu.laba.tests;

import hu.laba.RequestResponseContext;

import java.nio.file.Path;

public class GetTest extends AbstractTest {

	public GetTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "GetVehicles";
	}

	private RequestResponseContext getVehicleTestOrFail(int requestId) {
		return getVehicleTest(requestId)
			.orElseThrow(() -> new IllegalStateException("no vehicles available to get?"));
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return getVehicleTestOrFail(requestId);
	}

}
