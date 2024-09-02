package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestResponseContext;

public class CreateVehicleTester extends AbstractVehicleTester {

	public CreateVehicleTester(Configuration configuration, VehicleTracker vehicleTracker) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "CreateVehicleTester creating vehicles";
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return createVehicleTest(requestId);
	}

}
