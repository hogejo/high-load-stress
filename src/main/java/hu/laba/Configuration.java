package hu.laba;

import com.beust.jcommander.Parameter;

public class Configuration {

	@Parameter(names = "--endpoint", description = "Endpoint to run against in `host:port` format")
	public String endpoint = "localhost:8080";

	@Parameter(names = "--help", help = true, description = "Show help/usage")
	public boolean help = false;

	@Parameter(names = "--timelineOutput", description = "Output of timeline CSV")
	public String timelineOutput = "./timeline.csv";

}
