package hu.laba.scenarios;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.tests.Tester;

public interface Scenario extends RequestBuilder, ResponseValidator {

	String getIdentifier();

	String getDescription();

	Tester getTester();

	float getDurationInSeconds();

	boolean isOver(float time);

	int getTotalRequests();

	int getRequestCountAtTime(float time);

	Scenario withMaximumInFlightRequests(int maximumInFlightRequests);

	int getMaximumInFlightRequests();

}
