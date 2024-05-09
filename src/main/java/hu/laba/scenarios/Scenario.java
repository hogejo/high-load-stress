package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.RequestResponseContext;
import hu.laba.ResponseValidator;

/**
 * Base class for all scenarios, with basic implementation for name and duration.
 */
public abstract class Scenario implements RequestBuilder, ResponseValidator {

	protected final String name;
	protected final float durationInSeconds;
	protected final RequestBuilder requestBuilder;
	protected final ResponseValidator responseValidator;
	protected int maximumInflightRequests = Integer.MAX_VALUE;

	/**
	 * Create a scenario with the given {@code name} and duration (in seconds).
	 * @param name name of the scenario
	 * @param durationInSeconds duration of the scenario in seconds
	 */
	protected Scenario(String name, float durationInSeconds, RequestBuilder requestBuilder, ResponseValidator responseValidator) {
		this.name = name;
		this.durationInSeconds = durationInSeconds;
		this.requestBuilder = requestBuilder;
		this.responseValidator = responseValidator;
	}

	public String getName() {
		return this.name + "-for-%.1fs".formatted(durationInSeconds);
	}

	public String getDescription() {
		return "%s for %.2f seconds".formatted(this.getClass().getSimpleName(), durationInSeconds);
	}

	public String getRequestBuilderDescription() {
		return "Building requests with " + requestBuilder.getDescription();
	}

	public float getDurationInSeconds() {
		return durationInSeconds;
	}

	public boolean isOver(float time) {
		return time > durationInSeconds;
	}

	public abstract int getTotalRequests();

	public Scenario withMaximumInflightRequests(int maximumInflightRequests) {
		this.maximumInflightRequests = maximumInflightRequests;
		return this;
	}

	public int getMaximumInflightRequests() {
		return maximumInflightRequests;
	}

	public abstract int getRequestCountAtTime(float time);

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return requestBuilder.buildRequest(requestId);
	}

	@Override
	public void validateResponse(RequestResponseContext context) {
		responseValidator.validateResponse(context);
	}

}
