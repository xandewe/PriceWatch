package io.github.xandewe.pricewatch;

import io.github.xandewe.pricewatch.pricing.Availability;
import io.github.xandewe.pricewatch.pricing.Price;
import io.github.xandewe.pricewatch.product.Product;
import io.github.xandewe.pricewatch.store.ProductStore;
import io.github.xandewe.pricewatch.store.Store;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)
class DomainPersistenceIntegrationTests {

	@Autowired
	private EntityManager entityManager;

	@Test
	void persistsTheMvpDomainAndPreservesPriceHistory() {
		var product = new Product(
				"Notebook",
				"notebook",
				"Portable computer",
				new BigDecimal("4500.00"));
		var store = new Store(
				"Trusted Store",
				"trusted-store",
				"https://store.example");
		var listing = new ProductStore(
				product,
				store,
				"https://store.example/notebook",
				"https://store.example/notebook",
				"NB-001");
		var firstPrice = new Price(
				listing,
				new BigDecimal("4299.90"),
				Availability.AVAILABLE,
				"Launch offer",
				Instant.parse("2026-07-22T12:00:00Z"));
		var secondPrice = new Price(
				listing,
				new BigDecimal("4199.90"),
				Availability.AVAILABLE,
				null,
				Instant.parse("2026-07-22T13:00:00Z"));

		entityManager.persist(product);
		entityManager.persist(store);
		entityManager.persist(listing);
		entityManager.persist(firstPrice);
		entityManager.persist(secondPrice);
		entityManager.flush();

		assertThat(product.getId()).isNotNull();
		assertThat(store.getId()).isNotNull();
		assertThat(listing.getId()).isNotNull();
		assertThat(firstPrice.getId()).isNotNull();
		assertThat(product.getCreatedAt()).isNotNull();
		assertThat(product.getUpdatedAt()).isNotNull();
		assertThat(store.getCreatedAt()).isNotNull();
		assertThat(listing.getCreatedAt()).isNotNull();
		assertThat(firstPrice.getCreatedAt()).isNotNull();

		entityManager.clear();

		ProductStore persistedListing = entityManager.find(ProductStore.class, listing.getId());
		assertThat(persistedListing.getProduct().getId()).isEqualTo(product.getId());
		assertThat(persistedListing.getStore().getId()).isEqualTo(store.getId());

		var priceHistory = entityManager.createQuery("""
				SELECT price
				FROM Price price
				WHERE price.productStore.id = :listingId
				ORDER BY price.recordedAt
				""", Price.class)
				.setParameter("listingId", listing.getId())
				.getResultList();

		assertThat(priceHistory)
				.extracting(Price::getAmount)
				.containsExactly(new BigDecimal("4299.90"), new BigDecimal("4199.90"));
		assertThat(priceHistory)
				.extracting(Price::getRecordedAt)
				.containsExactly(
						Instant.parse("2026-07-22T12:00:00Z"),
						Instant.parse("2026-07-22T13:00:00Z"));
	}
}
