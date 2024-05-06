package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.ResponseValidatorFunction;
import hu.laba.VehicleGenerator;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StressTest implements RequestBuilder, ResponseValidator {

	private final String base;
	private final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	private final VehicleTracker vehicleTracker;

	public StressTest(String base, VehicleTracker vehicleTracker) {
		this.base = base;
		this.vehicleTracker = vehicleTracker;
	}

	@Override
	public String getDescription() {
		return "StressTest";
	}

	private Request createVehicleTest(int requestId) {
		validators.put(requestId, response -> vehicleTracker.validateCreateVehicleResponse(requestId, response));
		return vehicleTracker.createVehicle(requestId, base, vehicleTracker.createNewRandomVehicle());
	}

	private Request getVehicleTest(int requestId) {
		Optional<Request> optionalRequest = vehicleTracker.getVehicle(requestId, base);
		if (optionalRequest.isEmpty()) {
			return createVehicleTest(requestId);
		} else {
			validators.put(requestId, response -> vehicleTracker.validateGetVehicleResponse(requestId, response));
			return optionalRequest.get();
		}
	}

	private Request searchVehicleTest(int requestId) {
		validators.put(requestId, response -> ResponseValidator.validateStatusCode(requestId, response, 200) && ResponseValidator.validateBodyNotNull(requestId, response));
		return RequestBuilder.searchVehiclesRequest(base, "AA" + VehicleGenerator.registrationCharacters.charAt(requestId % VehicleGenerator.registrationCharacters.length()));
	}

	@Override
	public Request buildRequest(int requestId) {
		return switch (requestId % 10) {
			case 0 -> createVehicleTest(requestId);
			case 1, 2 -> searchVehicleTest(requestId);
			default -> getVehicleTest(requestId);
		};
	}

	@Override
	public Boolean validateResponse(int requestId, Response response) {
		return validators.get(requestId).apply(response);
	}

}
