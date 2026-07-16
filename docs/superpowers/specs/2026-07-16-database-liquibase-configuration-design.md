# Database, Hibernate and Liquibase configuration

## Goal

Configure the PriceWatch application to connect to PostgreSQL using the environment variables already established by Docker Compose. Liquibase must be the only schema-management mechanism, while Hibernate only validates the schema and does not keep persistence sessions open during web rendering.

## Scope

The change is limited to GitHub issue #3:

- external datasource configuration;
- Hibernate validation settings;
- an empty Liquibase master changelog;
- documentation for future formatted SQL migrations;
- an integration test backed by a real PostgreSQL 17 container.

The implementation must not add domain entities, business tables, fictitious tables, domain migrations, authentication, integrations, or new application features.

## Configuration design

`src/main/resources/application.yaml` will build the JDBC URL from `DB_HOST` and `DB_PORT`, both with the defaults required by the issue, and from the required `POSTGRES_DB` value. Credentials will come from `POSTGRES_USER` and `POSTGRES_PASSWORD` without application defaults. The same variables are already used by `compose.yaml` and `.env.example`.

JPA configuration will set:

- `spring.jpa.hibernate.ddl-auto: validate`;
- `spring.jpa.open-in-view: false`.

Liquibase will explicitly reference `classpath:db/changelog/db.changelog-master.yaml`. The master changelog will contain an empty `databaseChangeLog` collection, so Liquibase initializes its control tables without introducing any domain schema.

## Migration convention

The root README will document that the YAML master file is the single changelog entry point. Future schema changes must be authored as Liquibase formatted SQL files and included by the master YAML in execution order. Existing migrations must not be edited after execution.

No formatted SQL migration is created by this issue because the issue explicitly prohibits domain or fictitious tables.

## Integration test design

A dedicated `@SpringBootTest` will start `postgres:17-alpine` through Testcontainers. A static `PostgreSQLContainer` annotated with `@ServiceConnection` will provide connection details to Spring Boot, replacing the external datasource values only for the test.

The test will use `JdbcTemplate` against PostgreSQL's `information_schema` to verify that:

- the application context starts against PostgreSQL 17;
- `databasechangelog` exists;
- `databasechangeloglock` exists;
- no additional application tables were created in the `public` schema.

The Spring environment will also be asserted to contain `validate` for Hibernate schema handling and `false` for Open Session in View. Together, the empty changelog, the table inspection, and the configured Hibernate mode demonstrate that Liquibase initializes schema control while Hibernate neither creates nor alters application tables.

## Error and startup behavior

Outside tests, missing `POSTGRES_DB`, `POSTGRES_USER`, or `POSTGRES_PASSWORD` must fail configuration rather than silently connecting with development credentials. `DB_HOST` defaults to `localhost` and `DB_PORT` defaults to `5432`, matching the issue.

Liquibase runs during normal application startup. Any invalid migration or schema mismatch must fail startup rather than being hidden.

## Validation

The implementation is complete only after all of the following succeed:

- the focused PostgreSQL integration test;
- `.\mvnw.cmd test`;
- `.\mvnw.cmd package` or an equivalent Maven build that compiles, tests, and packages the application;
- a final diff review confirming that no domain schema or unrelated change was introduced.
