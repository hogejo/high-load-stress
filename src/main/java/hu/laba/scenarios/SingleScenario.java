package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;

public class SingleScenario extends Scenario {

	public SingleScenario(String name, RequestBuilder requestBuilder, ResponseValidator responseValidator) {
		super(name, 1, requestBuilder, responseValidator);
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
