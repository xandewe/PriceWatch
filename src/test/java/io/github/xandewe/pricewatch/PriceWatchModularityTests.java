package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

import com.tngtech.archunit.core.importer.ImportOption;
import io.github.xandewe.pricewatchfixtures.cycle.CycleApplication;
import io.github.xandewe.pricewatchfixtures.internalaccess.InternalAccessApplication;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceWatchModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(PriceWatchApplication.class);

	@Test
	void discoversExactlyTheProductStoreAndPricingModules() {
		var identifiers = StreamSupport.stream(modules.spliterator(), false)
				.map(module -> module.getIdentifier().toString())
				.toList();

		assertThat(identifiers)
				.containsExactlyInAnyOrder("product", "store", "pricing");
	}

	@Test
	void verifiesTheModularArchitecture() {
		assertThatCode(modules::verify)
				.doesNotThrowAnyException();
	}

	@Test
	void rejectsCyclicModuleDependencies() {
		assertThatThrownBy(() -> testModulesOf(CycleApplication.class).verify())
				.isInstanceOf(Violations.class);
	}

	@Test
	void rejectsAccessToAnotherModulesInternalTypes() {
		assertThatThrownBy(() -> testModulesOf(InternalAccessApplication.class).verify())
				.isInstanceOf(Violations.class);
	}

	private static ApplicationModules testModulesOf(Class<?> rootType) {
		return ApplicationModules.of(rootType, (ImportOption) location -> true);
	}
}
