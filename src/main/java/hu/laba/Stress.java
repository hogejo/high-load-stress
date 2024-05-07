package hu.laba;

import com.beust.jcommander.JCommander;
import hu.laba.scenarios.BurstScenario;
import hu.laba.scenarios.ConstantScenario;
import hu.laba.scenarios.LinearScenario;
import hu.laba.scenarios.Scenario;
import hu.laba.scenarios.SingleScenario;
import hu.laba.scenarios.WaitScenario;
import hu.laba.tests.CreateTest;
import hu.laba.tests.GetTest;
import hu.laba.tests.SearchTest;
import hu.laba.tests.StartTest;
import hu.laba.tests.StressTest;
import hu.laba.tests.VehicleTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Stress {

	@SuppressWarnings("HttpUrlsUsage")
	public static void main(String[] arguments) {
		Configuration configuration = new Configuration();
		JCommander jCommander = JCommander.newBuilder()
			.addObject(configuration)
			.build();
		jCommander.parse(arguments);
		String endpointRegex = "^[a-z.]+:[0-9]{2,5}$";
		if (!configuration.endpoint.matches(endpointRegex)) {
			System.err.println("Endpoint does not match regex: " + endpointRegex);
			System.exit(1);
		}
		Path timelineOutputPath = Path.of(configuration.timelineOutput);
		if (Files.exists(timelineOutputPath) && !Files.isWritable(timelineOutputPath)) {
			System.err.println("Can't write to file: " + configuration.timelineOutput);
			System.exit(1);
		}
		if (!Files.exists(timelineOutputPath) && !Files.isWritable(timelineOutputPath.getParent())) {
			System.err.println("Can't create file: " + timelineOutputPath.getParent());
			System.exit(1);
		}
		Path dumpdirectory = null;
		if (configuration.dumpRequests) {
			Path requestDumpDirectoryPath = Path.of(configuration.requestDumpDirectory);
			if (!Files.exists(requestDumpDirectoryPath)) {
				try {
					Files.createDirectory(requestDumpDirectoryPath);
					System.out.println("Created directory for dumping invalid responses: " + requestDumpDirectoryPath);
				} catch (IOException exception) {
					System.err.println("Can't create directory: " + requestDumpDirectoryPath);
					exception.printStackTrace(System.err);
					System.exit(1);
				}
			}
			if (!Files.isDirectory(requestDumpDirectoryPath)) {
				System.err.println("Dump directory is not a directory: " + requestDumpDirectoryPath);
				System.exit(1);
			}
			dumpdirectory = requestDumpDirectoryPath;
		}
		if (configuration.help) {
			System.out.println("Run (entire) official stress test against HTTP endpoint.");
			System.out.println();
			jCommander.setProgramName("stress");
			jCommander.usage();
			System.exit(0);
		}
		String baseUrl = "http://" + configuration.endpoint;
		try {
			run(baseUrl, timelineOutputPath, dumpdirectory);
		} catch (InterruptedException | IOException e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void run(String baseUrl, Path timelineOutputPath, Path dumpDirectory) throws InterruptedException, IOException {
		System.out.println("Running stress test against base URL: " + baseUrl);
		VehicleTracker vehicleTracker = new VehicleTracker();
		StartTest startTest = new StartTest(baseUrl, vehicleTracker, dumpDirectory);
		CreateTest createTest = new CreateTest(baseUrl, vehicleTracker, dumpDirectory);
		GetTest getTest = new GetTest(baseUrl, vehicleTracker, dumpDirectory);
		SearchTest searchTest = new SearchTest(baseUrl, vehicleTracker, dumpDirectory);
		StressTest stressTest = new StressTest(baseUrl, vehicleTracker, dumpDirectory);
		SingleScenario countCheck = new SingleScenario("count-check",
			requestId -> RequestBuilder.countVehiclesRequest(baseUrl),
			(requestId, response) -> vehicleTracker.validateCountVehiclesResponse(
				requestId,
				response,
				actualCount -> {
					System.err.println("Count check returned " + actualCount);
					return true;
				},
				(ignored, ignored2) -> {}
			)
		);
		Scenario[] scenarios = new Scenario[]{
			new ConstantScenario(10, 10, startTest, startTest)
				.withMaximumInflightRequests(10),
			new ConstantScenario(100, 60, createTest, createTest)
				.withMaximumInflightRequests(100),
			new ConstantScenario(100, 60, getTest, getTest)
				.withMaximumInflightRequests(100),
			new ConstantScenario(100, 60, searchTest, searchTest)
				.withMaximumInflightRequests(100),

			new WaitScenario("sleep", 5),
			countCheck,
			new WaitScenario("sleep", 5),

			new ConstantScenario(500, 60, stressTest, stressTest),

			new WaitScenario("sleep", 5),
			countCheck,
			new WaitScenario("sleep", 5),

			new LinearScenario(25_000, 100, stressTest, stressTest),

			new WaitScenario("sleep", 5),
			countCheck,
			new WaitScenario("sleep", 5),

			new BurstScenario(5000, 5, stressTest, stressTest),

			new WaitScenario("sleep", 5),
			countCheck,
			new WaitScenario("sleep", 5),

			new LinearScenario(100_000, 100, stressTest, stressTest),

			new WaitScenario("sleep", 5),
			countCheck
		};
		System.out.println("Expected scenarios will be:");
		for (int i = 0; i < scenarios.length; i++) {
			Scenario scenario = scenarios[i];
			System.out.printf("  %d: %s%n", i, scenario.getDescription());
			if (scenario.getClass().equals(WaitScenario.class) || scenario.getClass().equals(SingleScenario.class)) {
				continue;
			}
			System.out.printf("    %s%n", scenario.getRequestBuilderDescription());
		}
		TimeKeeper globalTimeKeeper = new TimeKeeper();
		ScenarioLogger scenarioLogger = new ScenarioLogger(globalTimeKeeper);
		for (Scenario scenario : scenarios) {
			TimeKeeper timeKeeper = new TimeKeeper();
			scenarioLogger.setScenario(scenario);
			ScenarioRunner scenarioRunner = new ScenarioRunner(timeKeeper, scenario, scenarioLogger);
			scenarioRunner.run();
		}
		System.out.println("Writing CSV results to file: " + timelineOutputPath);
		scenarioLogger.saveAsCSV(timelineOutputPath);
		System.out.println("Last datapoint is:");
		ScenarioLogger.DataPoint lastDataPoint = scenarioLogger.getCurrentDataPoint();
		System.out.println("  " + lastDataPoint);
		System.out.printf("  %.2f%% expected requests scheduled%n", lastDataPoint.scheduledRequests() * 100 / (float) lastDataPoint.expectedRequests());
		System.out.printf("  %.2f%% scheduled requests started%n", lastDataPoint.startedRequests() * 100 / (float) lastDataPoint.scheduledRequests());
		System.out.printf("  %.2f%% started requests finished successful%n", lastDataPoint.successfulRequests() * 100 / (float) lastDataPoint.startedRequests());
		System.out.printf("  %.2f%% successful requests valid%n", lastDataPoint.validResponses() * 100 / (float) lastDataPoint.successfulRequests());
		System.out.println("Latency buckets are:");
		scenarioLogger.getBuckets().forEach(s -> System.out.println("  " + s));
		System.out.println("Fail reasons are:");
		scenarioLogger.getFailReasons().forEach(s -> System.out.println("  " + s));
	}

}
