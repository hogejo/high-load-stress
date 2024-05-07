package hu.laba;

import com.beust.jcommander.Parameter;

public class Configuration {

	@Parameter(order = 0, names = "--help", help = true, description = "Show help/usage")
	public boolean help = false;

	@Parameter(order = 10, names = "--endpoint", description = "Endpoint to run against in `host:port` format")
	public String endpoint = "localhost:8080";

	@Parameter(order = 20, names = "--timelineOutput", description = "Output of timeline CSV")
	public String timelineOutput = "./timeline.csv";

	@Parameter(order = 30, names = "--dumpRequests", description = "Whether to dump invalid responses")
	public boolean dumpRequests = false;

	@Parameter(order = 31, names = "--requestDumpDirectory", description = "Output directory for dumping invalid response details. Existing files will be overwritten.")
	public String requestDumpDirectory = "./dump";

}
