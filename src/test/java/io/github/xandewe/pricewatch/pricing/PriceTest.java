package io.github.xandewe.pricewatch.pricing;

import io.github.xandewe.pricewatch.product.Product;
import io.github.xandewe.pricewatch.store.InactiveStoreException;
import io.github.xandewe.pricewatch.store.ProductStore;
import io.github.xandewe.pricewatch.store.Store;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

	@Test
	void rejectsAPriceForAListingWhoseStoreBecameInactive() {
		var product = new Product("Notebook", "notebook", null, null);
		var store = new Store("Trusted Store", "trusted store", null);
		var listing = new ProductStore(
				product,
				store,
				"https://store.example/notebook",
				"https://store.example/notebook",
				null);
		store.deactivate(Instant.parse("2026-07-23T12:00:00Z"));

		assertThatThrownBy(() -> new Price(
				listing,
				new BigDecimal("3999.90"),
				Availability.AVAILABLE,
				null,
				Instant.parse("2026-07-23T13:00:00Z")))
				.isInstanceOf(InactiveStoreException.class)
				.hasMessage("Inactive stores cannot receive new listings or prices");
	}
}
