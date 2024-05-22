package hu.laba;

import hu.laba.scenarios.Scenario;
import hu.laba.tests.VehicleTracker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class Stress {

	private final Configuration configuration;
	private final VehicleTracker vehicleTracker;
	private final List<Scenario> scenarios;

	@Inject
	public Stress(
		Configuration configuration,
		VehicleTracker vehicleTracker,
		List<Scenario> scenarios
	) {
		this.configuration = configuration;
		this.vehicleTracker = vehicleTracker;
		this.scenarios = scenarios;
	}

	public void printIntroduction() {
		System.out.println("Running stress test against endpoint: " + configuration.endpoint);
		System.out.println("Expected scenarios will be:");
		Application.listScenarios(scenarios);
	}

	public void run() throws InterruptedException {
		printIntroduction();
		TimeKeeper globalTimeKeeper = new TimeKeeper();
		ScenarioLogger scenarioLogger = new ScenarioLogger(globalTimeKeeper);
		for (Scenario scenario : scenarios) {
			TimeKeeper timeKeeper = new TimeKeeper();
			scenarioLogger.setScenario(scenario);
			ScenarioRunner scenarioRunner = new ScenarioRunner(timeKeeper, scenario, scenarioLogger);
			System.out.println();
			scenarioRunner.run();
		}
		System.out.println();
		System.out.println("Writing CSV results to file: " + configuration.timelineOutput);
		scenarioLogger.saveAsCSV(configuration.timelineOutput);
		System.out.println();
		System.out.println("Last datapoint is:");
		ScenarioLogger.DataPoint lastDataPoint = scenarioLogger.getCurrentDataPoint();
		System.out.println("  " + lastDataPoint);
		if (lastDataPoint.expectedRequests() == 0) {
			return;
		}
		System.out.printf("  %8.2f%% expected requests scheduled%n", lastDataPoint.scheduledRequests() * 100 / (float) lastDataPoint.expectedRequests());
		System.out.printf("  %8.2f%% scheduled requests started%n", lastDataPoint.startedRequests() * 100 / (float) lastDataPoint.scheduledRequests());
		System.out.printf("  %8.2f%% started requests finished successful%n", lastDataPoint.successfulRequests() * 100 / (float) lastDataPoint.startedRequests());
		System.out.printf("  %8.2f%% successful requests valid%n", lastDataPoint.validResponses() * 100 / (float) lastDataPoint.successfulRequests());
		System.out.println();
		System.out.println("Latency buckets are:");
		scenarioLogger.getBuckets().forEach(s -> System.out.println("  " + s));
		List<String> failReasons = scenarioLogger.getFailReasons();
		if (!failReasons.isEmpty()) {
			System.out.println();
			System.out.println("Fail reasons are:");
			failReasons.forEach(s -> System.out.println("  " + s));
			System.out.println("Fail details are:");
			scenarioLogger.getFailDetails().forEach(System.out::println);
		}
	}

}
