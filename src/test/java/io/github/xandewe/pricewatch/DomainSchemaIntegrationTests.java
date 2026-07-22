package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)
class DomainSchemaIntegrationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void usesTimezoneAwareColumnsForEveryDomainTimestamp() {
		List<String> timezoneAwareColumns = jdbcTemplate.queryForList("""
				SELECT table_name || '.' || column_name
				FROM information_schema.columns
				WHERE table_schema = 'public'
				  AND data_type = 'timestamp with time zone'
				ORDER BY table_name, column_name
				""", String.class);

		assertThat(timezoneAwareColumns).containsExactly(
				"prices.created_at",
				"prices.recorded_at",
				"product_stores.created_at",
				"product_stores.deleted_at",
				"product_stores.updated_at",
				"products.created_at",
				"products.deleted_at",
				"products.updated_at",
				"stores.created_at",
				"stores.deleted_at",
				"stores.updated_at");
	}

	@Test
	void createsIndexesForTheRequiredQueryDimensions() {
		List<String> indexNames = jdbcTemplate.queryForList("""
				SELECT indexname
				FROM pg_indexes
				WHERE schemaname = 'public'
				  AND indexname LIKE 'idx_%'
				ORDER BY indexname
				""", String.class);

		assertThat(indexNames).containsExactly(
				"idx_prices_availability",
				"idx_prices_product_store_recorded_at",
				"idx_prices_recorded_at",
				"idx_product_stores_product_active",
				"idx_product_stores_store_active",
				"idx_products_active",
				"idx_stores_active");
	}

	@Test
	void rejectsDuplicateNormalizedProductNames() {
		insertProduct("notebook", new BigDecimal("4500.00"));

		assertThatThrownBy(() -> insertProduct("notebook", new BigDecimal("4200.00")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsDuplicateNormalizedStoreNames() {
		insertStore("trusted-store");

		assertThatThrownBy(() -> insertStore("trusted-store"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void keepsAnInactiveListingUrlReservedWithinTheStore() {
		UUID productId = insertProduct("notebook", null);
		UUID storeId = insertStore("trusted-store");
		insertProductStore(productId, storeId, "https://store.example/notebook", false);

		assertThatThrownBy(() -> insertProductStore(
				productId,
				storeId,
				"https://store.example/notebook",
				true))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void allowsTheSameNormalizedUrlInDifferentStores() {
		UUID productId = insertProduct("notebook", null);
		UUID firstStoreId = insertStore("first-store");
		UUID secondStoreId = insertStore("second-store");

		insertProductStore(productId, firstStoreId, "https://catalog.example/notebook", true);
		insertProductStore(productId, secondStoreId, "https://catalog.example/notebook", true);

		Integer listingCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM product_stores",
				Integer.class);
		assertThat(listingCount).isEqualTo(2);
	}

	@Test
	void requiresAnExistingProductForAListing() {
		UUID storeId = insertStore("trusted-store");

		assertThatThrownBy(() -> insertProductStore(
				UUID.randomUUID(),
				storeId,
				"https://store.example/notebook",
				true))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void requiresAnExistingStoreForAListing() {
		UUID productId = insertProduct("notebook", null);

		assertThatThrownBy(() -> insertProductStore(
				productId,
				UUID.randomUUID(),
				"https://store.example/notebook",
				true))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void requiresAnExistingListingForAPrice() {
		assertThatThrownBy(() -> insertPrice(
				UUID.randomUUID(),
				new BigDecimal("4299.90"),
				"AVAILABLE",
				Instant.parse("2026-07-22T12:00:00Z")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"0.00", "-0.01"})
	void rejectsNonPositiveDesiredPrices(String desiredPrice) {
		assertThatThrownBy(() -> insertProduct("notebook", new BigDecimal(desiredPrice)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"0.00", "-0.01"})
	void rejectsNonPositiveRecordedPrices(String amount) {
		UUID listingId = insertListingPrerequisites();

		assertThatThrownBy(() -> insertPrice(
				listingId,
				new BigDecimal(amount),
				"AVAILABLE",
				Instant.parse("2026-07-22T12:00:00Z")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsUnsupportedAvailability() {
		UUID listingId = insertListingPrerequisites();

		assertThatThrownBy(() -> insertPrice(
				listingId,
				new BigDecimal("4299.90"),
				"BACKORDERED",
				Instant.parse("2026-07-22T12:00:00Z")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"AVAILABLE", "UNAVAILABLE", "UNKNOWN"})
	void acceptsEverySupportedAvailability(String availability) {
		UUID listingId = insertListingPrerequisites();

		insertPrice(
				listingId,
				new BigDecimal("4299.90"),
				availability,
				Instant.parse("2026-07-22T12:00:00Z"));

		Integer priceCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM prices WHERE product_store_id = ?",
				Integer.class,
				listingId);
		assertThat(priceCount).isOne();
	}

	@Test
	void preventsDeletingAProductWithListings() {
		UUID productId = insertProduct("notebook", null);
		UUID storeId = insertStore("trusted-store");
		insertProductStore(productId, storeId, "https://store.example/notebook", true);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"DELETE FROM products WHERE id = ?",
				productId))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsDeletingAStoreWithListings() {
		UUID productId = insertProduct("notebook", null);
		UUID storeId = insertStore("trusted-store");
		insertProductStore(productId, storeId, "https://store.example/notebook", true);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"DELETE FROM stores WHERE id = ?",
				storeId))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsDeletingAListingWithPriceHistory() {
		UUID listingId = insertListingPrerequisites();
		insertPrice(
				listingId,
				new BigDecimal("4299.90"),
				"AVAILABLE",
				Instant.parse("2026-07-22T12:00:00Z"));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"DELETE FROM product_stores WHERE id = ?",
				listingId))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preservesEveryPriceRecordedForAListing() {
		UUID listingId = insertListingPrerequisites();
		insertPrice(
				listingId,
				new BigDecimal("4299.90"),
				"AVAILABLE",
				Instant.parse("2026-07-22T12:00:00Z"));
		insertPrice(
				listingId,
				new BigDecimal("4199.90"),
				"AVAILABLE",
				Instant.parse("2026-07-22T13:00:00Z"));

		List<BigDecimal> amounts = jdbcTemplate.queryForList(
				"SELECT amount FROM prices WHERE product_store_id = ? ORDER BY recorded_at",
				BigDecimal.class,
				listingId);
		assertThat(amounts).containsExactly(
				new BigDecimal("4299.90"),
				new BigDecimal("4199.90"));
	}

	private UUID insertListingPrerequisites() {
		UUID productId = insertProduct("notebook-" + UUID.randomUUID(), null);
		UUID storeId = insertStore("store-" + UUID.randomUUID());
		return insertProductStore(
				productId,
				storeId,
				"https://store.example/" + UUID.randomUUID(),
				true);
	}

	private UUID insertProduct(String normalizedName, BigDecimal desiredPrice) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO products (id, name, normalized_name, desired_price, active)
				VALUES (?, ?, ?, ?, TRUE)
				""", id, "Product", normalizedName, desiredPrice);
		return id;
	}

	private UUID insertStore(String normalizedName) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO stores (id, name, normalized_name, active)
				VALUES (?, ?, ?, TRUE)
				""", id, "Store", normalizedName);
		return id;
	}

	private UUID insertProductStore(
			UUID productId,
			UUID storeId,
			String normalizedUrl,
			boolean active) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO product_stores
				    (id, product_id, store_id, url, normalized_url, active)
				VALUES (?, ?, ?, ?, ?, ?)
				""", id, productId, storeId, normalizedUrl, normalizedUrl, active);
		return id;
	}

	private UUID insertPrice(
			UUID productStoreId,
			BigDecimal amount,
			String availability,
			Instant recordedAt) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO prices
				    (id, product_store_id, amount, availability, recorded_at)
				VALUES (?, ?, ?, ?, ?)
				""", id, productStoreId, amount, availability, recordedAt.atOffset(ZoneOffset.UTC));
		return id;
	}
}
