package hu.laba.tests;

import hu.laba.RequestResponseContext;

import java.nio.file.Path;

public class CreateTest extends AbstractTest {

	public CreateTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "CreateVehicles";
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return createVehicleTest(requestId);
	}

}
