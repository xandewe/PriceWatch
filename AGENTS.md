# AGENTS.md

## Project overview

This repository contains the backend application for **PriceWatch**.

PriceWatch is a REST API for tracking product prices across trusted stores.

The application allows users to:

* Register products.
* Register stores.
* Associate products with different stores.
* Record product prices manually over time.
* View the current lowest price.
* View the historical lowest price.
* Identify the date and store where the historical lowest price was found.
* Compare recorded prices with a target price defined by the user.

In the first version, the project focuses on product management, price history and price analysis.

Automatic price collection, web scraping and external store integrations are outside the initial MVP scope.

Main technologies:

* Java 17 or later.
* Spring Boot.
* Spring Web.
* Spring Data JPA.
* PostgreSQL.
* Flyway or Liquibase for database migrations.
* Maven.
* JUnit 5.
* Mockito.
* Docker and Docker Compose.
* OpenAPI / Swagger.

## Current project stage

The project is currently in the **foundation and MVP development** stage.

Only implement functionality explicitly requested by the assigned GitHub issue or task.

Do not implement future features such as:

* Automatic price collection.
* Web scraping.
* Store API integrations.
* Notifications.
* Authentication.
* Multiple users.
* Mobile applications.
* Frontend applications.
* Messaging or asynchronous processing.

These features may only be implemented when explicitly requested by a future issue.

## Scope rules

* Work only within the scope described in the assigned task or GitHub issue.
* Do not implement future features prematurely.
* Do not make unrelated refactors.
* Do not modify unrelated files.
* Prefer the simplest solution that fully satisfies the requirement.
* Suggestions outside the requested scope may be presented at the end, but must not be implemented automatically.
* Preserve backward compatibility unless the task explicitly allows breaking changes.
* Do not introduce new dependencies without clear technical justification.
* Do not create abstractions for hypothetical future requirements.
* Do not increase the project complexity without a current and concrete need.

## Code quality

* Follow Clean Code principles.
* Apply SOLID principles when they improve maintainability.
* Prefer clear and explicit code over unnecessary abstractions.
* Keep classes and methods focused on a single responsibility.
* Use descriptive names for classes, methods, variables and tests.
* Avoid duplicated business rules.
* Avoid premature optimization.
* Do not add comments that merely repeat what the code already expresses.
* Document decisions that are not obvious from the implementation.
* Prefer domain terminology related to products, stores and prices.
* Use English for source code, class names, methods, variables and database objects.
* Keep business calculations centralized and testable.

## Architecture

Respect the architecture and conventions already established in the repository.

The expected backend responsibilities are:

### `controller`

Receives HTTP requests, validates request contracts and builds HTTP responses.

Controllers must not contain business rules or persistence logic.

### `dto`

Defines API input and output contracts.

DTOs must be used to prevent persistence entities from being exposed directly through the API.

### `application` or `usecase`

Coordinates application use cases.

Examples:

* Registering a product.
* Registering a store.
* Associating a product with a store.
* Recording a new price.
* Retrieving the current lowest price.
* Retrieving the historical lowest price.

This layer may define transaction boundaries when appropriate.

### `service`

Contains reusable domain or business operations.

Examples:

* Calculating the lowest current price.
* Calculating the historical lowest price.
* Comparing a recorded price with the target price.
* Validating product and store relationships.

Avoid creating a service that only forwards calls without adding meaningful behavior.

### `domain`

Contains business rules, domain entities, value objects and domain-specific concepts.

Important domain concepts may include:

* Product.
* Store.
* Product store association.
* Price record.
* Target price.
* Current lowest price.
* Historical lowest price.

### `repository`

Defines and implements data-access operations.

Repositories must not contain HTTP-related logic.

Prefer explicit queries when they improve readability or performance.

### `mapper`

Converts between:

* DTOs.
* Domain objects.
* Persistence entities.
* API response models.

Avoid placing business rules inside mappers.

### `infrastructure`

Contains technical implementations such as:

* Database configuration.
* External integrations.
* Persistence implementations.
* Clock or date providers.
* Future notification providers.

External integrations must remain isolated from business rules.

### `config`

Contains framework and application configuration.

### `exception`

Defines application-specific exceptions and centralized HTTP error handling.

Do not create new architectural layers unless they solve a real problem in the current scope.

Follow the existing dependency direction.

Business rules should not depend directly on:

* HTTP.
* Controllers.
* Database implementations.
* Spring-specific classes when this dependency can reasonably be avoided.

## Main domain rules

Implement business rules according to the current issue and documented project requirements.

Unless an issue explicitly changes these definitions, consider the following concepts:

### Product

A product represents an item whose prices will be tracked.

A product may have information such as:

* Name.
* Description.
* Brand.
* Category.
* Target price.
* Active status.

Do not add attributes without a current requirement.

### Store

A store represents a trusted seller where product prices may be found.

A store may have information such as:

* Name.
* Website URL.
* Active status.

The existence of a store in PriceWatch does not imply that prices are collected automatically.

### Product-store association

A product must be associated with a store before a price can be recorded for that product in that store.

The same product should not have duplicate active associations with the same store.

### Price record

A price record represents the price found for a product in a specific store at a specific time.

A price record should contain, when required:

* Product-store association.
* Price value.
* Date and time when the price was found.
* Date and time when the record was created.

Price history must be preserved.

Do not update an old price record to represent a newly observed price.

Create a new price record instead.

### Current price

Unless otherwise defined by an issue, the current price of a product in a store is the most recently recorded price for that product-store association.

### Current lowest price

The current lowest price is the smallest value among the most recent price records from each active store associated with the product.

Do not compare all historical records when calculating the current lowest price.

### Historical lowest price

The historical lowest price is the smallest valid price ever recorded for the product.

The response should identify:

* Price value.
* Store where it was found.
* Date when it was found.

### Target price

A target price represents the value the user would like the product to reach.

When a target price exists, the application may indicate whether:

* The current lowest price is below the target.
* The current lowest price is equal to the target.
* The current lowest price is above the target.

Do not implement automatic alerts unless explicitly requested.

### Monetary values

* Use `BigDecimal` for monetary values.
* Never use `float` or `double` for prices.
* Define monetary scale and rounding behavior explicitly when calculations require rounding.
* Price values must be greater than zero unless an issue explicitly defines another rule.
* Do not format monetary values inside domain calculations.

## API conventions

* Follow REST conventions already established in the repository.
* Use plural nouns for collection resources.
* Use appropriate HTTP methods and status codes.
* Validate all external input.
* Keep request and response contracts explicit.
* Do not expose persistence entities directly through the API.
* Return consistent error responses.
* Maintain compatibility with existing API contracts.
* Update OpenAPI or Swagger documentation when an endpoint contract changes.
* Use pagination for endpoints that may return growing collections.
* Avoid verbs in endpoint paths when the operation can be represented as a resource.

Possible resource structures include:

```text
/api/v1/products
/api/v1/stores
/api/v1/products/{productId}/stores
/api/v1/products/{productId}/prices
/api/v1/products/{productId}/price-summary
```

These paths are references only.

Follow the contracts defined by the current issue and existing application conventions.

### Expected HTTP behavior

Use status codes consistently:

* `200 OK` for successful queries and updates.
* `201 Created` for successful resource creation.
* `204 No Content` for successful operations without a response body.
* `400 Bad Request` for malformed or invalid requests.
* `404 Not Found` when a requested resource does not exist.
* `409 Conflict` for duplicate resources or business conflicts.
* `422 Unprocessable Entity` only when this convention is already adopted by the project.
* `500 Internal Server Error` for unexpected failures without exposing internal details.

## Database and persistence

* Use the repository's existing persistence conventions.
* Create database migrations for every schema change.
* Never modify an existing migration that may already have been executed.
* Add constraints when they represent real business invariants.
* Consider indexes for fields frequently used in searches, joins or ordering.
* Avoid N+1 queries.
* Avoid loading unnecessary data.
* Keep transaction boundaries explicit and as small as reasonably possible.
* Do not place external API calls inside database transactions.
* Never delete or alter production data through development scripts without explicit authorization.
* Preserve price history.
* Prefer database constraints in addition to application validation for critical invariants.

Possible invariants include:

* Product names must not be blank.
* Store names must not be blank.
* Price values must be positive.
* Product and store references must exist.
* A product-store association must be unique.
* Required relationships must not be null.

### Queries involving prices

Queries that calculate current or historical prices must be reviewed for correctness and performance.

Consider:

* Sorting by the date when the price was found.
* Deterministic ordering when two records have the same date.
* Indexes involving product, store and price date.
* Avoiding the loading of the entire price history when only one result is required.
* Performing calculations in the database when this produces a clearer and more efficient implementation.
* Keeping business semantics explicit even when using optimized queries.

## Date and time

* Use Java Time API types.
* Prefer `Instant`, `OffsetDateTime` or another project-standard type for persisted timestamps.
* Do not use legacy `java.util.Date`.
* Store timestamps consistently.
* Avoid using the server's implicit local timezone in business logic.
* Inject or abstract `Clock` when time-dependent behavior requires deterministic tests.
* Distinguish the date when a price was found from the date when the record was created.

## Security

The MVP may not initially include authentication, but secure development practices still apply.

* Never commit credentials, tokens, certificates, private keys or secrets.
* Use environment variables or the project's secret-management mechanism.
* Do not rely only on frontend validation.
* Do not log passwords, tokens, personal data or sensitive payloads.
* Use parameterized queries or the ORM safely.
* Follow the principle of least privilege.
* Do not expose internal database identifiers unnecessarily when a public identifier convention exists.
* Validate and normalize external URLs when store URLs are accepted.
* Do not implement authentication or authorization unless explicitly requested.

## External integrations

External store integrations and automatic price collection are outside the initial MVP scope.

When an integration is introduced by a future issue:

* Keep it isolated from business rules.
* Configure URLs, credentials and timeouts externally.
* Define explicit network timeouts.
* Handle expected integration failures.
* Use retries only for transient and idempotent operations.
* Apply exponential backoff when appropriate.
* Do not retry validation errors or permanent failures.
* Consider idempotency for operations that may be processed more than once.
* Do not silently ignore external integration errors.
* Do not couple the product or price domain directly to a specific store provider.

## Observability

* Use structured and meaningful logs.
* Include relevant identifiers such as product ID, store ID or price record ID.
* Do not log sensitive information.
* Log failures with enough context to support investigation.
* Avoid excessive logs in normal execution paths.
* Do not log every successful database operation.
* Preserve request or correlation identifiers when available.
* Update health checks when adding critical infrastructure dependencies.
* Expose only safe operational details through health endpoints.

Examples of relevant log events:

* Product registration failure.
* Store registration failure.
* Invalid product-store association.
* Price registration failure.
* Failure while calculating a price summary.
* Database or infrastructure errors.

## Error handling

* Use domain-specific or application-specific exceptions when appropriate.
* Do not expose internal stack traces to API consumers.
* Map errors to consistent HTTP responses.
* Distinguish validation, not-found, conflict, business and infrastructure failures.
* Do not use generic exception handling to hide programming errors.
* Preserve the original cause when wrapping exceptions.
* Return error responses containing useful and stable fields.

A consistent error response may contain:

```json
{
  "timestamp": "2026-07-16T15:30:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product not found",
  "path": "/api/v1/products/123"
}
```

Follow the error contract established by the repository.

## Testing

Every behavior change must be covered by an appropriate test.

Use the smallest effective test level:

* Unit tests for isolated business rules.
* Repository tests for database queries and constraints.
* Integration tests for Spring, database and transaction behavior.
* API tests for endpoint contracts and request flows.
* End-to-end tests only when the behavior cannot be validated effectively at lower levels.

### Important unit test scenarios

Depending on the implemented scope, consider testing:

* Product creation.
* Store creation.
* Duplicate product-store associations.
* Price value validation.
* Price registration for a nonexistent product.
* Price registration for a nonexistent store.
* Price registration without an existing product-store association.
* Current price selection.
* Current lowest price selection.
* Historical lowest price selection.
* Store and date returned with the historical lowest price.
* Comparison between the current lowest price and the target price.
* Products without price records.
* Products with only one store.
* Products with multiple stores.
* Multiple price records for the same store.
* Equal prices recorded by different stores.

### Test quality

Tests must:

* Be deterministic.
* Be independent from execution order.
* Use clear arrange, act and assert phases.
* Cover the success scenario.
* Cover relevant validation and failure scenarios.
* Avoid unnecessary mocks.
* Test observable behavior instead of private implementation details.
* Avoid depending on the machine's current time.
* Avoid depending on external services.
* Use realistic monetary values with `BigDecimal`.

Do not remove or weaken tests only to make the test suite pass.

## Local validation

Before considering a task complete, run the relevant project checks.

For a Maven project:

```bash
./mvnw test
./mvnw verify
```

When Docker Compose is available:

```bash
docker compose config
docker compose up -d
docker compose ps
```

When database migrations are involved, validate that:

* The application starts with an empty database.
* All migrations execute successfully.
* The resulting schema matches the expected model.
* Existing migrations were not modified.

Use only commands supported by the repository.

If a command cannot be executed, clearly report:

* Which command was not executed.
* Why it could not be executed.
* What remains unverified.

## Configuration and environments

* Keep configuration externalized.
* Update `.env.example` when adding new environment variables.
* Never add real secrets to example files.
* Use safe defaults for local development when possible.
* Document required configuration changes.
* Preserve compatibility between local, test and production environments.
* Do not hardcode environment-specific URLs or credentials.
* Use Spring profiles only when they provide a concrete benefit.
* Keep test configuration isolated from development configuration.

Expected configuration may include:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
SERVER_PORT
SPRING_PROFILES_ACTIVE
```

Follow the names already established by the repository.

## Docker

Docker should provide a reproducible local environment.

When Docker is part of the issue scope:

* Keep images minimal and reproducible.
* Pin relevant image versions.
* Add health checks when useful.
* Do not store secrets in Dockerfiles or Compose files.
* Use environment variables for configuration.
* Ensure PostgreSQL data can be persisted locally when required.
* Avoid adding infrastructure not currently used by the project.
* Document how to start and stop the environment.
* Ensure another developer can start the project by following the README.

The initial environment should remain simple.

Do not add Redis, Kafka, RabbitMQ or other infrastructure without an explicit requirement.

## Documentation

Update documentation when the implementation changes:

* Setup and execution steps.
* Environment variables.
* API contracts.
* Architecture decisions.
* Database migrations.
* Domain rules.
* Local Docker usage.
* Validation commands.
* Known limitations.

Documentation must reflect the actual behavior of the repository.

Do not document features that have not been implemented.

## Git and change management

* Keep changes small and focused.
* Do not mix unrelated changes in the same implementation.
* Do not rewrite repository history.
* Do not force-push unless explicitly requested.
* Do not commit generated files unless the repository already tracks them.
* Do not modify lock files unless dependencies actually changed.
* Follow the branch and commit conventions documented by the repository.
* Reference the related issue when applicable.

Recommended commit format:

```text
<type>(<scope>): <short description>
```

Common types:

* `feat`
* `fix`
* `refactor`
* `test`
* `docs`
* `chore`
* `build`
* `ci`

Possible scopes for PriceWatch:

* `product`
* `store`
* `price`
* `database`
* `api`
* `docker`
* `docs`
* `ci`

Examples:

```text
feat(product): add product registration endpoint
feat(price): add price history persistence
fix(price): calculate current lowest price per store
test(store): cover duplicate product store association
docs(api): document price summary endpoint
chore(docker): add PostgreSQL service
```

## Pull request expectations

When preparing a pull request, include:

* A clear summary of what changed.
* The reason for the change.
* The related GitHub issue.
* The main technical decisions.
* How the change was validated.
* Tests that were added or updated.
* Database migration details.
* Configuration or environment changes.
* Known limitations or follow-up work.

The pull request must not include changes outside the issue scope.

## Definition of done

A task is complete only when:

* The requested behavior has been implemented.
* The implementation remains within the agreed scope.
* Relevant tests have been added or updated.
* Existing tests continue to pass.
* The project builds successfully.
* Required database migrations are included.
* Configuration examples are updated.
* Relevant documentation has been updated.
* OpenAPI documentation reflects contract changes.
* No credentials or sensitive data were introduced.
* No unrelated changes were included.
* Remaining risks or unverified points are clearly reported.

## Final response format

At the end of each implementation task, provide:

1. **Summary**

   * Briefly explain what was implemented.

2. **Main files changed**

   * List the relevant files and their purpose.

3. **Tests and validation**

   * List the commands executed.
   * Report whether they passed or failed.

4. **Technical decisions**

   * Explain important decisions and trade-offs.

5. **Database and configuration**

   * Describe migrations, environment variables or setup changes.

6. **Risks or unverified items**

   * Clearly state anything that could not be validated.

7. **Suggestions not implemented**

   * Present optional improvements separately.
   * Do not imply that they were part of the completed issue.
