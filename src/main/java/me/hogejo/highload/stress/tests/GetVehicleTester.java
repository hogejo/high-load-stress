package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestResponseContext;

public class GetVehicleTester extends AbstractVehicleTester {

	public GetVehicleTester(Configuration configuration, VehicleTracker vehicleTracker) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "GetVehicleTester getting created vehicles";
	}

	private RequestResponseContext getVehicleTestOrFail(int requestId) {
		return getVehicleTest(requestId)
			.orElseThrow(() -> {
				vehicleTracker.printStatus();
				return new IllegalStateException("no vehicles available to get");
			});
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return getVehicleTestOrFail(requestId);
	}

}
