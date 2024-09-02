package me.hogejo.highload.stress.scenarios;

import dagger.Module;
import dagger.Provides;
import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.tests.CountVehicleTester;
import me.hogejo.highload.stress.tests.CreateVehicleTester;
import me.hogejo.highload.stress.tests.GetVehicleTester;
import me.hogejo.highload.stress.tests.SearchVehicleTester;
import me.hogejo.highload.stress.tests.StartVehicleTester;
import me.hogejo.highload.stress.tests.StressVehicleTester;
import me.hogejo.highload.stress.tests.VehicleTracker;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Module
public interface ScenarioModule {

	@Provides
	@Singleton
	static List<Scenario> provideScenarios(Configuration configuration, VehicleTracker vehicleTracker) {
		List<Scenario> scenarios = new ArrayList<>();
		WaitScenario wait3sScenario = new WaitScenario(3);
		SingleScenario countScenario = new SingleScenario("count", new CountVehicleTester(vehicleTracker, configuration));
		scenarios.add(
			new ConstantScenario(
				"start", 10, 10,
				new StartVehicleTester(configuration, vehicleTracker)
			)
				.withMaximumInFlightRequests(10)
		);
		scenarios.add(
			new ConstantScenario(
				"create", 100, 60,
				new CreateVehicleTester(configuration, vehicleTracker)
			)
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(
			new ConstantScenario(
				"get", 100, 60,
				new GetVehicleTester(configuration, vehicleTracker)
			)
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(
			new ConstantScenario(
				"search", 100, 60,
				new SearchVehicleTester(configuration, vehicleTracker)
			)
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(wait3sScenario);
		scenarios.add(countScenario);
		scenarios.add(
			new ConstantScenario(
				"stress-constant-500", 500, 60,
				new StressVehicleTester(configuration, vehicleTracker)
			)
		);
		scenarios.add(wait3sScenario);
		scenarios.add(countScenario);
		scenarios.add(
			new LinearScenario("stress-linear-25k", 25_000, 100,
				new StressVehicleTester(configuration, vehicleTracker)
			)
		);
		scenarios.add(wait3sScenario);
		scenarios.add(countScenario);
		scenarios.add(
			new BurstScenario("stress-burst", 5000, 5,
				new StressVehicleTester(configuration, vehicleTracker)
			)
		);
		scenarios.add(wait3sScenario);
		scenarios.add(countScenario);
		scenarios.add(
			new LinearScenario("stress-linear-100k", 100_000, 100,
				new StressVehicleTester(configuration, vehicleTracker)
			)
		);
		scenarios.add(wait3sScenario);
		scenarios.add(countScenario);
		return scenarios;
	}

}
