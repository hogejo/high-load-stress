package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;
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

	private RequestResponseContext getVehicleTestOrCreate(int requestId) {
		return getVehicleTest(requestId).orElseGet(() -> createVehicleTest(requestId));
	}

	private RequestResponseContext searchManyVehiclesTest(int requestId) {
		Request request = RequestBuilder.searchVehiclesRequest(base, "AA" + VehicleGenerator.registrationCharacters.charAt(requestId % VehicleGenerator.registrationCharacters.length()));
		validators.put(requestId, context -> {
			ResponseValidator.validateStatusCode(context, 200);
			ResponseValidator.validateBodyNotBlank(context);
			vehicleTracker.readListOfVehicles(context);
		});
		return new RequestResponseContext(requestId, request);
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return switch (requestId % 10) {
			case 0 -> createVehicleTest(requestId);
			case 1, 2 -> searchManyVehiclesTest(requestId);
			default -> getVehicleTestOrCreate(requestId);
		};
	}

}
