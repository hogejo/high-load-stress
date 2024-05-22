package hu.laba.tests;

import hu.laba.RequestBuilder;
import hu.laba.ResponseValidator;
import hu.laba.scenarios.Scenario;

public interface Tester extends RequestBuilder, ResponseValidator {

	void setScenario(Scenario scenario);

	String getDescription();

	String getSummary();

}
