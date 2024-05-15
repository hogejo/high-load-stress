package hu.laba;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTests {

	@Test
	void endpointRegex_allowsValidHostnames() {
		String[] hostnames = new String[]{
			"localhost",
			"tamas-gepe",
			"TamasGepe",
			"Sandors-iMac2",
			"2balkezes",
			"gep213",
		};
		for (String hostname : hostnames) {
			String endpoint = hostname + ":1234";
			assertTrue(Configuration.endpointPattern.matcher(endpoint).matches(), "Endpoint " + endpoint + " is not valid");
		}
	}

}
