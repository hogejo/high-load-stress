package hu.laba;

import okhttp3.Response;

import java.util.function.Consumer;

public interface ResponseValidator {

	ResponseValidator ALWAYS_VALID_VALIDATOR = (requestId, response) -> true;

	static boolean validateStatusCode(int requestId, Response response, int expectedCode, Consumer<String> messageConsumer) {
		int actualCode = response.code();
		if (!response.isSuccessful() || actualCode != expectedCode) {
			messageConsumer.accept("expected status code %d, got %d".formatted(expectedCode, actualCode));
			return false;
		}
		return true;
	}

	static boolean validateBodyNotNull(int requestId, Response response, Consumer<String> messageConsumer) {
		if (response.body() == null) {
			messageConsumer.accept("missing body");
			return false;
		}
		return true;
	}

	Boolean validateResponse(int requestId, Response response);

}
