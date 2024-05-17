package hu.laba.scenarios;

import hu.laba.tests.Tester;

/**
 * Scenario of immediate burst of requests at start, followed by a wait for the specified duration.
 */
public class BurstScenario extends AbstractScenario {

	private final int totalRequests;

	public BurstScenario(int totalRequests, float durationInSeconds, Tester tester) {
		super("burst-%d".formatted(totalRequests), durationInSeconds, tester);
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
