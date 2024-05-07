package hu.laba;

import hu.laba.scenarios.Scenario;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ScenarioRunner {

	private final Scenario scenario;
	private final ScenarioLogger scenarioLogger;
	private final TimeKeeper timeKeeper;

	public ScenarioRunner(TimeKeeper timeKeeper, Scenario scenario, ScenarioLogger scenarioLogger) {
		this.scenario = scenario;
		this.timeKeeper = timeKeeper;
		this.scenarioLogger = scenarioLogger;
	}

	private OkHttpClient buildOkHttpClient(ExecutorService executorService) {
		Dispatcher dispatcher = new Dispatcher(executorService);
		dispatcher.setMaxRequests(25_000);
		dispatcher.setMaxRequestsPerHost(25_000);
		return new OkHttpClient.Builder()
			.retryOnConnectionFailure(false)
			.followRedirects(false)
			.cache(null)
			.callTimeout(Duration.ofMillis(1000))
			.connectTimeout(Duration.ofMillis(1000))
			.readTimeout(Duration.ofMillis(1000))
			.writeTimeout(Duration.ofMillis(1000))
			.dispatcher(dispatcher)
			.eventListener(scenarioLogger)
			.build();
	}

	private Callback buildCallback(AtomicInteger completedRequests, ScenarioLogger scenarioLogger, Integer requestId, ResponseValidator responseValidator) {
		return new Callback() {

			private final int REQUEST_ID = requestId;

			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				completedRequests.incrementAndGet();
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) {
				completedRequests.incrementAndGet();
				if (responseValidator.validateResponse(REQUEST_ID, response)) {
					scenarioLogger.recordValidRequest();
				} else {
					System.err.println("Request #" + REQUEST_ID + " is not valid");
				}
				response.close();
			}
		};
	}

	@SuppressWarnings("BusyWait")
	public void run() throws InterruptedException {
		System.out.println();
		System.out.printf("Executing scenario %s%n", scenario.getName());
		System.out.println(scenario.getDescription());
		System.out.println(scenario.getRequestBuilderDescription());
		System.out.printf("This should take a total of %.2f seconds%n", scenario.getDurationInSeconds());
		final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
		final OkHttpClient client = buildOkHttpClient(executorService);
		timeKeeper.resetTime();
		timeKeeper.setTickFunction(scenarioLogger::maybeCreateDataPoint);
		int scheduledRequests = 0;
		AtomicInteger completedRequests = new AtomicInteger();
		int currentRequest = 0;
		int expectedRequestCount;
		float lastUpdate = timeKeeper.now();
		while (!scenario.isOver(timeKeeper.now())) {
			expectedRequestCount = scenario.getRequestCountAtTime(timeKeeper.now());
			long missingRequests = expectedRequestCount - scheduledRequests;
			for (int i = 0; i < missingRequests; i++) {
				if (scenarioLogger.getInflightRequests() > scenario.getMaximumInflightRequests()) {
					continue;
				}
				client.newCall(scenario.buildRequest(currentRequest)).enqueue(buildCallback(completedRequests, scenarioLogger, currentRequest, scenario));
				currentRequest++;
				scheduledRequests++;
				if (timeKeeper.now() - lastUpdate > 2) {
					System.out.println("    at 100% capacity with virtual threads :(");
					break;
				}
			}
			if (timeKeeper.now() - lastUpdate > 1) {
				System.out.printf("  time is %.2f, scheduled %d requests so far%n", timeKeeper.now(), scheduledRequests);
				lastUpdate = (int) timeKeeper.now();
			}
		}
		scenarioLogger.createDataPoint();
		System.out.printf("Scenario done. Scheduled %d requests in %.2f seconds (expected %.2f)%n", scheduledRequests, timeKeeper.now(), scenario.getDurationInSeconds());
		System.out.println("Waiting for any remaining requests to complete...");
		lastUpdate = timeKeeper.now();
		while (scheduledRequests > completedRequests.get()) {
			Thread.sleep((long) (ScenarioLogger.RESOLUTION * 1000));
			if (lastUpdate < timeKeeper.now() - 1) {
				System.out.printf("  time is %.2f, waiting for another %d requests to finish%n", timeKeeper.now(), scheduledRequests - completedRequests.get());
				lastUpdate = (int) timeKeeper.now();
			}
		}
		scenarioLogger.createDataPoint();
		executorService.shutdown();
		System.out.printf("All done in %.2f seconds total %n", timeKeeper.now());
	}

}
