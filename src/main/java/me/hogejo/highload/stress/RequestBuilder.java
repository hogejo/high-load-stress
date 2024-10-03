package me.hogejo.highload.stress;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.UUID;

public interface RequestBuilder {

	String X_REQUEST_ID_HEADER = "X-Request-ID";

	static Request createVehicleRequest(int requestId, String endpoint, String json) {
		return new Request.Builder()
			.post(
				RequestBody.create(
					json,
					MediaType.get("application/json")
				)
			)
			.url(endpoint + "/jarmuvek")
			.header(RequestBuilder.X_REQUEST_ID_HEADER, String.valueOf(requestId))
			.build();
	}

	static Request getVehicleRequest(int requestId, String endpoint, UUID uuid) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/jarmuvek/" + uuid.toString())
			.header(RequestBuilder.X_REQUEST_ID_HEADER, String.valueOf(requestId))
			.build();
	}

	static Request searchVehiclesRequest(int requestId, String endpoint, String keyword) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/kereses?q=" + keyword)
			.header(RequestBuilder.X_REQUEST_ID_HEADER, String.valueOf(requestId))
			.build();
	}

	static Request countVehiclesRequest(int requestId, String endpoint) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/jarmuvek")
			.header(RequestBuilder.X_REQUEST_ID_HEADER, String.valueOf(requestId))
			.build();
	}

	RequestResponseContext buildRequest(int requestId);

}
