package hu.laba.tests;

import hu.laba.Configuration;
import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;

public class CountVehicleTester extends AbstractVehicleTester {

	public CountVehicleTester(VehicleTracker vehicleTracker, Configuration configuration) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "Get count of vehicles and print to System.err";
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		validators.put(requestId, context -> vehicleTracker.validateCountVehiclesResponse(
			context,
			actualCount -> {
				System.err.println("Count check returned " + actualCount);
				return true;
			}
		));
		return new RequestResponseContext(scenario, requestId, RequestBuilder.countVehiclesRequest(configuration.endpoint));
	}

}
