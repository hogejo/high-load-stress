package hu.laba.scenarios;

import dagger.Module;
import dagger.Provides;
import hu.laba.Configuration;
import hu.laba.tests.CountVehicleTester;
import hu.laba.tests.CreateVehicleTester;
import hu.laba.tests.GetVehicleTester;
import hu.laba.tests.SearchVehicleTester;
import hu.laba.tests.StartVehicleTester;
import hu.laba.tests.VehicleTracker;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Module
public interface ScenarioModule {

	@Provides
	@Singleton
	static List<Scenario> provideScenarios(Configuration configuration, VehicleTracker vehicleTracker) {
		List<Scenario> scenarios = new ArrayList<>();
		WaitScenario wait5sScenario = new WaitScenario(5);
		SingleScenario countScenario = new SingleScenario("count", new CountVehicleTester(vehicleTracker, configuration));
		scenarios.add(
			new ConstantScenario("start", 10, 10, new StartVehicleTester(configuration, vehicleTracker))
				.withMaximumInFlightRequests(10)
		);
		scenarios.add(
			new ConstantScenario("create", 100, 60, new CreateVehicleTester(configuration, vehicleTracker))
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(
			new ConstantScenario("get", 100, 60, new GetVehicleTester(configuration, vehicleTracker))
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(
			new ConstantScenario("search", 100, 60, new SearchVehicleTester(configuration, vehicleTracker))
				.withMaximumInFlightRequests(100)
		);
		scenarios.add(wait5sScenario);
		scenarios.add(countScenario);
		scenarios.add(wait5sScenario);
		// TODO: Continue here
		return scenarios;
	}

}
