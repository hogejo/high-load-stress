package hu.laba;

import com.beust.jcommander.JCommander;
import hu.laba.dagger.DaggerProductionComponent;
import hu.laba.dagger.ProductionComponent;
import hu.laba.scenarios.Scenario;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Application {

	public static void main(String[] arguments) {
		Configuration configuration = new Configuration();
		JCommander jCommander = JCommander.newBuilder()
			.addObject(configuration)
			.build();
		jCommander.parse(arguments);
		if (configuration.help) {
			System.out.println("Run (entire) official stress test against HTTP endpoint.");
			System.out.println();
			jCommander.setProgramName("stress");
			jCommander.usage();
			return;
		}
		configuration.validate();
		ProductionComponent productionComponent = DaggerProductionComponent.builder()
			.configuration(configuration)
			.build();
		List<Scenario> scenarios = productionComponent.scenarios();
		if (configuration.listScenarios) {
			System.out.println("Available scenarios are:");
			listUniqueScenarios(scenarios);
			return;
		}
		if (configuration.scenario != null && !configuration.scenario.isBlank()) {
			Optional<Scenario> optionalScenario = scenarios.stream().filter(s -> s.getIdentifier().equals(configuration.scenario)).findFirst();
			if (optionalScenario.isEmpty()) {
				System.out.println("Can't find scenario: " + configuration.scenario);
				return;
			} else {
				scenarios.clear();
				scenarios.add(optionalScenario.get());
			}
		}
		Stress stress = productionComponent.stress();
		try {
			stress.run();
		} catch (InterruptedException exception) {
			System.err.println("Exception while running application: " + exception.getMessage());
			exception.printStackTrace(System.err);
			System.exit(1);
		}
	}

	public static void listUniqueScenarios(List<Scenario> scenarios) {
		listScenarios(scenarios.stream().distinct().sorted(Comparator.comparing(Scenario::getIdentifier)).toList());
	}

	public static void listScenarios(List<Scenario> scenarios) {
		int width = scenarios.stream().map(s -> s.getIdentifier().length()).max(Integer::compareTo).orElse(0);
		for (Scenario scenario : scenarios) {
			System.out.printf("  %-" + width + "s : %s", scenario.getIdentifier(), scenario.getDescription());
			if (scenario.getTotalRequests() > 0) {
				System.out.printf(" (%s)", scenario.getTester().getDescription());
			}
			System.out.println();
		}
	}

}
