package io.github.xandewe.pricewatch.store;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreNameNormalizerTest {

	private final StoreNameNormalizer normalizer = new StoreNameNormalizer();

	@Test
	void cleansLeadingTrailingAndRepeatedWhitespace() {
		assertThat(normalizer.clean("  Trusted \t Store  "))
				.isEqualTo("Trusted Store");
	}

	@Test
	void normalizesCaseAndWhitespaceForDuplicateDetection() {
		assertThat(normalizer.normalize("  TRUSTED \t Store  "))
				.isEqualTo("trusted store");
	}
}
