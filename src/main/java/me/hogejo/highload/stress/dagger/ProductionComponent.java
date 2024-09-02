package me.hogejo.highload.stress.dagger;

import dagger.BindsInstance;
import dagger.Component;
import me.hogejo.highload.stress.Configuration;
import me.hogejo.highload.stress.Stress;
import me.hogejo.highload.stress.scenarios.Scenario;
import me.hogejo.highload.stress.scenarios.ScenarioModule;

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
