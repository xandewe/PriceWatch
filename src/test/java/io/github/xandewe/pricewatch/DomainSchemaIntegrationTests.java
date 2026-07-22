package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
}
