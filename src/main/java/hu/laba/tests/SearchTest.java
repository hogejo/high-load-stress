package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SearchTest implements RequestBuilder, ResponseValidator {

	private final String base;
	private final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	private final VehicleTracker vehicleTracker;

	public SearchTest(String base, VehicleTracker vehicleTracker) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
	}

	@Override
	public String getDescription() {
		return "SearchVehicles";
	}

	private Request searchVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.searchVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			throw new IllegalStateException("no vehicles to search?");
		} else {
			validators.put(requestId, response -> vehicleTracker.validateSearchVehicleResponse(requestId, response));
			return optionalRequest.get();
		}
	}

	@Override
	public Request buildRequest(int requestId) {
		return searchVehicleTest(requestId);
	}

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

}
