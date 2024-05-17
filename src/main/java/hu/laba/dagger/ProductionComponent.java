package hu.laba.dagger;

import dagger.BindsInstance;
import dagger.Component;
import hu.laba.Configuration;
import hu.laba.Stress;
import hu.laba.scenarios.Scenario;
import hu.laba.scenarios.ScenarioModule;

import javax.inject.Singleton;
import java.util.List;

@Component(modules = {
	ScenarioModule.class,
})
@Singleton
public interface ProductionComponent {

	List<Scenario> scenarios();

	Stress stress();

	@Component.Builder
	interface Builder {

		@BindsInstance
		Builder configuration(Configuration configuration);

		ProductionComponent build();

	}

}
