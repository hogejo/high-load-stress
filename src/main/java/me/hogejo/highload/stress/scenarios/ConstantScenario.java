package me.hogejo.highload.stress.scenarios;

import me.hogejo.highload.stress.tests.Tester;

import static java.lang.Integer.min;

/**
 * Scenario with a constant rate (request per second) of requests for the given duration.
 */
public class ConstantScenario extends AbstractScenario {

	private final int constantRequestsPerSecond;
	private final int totalRequests;

	public ConstantScenario(String identifier, int constantRequestsPerSecond, float durationInSeconds, Tester tester) {
		super(identifier, durationInSeconds, tester);
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
