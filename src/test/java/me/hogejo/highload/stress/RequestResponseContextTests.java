package me.hogejo.highload.stress;

import me.hogejo.highload.stress.scenarios.SingleScenario;
import me.hogejo.highload.stress.tests.CountVehicleTester;
import me.hogejo.highload.stress.tests.VehicleTracker;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestResponseContextTests {

	@Test
	public void contextPrintsRequestBody() {
		VehicleTracker vehicleTracker = new VehicleTracker();
		Configuration configuration = new Configuration();
		CountVehicleTester vehicleTester = new CountVehicleTester(vehicleTracker, configuration);
		SingleScenario scenario = new SingleScenario("contextPrintsRequestBody", vehicleTester);
		String content = "cipőfűző";
		RequestBody requestBody = RequestBody.create(content, MediaType.get("text/plain"));
		Request request = new Request.Builder()
			.post(requestBody)
			.url("https://foo.bar/baz")
			.build();
		RequestResponseContext requestResponseContext = new RequestResponseContext(scenario, 0, "description", request);
		assertEquals(content, requestResponseContext.getRequestBody());
	}

	@Test
	public void contextPrintsResponseBody() {
		VehicleTracker vehicleTracker = new VehicleTracker();
		Configuration configuration = new Configuration();
		CountVehicleTester vehicleTester = new CountVehicleTester(vehicleTracker, configuration);
		SingleScenario scenario = new SingleScenario("contextPrintsResponseBody", vehicleTester);
		Request request = new Request.Builder()
			.get()
			.url("https://foo.bar/baz")
			.build();
		RequestResponseContext requestResponseContext = new RequestResponseContext(scenario, 0, "description", request);
		String content = "cipőfűző";
		ResponseBody responseBody = ResponseBody.create(content, MediaType.get("text/plain"));
		Response response = new Response.Builder()
			.request(request)
			.protocol(Protocol.HTTP_1_1)
			.code(200)
			.message("OK")
			.body(responseBody)
			.build();
		requestResponseContext.setResponse(response);
		assertEquals(content, requestResponseContext.getResponseBody());
	}

}
