package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)
class DatabaseConfigurationIntegrationTests {

	@Autowired
	private Environment environment;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void startsWithPostgreSql17() {
		Integer serverVersion = jdbcTemplate.queryForObject(
				"SELECT current_setting('server_version_num')::integer",
				Integer.class);

		assertThat(serverVersion).isBetween(170000, 179999);
	}

	@Test
	void createsMvpDomainTablesThroughLiquibase() {
		List<String> tableNames = jdbcTemplate.queryForList("""
				SELECT table_name
				FROM information_schema.tables
				WHERE table_schema = 'public'
				ORDER BY table_name
				""", String.class);

		assertThat(tableNames).containsExactly(
				"databasechangelog",
				"databasechangeloglock",
				"prices",
				"product_stores",
				"products",
				"stores");
	}

	@Test
	void configuresLiquibaseAsTheSchemaManager() {
		assertThat(environment.getProperty("spring.liquibase.change-log"))
				.isEqualTo("classpath:db/changelog/db.changelog-master.yaml");
		assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
				.isEqualTo("validate");
		assertThat(environment.getProperty("spring.jpa.open-in-view", Boolean.class))
				.isFalse();
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class PostgreSQLTestConfiguration {

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgreSQLContainer() {
			return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
		}
	}
}
