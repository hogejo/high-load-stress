package me.hogejo.highload.stress.scenarios;

import me.hogejo.highload.stress.tests.Tester;

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
