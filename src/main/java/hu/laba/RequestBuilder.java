package hu.laba;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.UUID;

public interface RequestBuilder {

	RequestBuilder NO_REQUEST_BUILDER = requestId -> {
		throw new UnsupportedOperationException();
	};

	static Request createVehicleRequest(String base, String json) {
		return new Request.Builder()
			.post(
				RequestBody.create(
					json,
					MediaType.get("application/json")
				)
			)
			.url(base + "/jarmuvek")
			.build();
	}

	static Request getVehicleRequest(String base, UUID uuid) {
		return new Request.Builder()
			.get()
			.url(base + "/jarmuvek/" + uuid.toString())
			.build();
	}

	static Request searchVehiclesRequest(String base, String keyword) {
		return new Request.Builder()
			.get()
			.url(base + "/kereses?q=" + keyword)
			.build();
	}

	static Request countVehiclesRequest(String base) {
		return new Request.Builder()
			.get()
			.url(base + "/jarmuvek")
			.build();
	}

	default String getDescription() {
		return "<lambda>";
	}

	Request buildRequest(int requestId);

}
