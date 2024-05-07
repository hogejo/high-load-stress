package hu.laba;

import hu.laba.scenarios.Scenario;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ScenarioLogger extends EventListener {

	private static final String csvHeader = "time,expectedRequests,scheduledRequests,startedRequests,successfulRequests,validResponses,failedRequests,requestsPerSecond,latency90th";

	public record DataPoint(
		float time,
		int expectedRequests,
		int scheduledRequests,
		int startedRequests,
		int successfulRequests,
		int validResponses,
		int failedRequests,
		int requestsPerSecond,
		int latency90th
	) {

		public String toCSVLine() {
			return "%.2f,%d,%d,%d,%d,%d,%d,%d,%d".formatted(
				time(),
				expectedRequests(),
				scheduledRequests(),
				startedRequests(),
				successfulRequests(),
				validResponses(),
				failedRequests(),
				requestsPerSecond(),
				latency90th()
			);
		}

		@Override
		public String toString() {
			return "time=%.2f, expectedRequests=%d, scheduledRequests=%d, startedRequests=%d, successfulRequests=%d, validResponses=%d, failedRequests=%d, requestsPerSecond=%d, latency90th=%d".formatted(
				time(),
				expectedRequests(),
				scheduledRequests(),
				startedRequests(),
				successfulRequests(),
				validResponses(),
				failedRequests(),
				requestsPerSecond(),
				latency90th()
			);
		}

	}

	public record Bucket(long upperBound, AtomicLong counter) {

		public Bucket(long upperBound) {
			this(upperBound, new AtomicLong());
		}

		public void increment() {
			counter.incrementAndGet();
		}

	}

	public static final float RESOLUTION = 0.1f;

	private Scenario scenario;
	private final TimeKeeper timeKeeper;

	// Data points
	private float lastDataPoint = -RESOLUTION * 2;
	private int expectedRequestOffset = 0;
	private float expectedRequestTimeOffset = 0;
	private final AtomicInteger scheduledRequests = new AtomicInteger();
	private final AtomicInteger startedRequests = new AtomicInteger();
	private final AtomicInteger successfulRequests = new AtomicInteger();
	private final AtomicInteger validResponses = new AtomicInteger();
	private final AtomicInteger failedRequests = new AtomicInteger();
	private final List<Integer> allLatencies = new CopyOnWriteArrayList<>();
	private final PriorityBlockingQueue<Float> requestTimestamps = new PriorityBlockingQueue<>();
	private final ConcurrentHashMap<String, AtomicInteger> failReasons = new ConcurrentHashMap<>();

	// Latency buckets
	private final Map<Call, Long> requestStartTimes = new ConcurrentHashMap<>();
	private final List<Bucket> requestTimeBuckets = new ArrayList<>();
	private final List<DataPoint> dataPoints = new ArrayList<>();

	public ScenarioLogger(TimeKeeper timeKeeper) {
		this.scenario = null;
		this.timeKeeper = timeKeeper;
		requestTimeBuckets.add(new Bucket(5));
		requestTimeBuckets.add(new Bucket(10));
		requestTimeBuckets.add(new Bucket(25));
		requestTimeBuckets.add(new Bucket(50));
		requestTimeBuckets.add(new Bucket(100));
		requestTimeBuckets.add(new Bucket(250));
		requestTimeBuckets.add(new Bucket(500));
		requestTimeBuckets.add(new Bucket(1_000));
		requestTimeBuckets.add(new Bucket(10_000));
	}

	public void setScenario(Scenario scenario) {
		if (this.scenario != null) {
			expectedRequestOffset += this.scenario.getTotalRequests();
			expectedRequestTimeOffset -= this.scenario.getDurationInSeconds();
		}
		this.scenario = scenario;
	}

	public int getCompletedRequests() {
		return successfulRequests.get() + failedRequests.get();
	}

	public int getInflightRequests() {
		return scheduledRequests.get() - getCompletedRequests();
	}

	synchronized public void createDataPoint() {
		dataPoints.add(getCurrentDataPoint());
		lastDataPoint = timeKeeper.now();
	}

	public void maybeCreateDataPoint() {
		if (timeKeeper.now() - lastDataPoint > RESOLUTION) {
			synchronized (this) {
				if (timeKeeper.now() - lastDataPoint > RESOLUTION) {
					createDataPoint();
				}
			}
		}
	}

	public DataPoint getCurrentDataPoint() {
		ArrayList<Integer> latencies = new ArrayList<>(allLatencies);
		if (latencies.isEmpty()) {
			latencies.add(0);
		} else {
			latencies.sort(Integer::compare);
		}
		int latency90th = latencies.get((int) (latencies.size() * 0.9));
		if (latency90th < latencies.size() * 0.9 && latency90th < latencies.size() - 1) {
			latency90th++;
		}
		int rps;
		float timestampCutoff = timeKeeper.now() - 1.0f;
		requestTimestamps.removeIf(f -> f < timestampCutoff);
		rps = requestTimestamps.size();
		int expectedRequests = expectedRequestOffset;
		if (scenario != null) {
			expectedRequests += scenario.getRequestCountAtTime(timeKeeper.now() + expectedRequestTimeOffset);
		}
		return new DataPoint(
			timeKeeper.now(),
			expectedRequests,
			scheduledRequests.get(),
			startedRequests.get(),
			successfulRequests.get(),
			validResponses.get(),
			failedRequests.get(),
			rps,
			latency90th
		);
	}

	public List<String> getAsCSV() {
		return Stream.concat(Stream.of(csvHeader), dataPoints.stream().map(DataPoint::toCSVLine)).toList();
	}

	public void saveAsCSV(Path path) {
		try {
			Files.write(path, getAsCSV());
		} catch (IOException exception) {
			System.err.println("Failed to write CSV file: " + exception.getMessage());
		}
	}

	private void putLatencyInBucket(long timeInMillis) {
		allLatencies.add((int) timeInMillis);
		int i = 0;
		while (i < requestTimeBuckets.size() - 1 && requestTimeBuckets.get(i).upperBound() < timeInMillis) {
			i++;
		}
		requestTimeBuckets.get(i).increment();
	}

	public List<String> getBuckets() {
		return requestTimeBuckets.stream().map(b -> "<%dms,%d".formatted(b.upperBound(), b.counter.get())).toList();
	}

	public List<String> getFailReasons() {
		return failReasons.entrySet().stream().map(e -> "%s,%d".formatted(e.getKey(), e.getValue().get())).toList();
	}

	public void recordValidRequest() {
		validResponses.incrementAndGet();
	}

	@Override
	public void callStart(@NotNull Call call) {
		super.callStart(call);
		scheduledRequests.incrementAndGet();
	}

	@Override
	public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
		super.connectionAcquired(call, connection);
		requestStartTimes.put(call, System.currentTimeMillis());
		startedRequests.incrementAndGet();
		requestTimestamps.add(timeKeeper.now());
	}

	@Override
	public void connectionReleased(@NotNull Call call, @NotNull Connection connection) {
		super.connectionReleased(call, connection);
		putLatencyInBucket(System.currentTimeMillis() - requestStartTimes.remove(call));
	}

	@Override
	public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
		super.responseHeadersEnd(call, response);
		successfulRequests.incrementAndGet();
		maybeCreateDataPoint();
	}

	@Override
	public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
		super.callFailed(call, ioe);
		failedRequests.incrementAndGet();
		maybeCreateDataPoint();
		failReasons.computeIfAbsent(ioe.getMessage(), key -> new AtomicInteger()).incrementAndGet();
	}

}
