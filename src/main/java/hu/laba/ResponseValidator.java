package hu.laba;

import okhttp3.Response;

public interface ResponseValidator {

	ResponseValidator ALWAYS_VALID_VALIDATOR = (requestId, response) -> true;

	static boolean validateStatusCode(int requestId, Response response, int expectedCode) {
		int actualCode = response.code();
		if (!response.isSuccessful() || actualCode != expectedCode) {
			System.err.printf("Request #%d: expected %d, got %d%n", requestId, expectedCode, actualCode);
			return false;
		}
		return true;
	}

	static boolean validateBodyNotNull(int requestId, Response response) {
		if (response.body() == null) {
			System.err.printf("Request #%d: missing body%n", requestId);
			return false;
		}
		return true;
	}

	Boolean validateResponse(int requestId, Response response);

}
