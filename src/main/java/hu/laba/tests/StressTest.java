package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.VehicleGenerator;
import okhttp3.Request;

import java.nio.file.Path;

public class StressTest extends AbstractTest {

	public StressTest(String base, VehicleTracker vehicleTracker, Path dumpDirectory) {
		super(base, vehicleTracker, dumpDirectory);
	}

	@Override
	public String getDescription() {
		return "StressTest";
	}

	private Request getVehicleTestOrCreate(int requestId) {
		return getVehicleTest(requestId).orElseGet(() -> createVehicleTest(requestId));
	}

	private Request searchManyVehiclesTest(int requestId) {
		Request request = RequestBuilder.searchVehiclesRequest(base, "AA" + VehicleGenerator.registrationCharacters.charAt(requestId % VehicleGenerator.registrationCharacters.length()));
		validators.put(requestId, response ->
			ResponseValidator.validateStatusCode(requestId, response, 200, forwardInvalidResponseMessage(requestId, request, response))
				&& ResponseValidator.validateBodyNotNull(requestId, response, forwardInvalidResponseMessage(requestId, request, response)));
		return request;
	}

	@Override
	public Request buildRequest(int requestId) {
		return switch (requestId % 10) {
			case 0 -> createVehicleTest(requestId);
			case 1, 2 -> searchManyVehiclesTest(requestId);
			default -> getVehicleTestOrCreate(requestId);
		};
	}

}
