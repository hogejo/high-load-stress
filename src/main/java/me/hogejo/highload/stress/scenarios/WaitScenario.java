package me.hogejo.highload.stress.scenarios;

import me.hogejo.highload.stress.RequestResponseContext;
import me.hogejo.highload.stress.tests.AbstractTester;

/**
 * Scenario with no requests but a wait duration.
 */
public class WaitScenario extends AbstractScenario {

	public WaitScenario(float durationInSeconds) {
		super("wait-%ds".formatted((int) durationInSeconds), durationInSeconds, new AbstractTester(null) {
			@Override
			public RequestResponseContext buildRequest(int requestId) {
				throw new UnsupportedOperationException("not building requests");
			}

			@Override
			public String getDescription() {
				return "no requests";
			}
		});
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
