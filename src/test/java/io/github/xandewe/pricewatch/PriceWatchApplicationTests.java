package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class PriceWatchApplicationTests {

	@Test
	void declaresSpringBootApplicationEntryPoint() {
		assertThat(PriceWatchApplication.class)
				.hasAnnotation(SpringBootApplication.class);
	}

}
