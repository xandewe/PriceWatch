# PriceWatch MVP Domain Schema Design

## Scope

Issue #8 establishes the initial persisted domain for products, stores, product-store listings, and price history. The implementation is limited to domain entities, the availability enum, versioned Liquibase migrations, database constraints and indexes, PostgreSQL integration tests, and modular architecture validation. Controllers, REST endpoints, repositories, price calculations, authentication, and automatic price collection remain out of scope.

## Domain model

The existing Spring Modulith boundaries remain unchanged:

- `product` owns `Product`;
- `store` owns `Store` and `ProductStore`;
- `pricing` owns `Price` and `Availability`.

The dependency direction is `pricing -> store -> product`, which is acyclic. The entities are JPA-backed domain objects so Hibernate can validate that their mappings match the schema managed by Liquibase. Identifiers use application-generated UUID values, as explicitly agreed for this issue.

The required relationships are:

- one `Product` has many `ProductStore` records;
- one `Store` has many `ProductStore` records;
- one `ProductStore` has many immutable historical `Price` records.

No cascading physical deletion is configured. Product, store, and product-store lifecycle state is represented by `active` and nullable `deletedAt` fields. Price records have no deletion field, and new observations are stored as new records.

## UTC timestamps

All domain timestamps use `Instant` in Java and `timestamp with time zone` in PostgreSQL. This represents an absolute point in time and prevents the server's local timezone from changing the meaning or ordering of stored events. `createdAt` values are set on insertion, `updatedAt` values are refreshed on update, `deletedAt` remains nullable, and `recordedAt` represents when a price was observed.

## Database schema

Liquibase is the only schema manager. A new formatted SQL changelog creates these tables and is included by `db.changelog-master.yaml`.

### `products`

- `id uuid` primary key;
- `name varchar(255)` not null;
- `normalized_name varchar(255)` not null and unique;
- `description text` nullable;
- `desired_price numeric(19,2)` nullable;
- `active boolean` not null;
- `created_at timestamptz` not null;
- `updated_at timestamptz` not null;
- `deleted_at timestamptz` nullable;
- check constraint allowing `desired_price` only when null or greater than zero.

### `stores`

- `id uuid` primary key;
- `name varchar(255)` not null;
- `normalized_name varchar(255)` not null and unique;
- `website_url varchar(2048)` nullable;
- `active boolean` not null;
- `created_at timestamptz` not null;
- `updated_at timestamptz` not null;
- `deleted_at timestamptz` nullable.

### `product_stores`

- `id uuid` primary key;
- `product_id uuid` not null with a foreign key to `products`;
- `store_id uuid` not null with a foreign key to `stores`;
- `url varchar(2048)` not null;
- `normalized_url varchar(2048)` not null;
- `external_code varchar(255)` nullable;
- `active boolean` not null;
- `created_at timestamptz` not null;
- `updated_at timestamptz` not null;
- `deleted_at timestamptz` nullable;
- unique constraint on `(store_id, normalized_url)` without filtering by lifecycle state, so inactive listings continue reserving their URL.

### `prices`

- `id uuid` primary key;
- `product_store_id uuid` not null with a foreign key to `product_stores`;
- `amount numeric(19,2)` not null;
- `availability varchar(20)` not null;
- `note text` nullable;
- `recorded_at timestamptz` not null;
- `created_at timestamptz` not null;
- check constraint requiring `amount > 0`;
- check constraint accepting only `AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN`.

Foreign keys use PostgreSQL's restrictive default deletion behavior. They do not cascade deletion into listings or price history.

## Indexes

The migration adds focused indexes for the query dimensions required by the issue:

- products by active status;
- stores by active status;
- product-store listings by product and status;
- product-store listings by store and status;
- prices by listing and observation date in descending order;
- prices by availability;
- prices by observation date.

Unique constraints also provide indexes for normalized product names, normalized store names, and normalized listing URLs within a store.

## Hibernate and Liquibase responsibilities

`spring.jpa.hibernate.ddl-auto` remains `validate`. Startup order is Liquibase migration followed by Hibernate mapping validation. Hibernate never creates, updates, or repairs database objects. Future schema changes must be delivered through new changelogs; executed migrations are not edited.

## Validation strategy

PostgreSQL 17 integration tests run through Testcontainers and verify:

- Liquibase creates all four domain tables;
- Hibernate starts successfully with schema validation enabled;
- required foreign keys and relationships reject invalid references;
- normalized product and store names are unique;
- normalized listing URLs are unique per store even for inactive listings;
- the same normalized URL can exist in different stores;
- desired prices and recorded prices obey their positive-value constraints;
- availability accepts only the three required values;
- multiple price records can coexist for one listing, preserving history;
- database timestamps use timezone-aware PostgreSQL types;
- the Spring Modulith architecture remains valid.

The existing full test suite and Maven verification build are run before publication.
