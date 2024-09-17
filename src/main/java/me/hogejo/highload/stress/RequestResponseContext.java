package me.hogejo.highload.stress;

import me.hogejo.highload.stress.scenarios.Scenario;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RequestResponseContext {

	public final Scenario scenario;
	public final int requestId;
	public final String description;
	public final Request request;
	private String requestBody = null;
	private Response response = null;
	private String responseBody = null;
	private final List<String> errorMessages = new ArrayList<>();

	public RequestResponseContext(Scenario scenario, int requestId, String description, @NotNull Request request) {
		this.scenario = scenario;
		this.requestId = requestId;
		this.description = description;
		this.request = request;
	}

	public String getRequestBody() {
		if (requestBody == null && request.body() != null) {
			try {
				Buffer buffer = new Buffer();
				this.request.body().writeTo(buffer);
				requestBody = buffer.readUtf8();
			} catch (IOException exception) {
				addErrorMessage("Failed to read request body: " + exception);
				requestBody = "";
				return "";
			}
		}
		return requestBody != null ? requestBody : "";
	}

	public void setResponse(@NotNull Response response) {
		if (this.response != null) {
			throw new IllegalStateException("Response has already been set");
		}
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}

	@NotNull
	public String getResponseBody() {
		if (responseBody == null && response != null && response.body() != null) {
			try {
				responseBody = response.body().string();
			} catch (IOException exception) {
				addErrorMessage("Failed to read response body: " + exception);
				responseBody = "";
				return "";
			} catch (IllegalStateException exception) {
				addErrorMessage("Unexpected state while reading response body: " + exception);
				responseBody = "";
				return "";
			}
		}
		return responseBody != null ? responseBody : "";
	}

	public void addErrorMessage(@NotNull String message) {
		errorMessages.add(message);
	}

	public List<String> getErrorMessages() {
		return List.copyOf(errorMessages);
	}

	public boolean isValid() {
		return errorMessages.isEmpty();
	}

}
