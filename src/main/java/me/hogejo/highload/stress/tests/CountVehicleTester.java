package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.RequestBuilder;
import me.hogejo.highload.stress.RequestResponseContext;

public class CountVehicleTester extends AbstractVehicleTester {

	public CountVehicleTester(VehicleTracker vehicleTracker, Configuration configuration) {
		super(configuration, vehicleTracker);
	}

	@Override
	public String getDescription() {
		return "Get count of vehicles";
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		validators.put(requestId, context -> vehicleTracker.validateCountVehiclesResponse(
			context,
			actualCount -> {
				summary = "count check returned %d".formatted(actualCount);
				return true;
			}
		));
		return new RequestResponseContext(scenario, requestId, "count check", RequestBuilder.countVehiclesRequest(requestId, configuration.endpoint));
	}

}
