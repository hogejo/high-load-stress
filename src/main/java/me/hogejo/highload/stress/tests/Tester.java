package me.hogejo.highload.stress.tests;

import me.hogejo.highload.stress.RequestBuilder;
import me.hogejo.highload.stress.ResponseValidator;
import me.hogejo.highload.stress.scenarios.Scenario;

public interface Tester extends RequestBuilder, ResponseValidator {

	void setScenario(Scenario scenario);

	String getDescription();

	String getSummary();

}
