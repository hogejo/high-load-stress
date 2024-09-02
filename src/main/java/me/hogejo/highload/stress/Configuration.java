package me.hogejo.highload.stress;

import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Configuration {

	@Parameter(order = 0, names = "--help", help = true, description = "Show help/usage")
	public boolean help = false;

	@Parameter(order = 10, names = "--listScenarios", description = "List available scenarios to run, then exit.")
	public boolean listScenarios = false;

	@Parameter(order = 11, names = {"--scenario", "--scenarios"}, description = "Scenarios to run")
	public List<String> scenarios = List.of();

	@Parameter(order = 20, names = "--endpoint", description = "Endpoint to run against")
	public String endpoint = "localhost:8080";

	@Parameter(order = 30, names = "--timelineOutput", description = "Output of timeline CSV")
	public Path timelineOutput = Path.of("./timeline.csv");

	@Parameter(order = 40, names = "--dump", description = "Whether to dump invalid and/or failed responses")
	public boolean dump = false;

	@Parameter(order = 41, names = "--dumpDirectory", description = "Output directory for dumping request response details (existing files will be overwritten).")
	public Path dumpDirectory = Path.of("./dump");

	@SuppressWarnings("HttpUrlsUsage")
	public void validate() {
		if (Files.exists(timelineOutput) && !Files.isWritable(timelineOutput)) {
			System.err.println("Can't write to file: " + timelineOutput);
			System.exit(1);
		}
		if (!Files.exists(timelineOutput) && !Files.isWritable(timelineOutput.getParent())) {
			System.err.println("Can't create file: " + timelineOutput.getParent());
			System.exit(1);
		}
		if (dump) {
			if (!Files.exists(dumpDirectory)) {
				try {
					Files.createDirectory(dumpDirectory);
					System.out.println("Created directory for dumping invalid responses: " + dumpDirectory);
				} catch (IOException exception) {
					System.err.printf("Can't create directory %s: %s%n", dumpDirectory, exception);
					System.exit(1);
				}
			}
			if (!Files.isDirectory(dumpDirectory)) {
				System.err.println("Dump directory is not a directory: " + dumpDirectory);
				System.exit(1);
			}
		}
		if (!endpoint.startsWith("http://")) {
			endpoint = "http://" + endpoint;
		}
	}

}
