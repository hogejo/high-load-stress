package hu.laba;

import okhttp3.Response;

import java.util.function.BiConsumer;

public interface ResponseValidator {

	ResponseValidator ALWAYS_VALID_VALIDATOR = (requestId, response) -> true;

	static boolean validateStatusCode(int requestId, Response response, int expectedCode, BiConsumer<String, String> messageAndBodyConsumer) {
		int actualCode = response.code();
		if (!response.isSuccessful() || actualCode != expectedCode) {
			messageAndBodyConsumer.accept("expected status code %d, got %d".formatted(expectedCode, actualCode), null);
			return false;
		}
		return true;
	}

	static boolean validateBodyNotNull(int requestId, Response response, BiConsumer<String, String> messageAndBodyConsumer) {
		if (response.body() == null) {
			messageAndBodyConsumer.accept("missing body", null);
			return false;
		}
		return true;
	}

	Boolean validateResponse(int requestId, Response response);

}
