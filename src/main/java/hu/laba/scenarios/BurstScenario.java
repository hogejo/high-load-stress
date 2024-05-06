package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;

/**
 * Scenario of immediate burst of requests at start, followed by a wait for the specified duration.
 */
public class BurstScenario extends Scenario {

	private final int totalRequests;

	public BurstScenario(int totalRequests, float durationInSeconds, RequestBuilder requestBuilder, ResponseValidator responseValidator) {
		super("single-burst-%d".formatted(totalRequests), durationInSeconds, requestBuilder, responseValidator);
		this.totalRequests = totalRequests;
	}

	@Override
	public String getDescription() {
		return "%s with %d immediate requests then a %.2f second wait".formatted(this.getClass().getSimpleName(), totalRequests, durationInSeconds);
	}

	@Override
	public int getTotalRequests() {
		return totalRequests;
	}

	@Override
	public int getRequestCountAtTime(float time) {
		return totalRequests;
	}

}
