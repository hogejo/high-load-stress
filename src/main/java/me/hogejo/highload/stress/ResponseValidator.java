package me.hogejo.highload.stress;

public interface ResponseValidator {

	static void validateStatusCode(RequestResponseContext context, int expectedCode) {
		int actualCode = context.getResponse().code();
		if (expectedCode < 10) {
			int actualCodeGroup = actualCode / 100;
			if (actualCodeGroup != expectedCode) {
				context.addErrorMessage("expected status code %dxx, got %d".formatted(expectedCode, actualCode));
			}
		} else {
			if (actualCode != expectedCode) {
				context.addErrorMessage("expected status code %d, got %d".formatted(expectedCode, actualCode));
			}
		}
	}

	static void validateBodyNotBlank(RequestResponseContext context) {
		if (context.getResponseBody().isBlank()) {
			context.addErrorMessage("missing body");
		}
	}

	void validateResponse(RequestResponseContext context);

}
