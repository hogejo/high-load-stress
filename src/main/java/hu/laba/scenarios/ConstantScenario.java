package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;

import static java.lang.Integer.min;

/**
 * Scenario with a constant rate (request per second) of requests for the given duration.
 */
public class ConstantScenario extends Scenario {

	private final int constantRequestsPerSecond;
	private final int totalRequests;

	public ConstantScenario(int constantRequestsPerSecond, float durationInSeconds, RequestBuilder requestBuilder, ResponseValidator responseValidator) {
		super("constant-%d-rps".formatted(constantRequestsPerSecond), durationInSeconds, requestBuilder, responseValidator);
		this.constantRequestsPerSecond = constantRequestsPerSecond;
		this.totalRequests = (int) (constantRequestsPerSecond * durationInSeconds);
	}

	@Override
	public String getDescription() {
		return "%s with %d requests per second for %.2f seconds".formatted(this.getClass().getSimpleName(), constantRequestsPerSecond, durationInSeconds);
	}

	@Override
	public int getTotalRequests() {
		return totalRequests;
	}

	@Override
	public int getRequestCountAtTime(float time) {
		return min((int) (constantRequestsPerSecond * time), totalRequests);
	}

}
