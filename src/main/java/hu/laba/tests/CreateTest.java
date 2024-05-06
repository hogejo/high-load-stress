package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CreateTest implements RequestBuilder, ResponseValidator {

	private final String base;
	private final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	private final VehicleTracker vehicleTracker;

	public CreateTest(String base, VehicleTracker vehicleTracker) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
	}

	@Override
	public String getDescription() {
		return "CreateVehicles";
	}

	private Request createVehicleTest(int requestId) {
		validators.put(requestId, response -> vehicleTracker.validateCreateVehicleResponse(requestId, response));
		return vehicleTracker.createVehicle(requestId, base, vehicleTracker.createNewRandomVehicle());
	}

	@Override
	public Request buildRequest(int requestId) {
		return createVehicleTest(requestId);
	}

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

}
