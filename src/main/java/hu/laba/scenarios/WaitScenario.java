package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;

/**
 * Scenario with no requests but a wait duration.
 */
public class WaitScenario extends Scenario {

	public WaitScenario(String name, float durationInSeconds) {
		super(name, durationInSeconds, RequestBuilder.NO_REQUEST_BUILDER, ResponseValidator.ALWAYS_VALID_VALIDATOR);
	}

	@Override
	public int getTotalRequests() {
		return 0;
	}

	@Override
	public int getRequestCountAtTime(float time) {
		return 0;
	}

}
