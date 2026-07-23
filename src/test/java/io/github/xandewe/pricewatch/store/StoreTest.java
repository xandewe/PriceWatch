package io.github.xandewe.pricewatch.store;

import io.github.xandewe.pricewatch.product.Product;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreTest {

	@Test
	void createsAnActiveStore() {
		var store = new Store("Trusted Store", "trusted store", "https://store.example");

		assertThat(store.isActive()).isTrue();
		assertThat(store.getDeletedAt()).isNull();
	}

	@Test
	void updatesEditableData() {
		var store = new Store("Trusted Store", "trusted store", "https://store.example");

		store.update("Updated Store", "updated store", null);

		assertThat(store.getName()).isEqualTo("Updated Store");
		assertThat(store.getNormalizedName()).isEqualTo("updated store");
		assertThat(store.getWebsiteUrl()).isNull();
	}

	@Test
	void deactivatesAndRestoresAStore() {
		var store = new Store("Trusted Store", "trusted store", null);
		var deletionTime = Instant.parse("2026-07-23T12:00:00Z");

		store.deactivate(deletionTime);

		assertThat(store.isActive()).isFalse();
		assertThat(store.getDeletedAt()).isEqualTo(deletionTime);

		store.restore();

		assertThat(store.isActive()).isTrue();
		assertThat(store.getDeletedAt()).isNull();
	}

	@Test
	void rejectsAProductListingForAnInactiveStore() {
		var store = new Store("Trusted Store", "trusted store", null);
		store.deactivate(Instant.parse("2026-07-23T12:00:00Z"));
		var product = new Product("Notebook", "notebook", null, null);

		assertThatThrownBy(() -> new ProductStore(
				product,
				store,
				"https://store.example/notebook",
				"https://store.example/notebook",
				null))
				.isInstanceOf(InactiveStoreException.class)
				.hasMessage("Inactive stores cannot receive new listings or prices");
	}
}
