package me.hogejo.highload.stress.scenarios;

import me.hogejo.highload.stress.RequestBuilder;
import me.hogejo.highload.stress.ResponseValidator;
import me.hogejo.highload.stress.tests.Tester;

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
