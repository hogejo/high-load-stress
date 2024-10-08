package me.hogejo.highload.stress;

import com.beust.jcommander.JCommander;
import me.hogejo.highload.stress.dagger.DaggerProductionComponent;
import me.hogejo.highload.stress.dagger.ProductionComponent;
import me.hogejo.highload.stress.scenarios.Scenario;

import java.util.ArrayList;
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
		if (!configuration.scenarios.isEmpty()) {
			List<Scenario> customScenarios = new ArrayList<>();
			for (String identifier : configuration.scenarios) {
				Optional<Scenario> optionalScenario = scenarios.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
				if (optionalScenario.isEmpty()) {
					System.out.println("Can't find scenario: " + identifier);
					return;
				} else {
					customScenarios.add(optionalScenario.get());
				}
			}
			scenarios.clear();
			scenarios.addAll(customScenarios);
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
