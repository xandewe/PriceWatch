# PriceWatch MVP Domain Schema Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist the issue #8 MVP domain with Liquibase-managed PostgreSQL tables, matching JPA entities, database invariants, UTC timestamps, and integration coverage.

**Architecture:** Liquibase remains the exclusive schema manager and Hibernate remains in validation mode. JPA-backed domain entities live in the existing `product`, `store`, and `pricing` Spring Modulith modules with an acyclic `pricing -> store -> product` dependency direction.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Data JPA, Hibernate, Liquibase formatted SQL, PostgreSQL 17, Testcontainers, JUnit 5, AssertJ, Maven.

## Global Constraints

- Implement only issue #8; do not add controllers, endpoints, repositories, authentication, scraping, notifications, or price calculations.
- Use `BigDecimal` mapped to `numeric(19,2)` for monetary values.
- Use application-generated UUID identifiers mapped to PostgreSQL `uuid`.
- Use `Instant` mapped to PostgreSQL `timestamp with time zone` for all timestamps.
- Liquibase creates and changes the schema; Hibernate stays configured with `ddl-auto: validate`.
- Preserve price history and configure no cascading physical deletion.
- Run tests against PostgreSQL 17 through Testcontainers.

---

### Task 1: Create the versioned MVP schema migration

**Files:**
- Create: `src/main/resources/db/changelog/changes/001-create-mvp-domain.sql`
- Modify: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Modify: `src/test/java/io/github/xandewe/pricewatch/DatabaseConfigurationIntegrationTests.java`
- Create: `src/test/java/io/github/xandewe/pricewatch/DomainSchemaIntegrationTests.java`

**Interfaces:**
- Consumes: the existing Liquibase master changelog and shared PostgreSQL 17 Testcontainers configuration.
- Produces: `products`, `stores`, `product_stores`, and `prices` tables with stable constraint and index names used by later entity mappings and tests.

- [ ] **Step 1: Change the existing table assertion and add failing schema tests**

Update `createsOnlyLiquibaseControlTables()` to `createsMvpDomainTablesThroughLiquibase()` and assert these exact public tables:

```java
assertThat(tableNames).containsExactly(
        "databasechangelog",
        "databasechangeloglock",
        "prices",
        "product_stores",
        "products",
        "stores");
```

Create `DomainSchemaIntegrationTests` using `@SpringBootTest`, `@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)`, and `JdbcTemplate`. Add tests that query `information_schema.columns` and `pg_indexes` to require timezone-aware timestamp columns and the issue's indexes before the migration exists.

- [ ] **Step 2: Run the focused tests and verify the red state**

Run:

```powershell
& $MAVEN_BIN -Dtest=DatabaseConfigurationIntegrationTests,DomainSchemaIntegrationTests test
```

Expected: failure because the four domain tables and their indexes do not exist.

- [ ] **Step 3: Add the formatted SQL migration**

Create a Liquibase formatted SQL changeset that defines:

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    description TEXT,
    desired_price NUMERIC(19, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_products_normalized_name UNIQUE (normalized_name),
    CONSTRAINT ck_products_desired_price_positive CHECK (desired_price IS NULL OR desired_price > 0)
);

CREATE TABLE stores (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    website_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_stores_normalized_name UNIQUE (normalized_name)
);

CREATE TABLE product_stores (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    store_id UUID NOT NULL,
    url VARCHAR(2048) NOT NULL,
    normalized_url VARCHAR(2048) NOT NULL,
    external_code VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_product_stores_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_stores_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT uk_product_stores_store_normalized_url UNIQUE (store_id, normalized_url)
);

CREATE TABLE prices (
    id UUID PRIMARY KEY,
    product_store_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    availability VARCHAR(20) NOT NULL,
    note TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prices_product_store FOREIGN KEY (product_store_id) REFERENCES product_stores (id),
    CONSTRAINT ck_prices_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_prices_availability CHECK (availability IN ('AVAILABLE', 'UNAVAILABLE', 'UNKNOWN'))
);
```

Add indexes for `products(active)`, `stores(active)`, `product_stores(product_id, active)`, `product_stores(store_id, active)`, `prices(product_store_id, recorded_at DESC)`, `prices(availability)`, and `prices(recorded_at)`. Include the SQL file from the YAML master changelog.

- [ ] **Step 4: Run the focused schema tests**

Run the same Maven command. Expected: the table, UTC type, and index assertions pass.

- [ ] **Step 5: Commit the schema task**

```powershell
git add src/main/resources/db/changelog src/test/java/io/github/xandewe/pricewatch/DatabaseConfigurationIntegrationTests.java src/test/java/io/github/xandewe/pricewatch/DomainSchemaIntegrationTests.java
git commit -m "feat(database): add MVP domain schema migration"
```

---

### Task 2: Map the domain entities and availability enum

**Files:**
- Create: `src/main/java/io/github/xandewe/pricewatch/product/Product.java`
- Create: `src/main/java/io/github/xandewe/pricewatch/store/Store.java`
- Create: `src/main/java/io/github/xandewe/pricewatch/store/ProductStore.java`
- Create: `src/main/java/io/github/xandewe/pricewatch/pricing/Availability.java`
- Create: `src/main/java/io/github/xandewe/pricewatch/pricing/Price.java`
- Create: `src/test/java/io/github/xandewe/pricewatch/DomainPersistenceIntegrationTests.java`

**Interfaces:**
- Consumes: the exact table and column names created by Task 1.
- Produces: public JPA entity types with UUID identifiers, `BigDecimal` money, `Instant` timestamps, lazy required `ManyToOne` associations, and no remove cascades.

- [ ] **Step 1: Write a persistence test against the desired entity API**

Create a transactional integration test using `EntityManager` that constructs and persists:

```java
var product = new Product("Notebook", "notebook", "Portable computer", new BigDecimal("4500.00"));
var store = new Store("Trusted Store", "trusted-store", "https://store.example");
var listing = new ProductStore(product, store, "https://store.example/notebook", "https://store.example/notebook", "NB-001");
var firstPrice = new Price(listing, new BigDecimal("4299.90"), Availability.AVAILABLE, "Launch offer", Instant.parse("2026-07-22T12:00:00Z"));
var secondPrice = new Price(listing, new BigDecimal("4199.90"), Availability.AVAILABLE, null, Instant.parse("2026-07-22T13:00:00Z"));
```

Persist them, flush and clear the persistence context, then assert generated UUIDs, UTC timestamps, required relationships, and two distinct price rows for the same listing.

- [ ] **Step 2: Run the persistence test and verify the red state**

Run:

```powershell
& $MAVEN_BIN -Dtest=DomainPersistenceIntegrationTests test
```

Expected: test compilation fails because the five domain types do not exist.

- [ ] **Step 3: Implement minimal JPA mappings**

For every entity, use field mappings with:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Map money with `@Column(precision = 19, scale = 2)`, lifecycle timestamps with `@CreationTimestamp` and `@UpdateTimestamp`, and all timestamps as `Instant`. Map `ProductStore.product`, `ProductStore.store`, and `Price.productStore` using required lazy `@ManyToOne` associations without cascade options. Map availability with `@Enumerated(EnumType.STRING)`. Provide the protected no-argument constructor required by JPA, the public constructor used by the test, and focused getters; do not add deletion operations, repositories, or services.

- [ ] **Step 4: Run persistence and modularity tests**

Run:

```powershell
& $MAVEN_BIN -Dtest=DomainPersistenceIntegrationTests,PriceWatchModularityTests test
```

Expected: persistence passes and Spring Modulith reports no cycles or internal API violations.

- [ ] **Step 5: Commit the entity mappings**

```powershell
git add src/main/java/io/github/xandewe/pricewatch src/test/java/io/github/xandewe/pricewatch/DomainPersistenceIntegrationTests.java
git commit -m "feat(database): map MVP domain entities"
```

---

### Task 3: Enforce and test database invariants

**Files:**
- Modify: `src/test/java/io/github/xandewe/pricewatch/DomainSchemaIntegrationTests.java`

**Interfaces:**
- Consumes: named constraints from Task 1 and the PostgreSQL Testcontainers application context.
- Produces: regression coverage for every database acceptance criterion in issue #8.

- [ ] **Step 1: Add constraint and history tests**

Using `JdbcTemplate`, add isolated tests that insert valid prerequisite rows and assert `DataIntegrityViolationException` for:

- duplicate `products.normalized_name`;
- duplicate `stores.normalized_name`;
- duplicate `(store_id, normalized_url)` when the first listing is inactive;
- missing product, store, or product-store references;
- zero and negative `desired_price` or `prices.amount`;
- availability outside `AVAILABLE`, `UNAVAILABLE`, and `UNKNOWN`.

Add positive assertions that the same normalized URL is accepted for different stores and that multiple prices remain stored for one listing.

- [ ] **Step 2: Run the constraint tests**

Run:

```powershell
& $MAVEN_BIN -Dtest=DomainSchemaIntegrationTests test
```

Expected: all named invariant tests pass against PostgreSQL 17. If a test exposes a missing or incorrect migration rule, first observe the failure and then make the smallest migration correction before rerunning.

- [ ] **Step 3: Commit the invariant coverage**

```powershell
git add src/test/java/io/github/xandewe/pricewatch/DomainSchemaIntegrationTests.java src/main/resources/db/changelog/changes/001-create-mvp-domain.sql
git commit -m "test(database): cover MVP schema constraints"
```

---

### Task 4: Document and verify the completed issue

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the completed schema, mappings, and tests from Tasks 1-3.
- Produces: accurate local documentation and fresh full-suite/build evidence.

- [ ] **Step 1: Update implemented database documentation**

Replace the foundation wording that says modules only define boundaries with a concise statement that issue #8 now provides the four persisted domain tables through Liquibase, Hibernate validates them, timestamps are stored as UTC instants, and APIs remain unavailable.

- [ ] **Step 2: Validate migration configuration**

Run:

```powershell
docker compose config
```

Expected: configuration is valid when the repository's documented environment values are supplied.

- [ ] **Step 3: Run the complete test suite**

Run:

```powershell
& $MAVEN_BIN test
```

Expected: zero failures and zero errors.

- [ ] **Step 4: Run the clean verification build**

Run:

```powershell
& $MAVEN_BIN clean verify
```

Expected: `BUILD SUCCESS` with zero test failures.

- [ ] **Step 5: Commit documentation**

```powershell
git add README.md docs/superpowers/plans/2026-07-22-mvp-domain-schema.md
git commit -m "docs(database): document MVP domain schema"
```

- [ ] **Step 6: Inspect final scope**

Run `git status --short`, `git diff origin/main...HEAD --check`, and `git diff --stat origin/main...HEAD`. Confirm that only issue #8 files, worktree metadata ignore, the approved spec, and this implementation plan are present.
