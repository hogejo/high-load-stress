package hu.laba.tests;

import hu.laba.Configuration;
import hu.laba.RequestResponseContext;

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
			.orElseThrow(() -> new IllegalStateException("no vehicles available to get"));
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return getVehicleTestOrFail(requestId);
	}

}
