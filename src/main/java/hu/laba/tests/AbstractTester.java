package hu.laba.tests;

import hu.laba.Configuration;
import hu.laba.RequestResponseContext;
import hu.laba.ResponseValidatorFunction;
import hu.laba.scenarios.Scenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractTester implements Tester {

	protected final Configuration configuration;
	protected final Map<Integer, ResponseValidatorFunction> validators = new ConcurrentHashMap<>();
	protected Scenario scenario;
	protected String summary = "";

	public AbstractTester(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getSummary() {
		return summary;
	}

	@Override
	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	@Override
	public abstract RequestResponseContext buildRequest(int requestId);

	@Override
	public final void validateResponse(RequestResponseContext context) {
		validators.get(context.requestId).accept(context);
		if (!context.isValid()) {
			dumpInvalidResponse(context);
		}
	}

	protected void dumpInvalidResponse(RequestResponseContext context) {
		if (configuration.dump) {
			System.err.printf("Request #%d is invalid. See dump for details.%n", context.requestId);
			String output = "== Response to request #%d was invalid. Reasons:%n".formatted(context.requestId);
			output += context.getErrorMessages().stream()
				.map(m -> "  " + m + "\n")
				.collect(Collectors.joining());
			// Request
			output += "== %s request went to %s%n".formatted(context.request.method(), context.request.url());
			output += "== Request headers were: %n%s%n".formatted(context.request.headers());
			// Response
			output += "== Response status code was %d%n".formatted(context.getResponse().code());
			output += "== Response headers were: %n%s%n".formatted(context.getResponse().headers());
			String responseBody = context.getResponseBody();
			if (!responseBody.isBlank()) {
				output += "== Response body was:%n%s%n".formatted(responseBody);
			} else {
				output += "== Response body was empty.\n";
			}
			Path outputFilePath = configuration.dumpDirectory.resolve("%s-%d.txt".formatted(context.scenario.getIdentifier(), context.requestId));
			try {
				if (Files.exists(outputFilePath)) {
					System.err.println("Overwriting existing dump file: " + outputFilePath);
				}
				Files.writeString(outputFilePath, output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException exception) {
				System.err.printf("Failed to write dump file to %s: %s%n", outputFilePath, exception);
				System.err.print(output);
			}
		} else {
			System.err.printf("Request #%d is invalid. Enable dumping for details.%n", context.requestId);
		}
	}

}
