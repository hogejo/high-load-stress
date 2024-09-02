package me.hogejo.highload.stress;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.UUID;

public interface RequestBuilder {

	static Request createVehicleRequest(String endpoint, String json) {
		return new Request.Builder()
			.post(
				RequestBody.create(
					json,
					MediaType.get("application/json")
				)
			)
			.url(endpoint + "/jarmuvek")
			.build();
	}

	static Request getVehicleRequest(String endpoint, UUID uuid) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/jarmuvek/" + uuid.toString())
			.build();
	}

	static Request searchVehiclesRequest(String endpoint, String keyword) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/kereses?q=" + keyword)
			.build();
	}

	static Request countVehiclesRequest(String endpoint) {
		return new Request.Builder()
			.get()
			.url(endpoint + "/jarmuvek")
			.build();
	}

	RequestResponseContext buildRequest(int requestId);

}
