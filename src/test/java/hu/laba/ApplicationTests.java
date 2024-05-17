package hu.laba;

import org.junit.jupiter.api.Test;

public class ApplicationTests {

	@Test
	public void help() {
		Application.main(new String[]{"--help"});
	}

	@Test
	public void listScenarios() {
		Application.main(new String[]{"--listScenarios"});
	}

}
