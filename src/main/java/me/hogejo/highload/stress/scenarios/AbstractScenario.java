package me.hogejo.highload.stress.scenarios;

import me.hogejo.highload.stress.RequestResponseContext;
import me.hogejo.highload.stress.tests.Tester;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Base class for all scenarios, with basic implementation for name and duration.
 */
public abstract class AbstractScenario implements Scenario {

	private static final Set<String> identifiers = new CopyOnWriteArraySet<>();

	protected final String identifier;
	protected final float durationInSeconds;
	protected final Tester tester;
	protected int maximumInflightRequests = Integer.MAX_VALUE;

	/**
	 * Create a scenario with the given {@code name} and duration (in seconds).
	 * @param durationInSeconds duration of the scenario in seconds
	 */
	protected AbstractScenario(String identifier, float durationInSeconds, Tester tester) {
		if (identifiers.contains(identifier)) {
			throw new IllegalArgumentException("Identifier '" + identifier + "' already exists");
		}
		identifiers.add(identifier);
		this.identifier = identifier;
		this.durationInSeconds = durationInSeconds;
		this.tester = tester;
		tester.setScenario(this);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getDescription() {
		return "%s for %.2f seconds".formatted(this.getClass().getSimpleName(), durationInSeconds);
	}

	@Override
	public Tester getTester() {
		return tester;
	}

	@Override
	public float getDurationInSeconds() {
		return durationInSeconds;
	}

	@Override
	public boolean isOver(float time) {
		return time > durationInSeconds;
	}

	@Override
	public AbstractScenario withMaximumInFlightRequests(int maximumInflightRequests) {
		this.maximumInflightRequests = maximumInflightRequests;
		return this;
	}

	@Override
	public int getMaximumInFlightRequests() {
		return maximumInflightRequests;
	}

	@Override
	public RequestResponseContext buildRequest(int requestId) {
		return tester.buildRequest(requestId);
	}

	@Override
	public void validateResponse(RequestResponseContext context) {
		tester.validateResponse(context);
	}

}
