package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GetTest implements RequestBuilder, ResponseValidator {

	private final String base;
	private final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	private final VehicleTracker vehicleTracker;

	public GetTest(String base, VehicleTracker vehicleTracker) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
	}

	@Override
	public String getDescription() {
		return "GetVehicles";
	}

	private Request getVehicleTest(int requestId) {
		validators.put(requestId, response -> vehicleTracker.validateGetVehicleResponse(requestId, response));
		Optional<Request> optionalRequest = vehicleTracker.getVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			throw new IllegalStateException("no vehicles available to get?");
		}
		return optionalRequest.get();
	}

	@Override
	public Request buildRequest(int requestId) {
		return getVehicleTest(requestId);
	}

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

}
