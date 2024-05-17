package hu.laba.scenarios;

import hu.laba.tests.Tester;

public class SingleScenario extends AbstractScenario {

	public SingleScenario(String name, Tester tester) {
		super(name, 1, tester);
	}

	@Override
	public int getTotalRequests() {
		return 1;
	}

	@Override
	public int getRequestCountAtTime(float time) {
		return 1;
	}

}
