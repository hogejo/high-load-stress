package hu.laba.tests;

import okhttp3.Request;

import java.nio.file.Path;

public class GetTest extends AbstractTest {

	public GetTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "GetVehicles";
	}

	private Request getVehicleTestOrFail(int requestId) {
		return getVehicleTest(requestId).orElseThrow(() -> new IllegalStateException("no vehicles available to get?"));
	}

	@Override
	public Request buildRequest(int requestId) {
		return getVehicleTestOrFail(requestId);
	}

}
