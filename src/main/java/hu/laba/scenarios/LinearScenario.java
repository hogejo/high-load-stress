package hu.laba.scenarios;

import hu.laba.tests.Tester;

import static java.lang.Integer.min;

/**
 * Scenario with linearly increasing rate (requests per second)
 */
public class LinearScenario extends AbstractScenario {

	private final int totalRequests;
	private final float rateAtTheEnd;

	public LinearScenario(int totalRequests, float durationInSeconds, Tester tester) {
		super("linear-%d".formatted(totalRequests), durationInSeconds, tester);
		this.totalRequests = totalRequests;
		this.rateAtTheEnd = (totalRequests * 2) / durationInSeconds;
	}

	@Override
	public String getDescription() {
		return "%s with a total of %d requests during %.2f seconds, with a peak of %.2f rps at the end".formatted(
			this.getClass().getSimpleName(),
			totalRequests,
			durationInSeconds,
			rateAtTheEnd
		);
	}

	@Override
	public int getTotalRequests() {
		return totalRequests;
	}

	@Override
	public int getRequestCountAtTime(float time) {
		return min(totalRequests, (int) ((time * time * rateAtTheEnd) / (2 * durationInSeconds)));
	}

}
