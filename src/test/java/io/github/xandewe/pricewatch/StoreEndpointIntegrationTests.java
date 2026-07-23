package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)
class StoreEndpointIntegrationTests {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@LocalServerPort
	private int port;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.update("DELETE FROM prices");
		jdbcTemplate.update("DELETE FROM product_stores");
		jdbcTemplate.update("DELETE FROM products");
		jdbcTemplate.update("DELETE FROM stores");
	}

	@Test
	void createsGetsAndUpdatesAStore() throws Exception {
		HttpResponse<String> creation = send(
				"POST",
				"/api/v1/stores",
				"""
						{
						  "name": "  Trusted   Store ",
						  "websiteUrl": "https://store.example"
						}
						""");

		assertThat(creation.statusCode()).isEqualTo(201);
		JsonNode createdStore = json(creation);
		String storeId = createdStore.get("id").stringValue();
		assertThat(createdStore.get("name").stringValue()).isEqualTo("Trusted Store");
		assertThat(createdStore.get("active").asBoolean()).isTrue();
		assertThat(creation.headers().firstValue("Location"))
				.hasValue("/api/v1/stores/" + storeId);

		HttpResponse<String> update = send(
				"PUT",
				"/api/v1/stores/" + storeId,
				"""
						{
						  "name": "Updated Store",
						  "websiteUrl": null
						}
						""");

		assertThat(update.statusCode()).isEqualTo(200);
		assertThat(json(update).get("name").stringValue()).isEqualTo("Updated Store");
		assertThat(json(update).get("websiteUrl").isNull()).isTrue();

		HttpResponse<String> query = send("GET", "/api/v1/stores/" + storeId, null);

		assertThat(query.statusCode()).isEqualTo(200);
		assertThat(json(query).get("name").stringValue()).isEqualTo("Updated Store");
	}

	@Test
	void rejectsDuplicateAndConflictingNormalizedNames() throws Exception {
		String firstStoreId = createStore("Trusted Store");

		HttpResponse<String> duplicate = send(
				"POST",
				"/api/v1/stores",
				"{\"name\":\"  TRUSTED   Store \"}");

		assertThat(duplicate.statusCode()).isEqualTo(409);
		assertThat(json(duplicate).get("code").stringValue()).isEqualTo("STORE_NAME_CONFLICT");

		String secondStoreId = createStore("Another Store");
		HttpResponse<String> conflict = send(
				"PUT",
				"/api/v1/stores/" + secondStoreId,
				"{\"name\":\"trusted store\"}");

		assertThat(conflict.statusCode()).isEqualTo(409);
		assertThat(firstStoreId).isNotEqualTo(secondStoreId);
	}

	@Test
	void listsActiveStoresByDefaultAndSupportsSearchStatusAndPagination() throws Exception {
		createStore("Alpha Market");
		String inactiveStoreId = createStore("Beta Market");
		createStore("Gamma Shop");
		send("DELETE", "/api/v1/stores/" + inactiveStoreId, null);

		HttpResponse<String> activeStores = send(
				"GET",
				"/api/v1/stores?page=0&size=1&sort=name&direction=asc&search=market",
				null);

		assertThat(activeStores.statusCode()).isEqualTo(200);
		JsonNode activePage = json(activeStores);
		assertThat(activePage.get("content")).hasSize(1);
		assertThat(activePage.at("/content/0/name").stringValue()).isEqualTo("Alpha Market");
		assertThat(activePage.get("totalElements").asLong()).isOne();
		assertThat(activePage.get("page").asInt()).isZero();
		assertThat(activePage.get("size").asInt()).isOne();

		HttpResponse<String> inactiveStores = send(
				"GET",
				"/api/v1/stores?status=INACTIVE",
				null);

		assertThat(inactiveStores.statusCode()).isEqualTo(200);
		assertThat(json(inactiveStores).at("/content/0/name").stringValue()).isEqualTo("Beta Market");

		HttpResponse<String> allStores = send(
				"GET",
				"/api/v1/stores?status=ALL",
				null);

		assertThat(allStores.statusCode()).isEqualTo(200);
		assertThat(json(allStores).get("totalElements").longValue()).isEqualTo(3);
	}

	@Test
	void softDeletesAndRestoresAStoreWithoutRemovingItsRecord() throws Exception {
		String storeId = createStore("Trusted Store");

		HttpResponse<String> deletion = send("DELETE", "/api/v1/stores/" + storeId, null);

		assertThat(deletion.statusCode()).isEqualTo(204);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM stores WHERE id = ?",
				Integer.class,
				UUID.fromString(storeId))).isOne();
		assertThat(jdbcTemplate.queryForObject(
				"SELECT active FROM stores WHERE id = ?",
				Boolean.class,
				UUID.fromString(storeId))).isFalse();
		assertThat(jdbcTemplate.queryForObject(
				"SELECT deleted_at IS NOT NULL FROM stores WHERE id = ?",
				Boolean.class,
				UUID.fromString(storeId))).isTrue();

		HttpResponse<String> restore = send(
				"PATCH",
				"/api/v1/stores/" + storeId + "/restore",
				null);

		assertThat(restore.statusCode()).isEqualTo(204);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT active AND deleted_at IS NULL FROM stores WHERE id = ?",
				Boolean.class,
				UUID.fromString(storeId))).isTrue();
	}

	@Test
	void softDeletePreservesListingsAndPriceHistory() throws Exception {
		UUID storeId = UUID.fromString(createStore("Trusted Store"));
		UUID productId = UUID.randomUUID();
		UUID listingId = UUID.randomUUID();
		UUID priceId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO products (id, name, normalized_name, active)
				VALUES (?, 'Notebook', ?, TRUE)
				""", productId, "notebook-" + productId);
		jdbcTemplate.update("""
				INSERT INTO product_stores
				    (id, product_id, store_id, url, normalized_url, active)
				VALUES (?, ?, ?, ?, ?, TRUE)
				""",
				listingId,
				productId,
				storeId,
				"https://store.example/notebook",
				"https://store.example/notebook");
		jdbcTemplate.update("""
				INSERT INTO prices
				    (id, product_store_id, amount, availability, recorded_at)
				VALUES (?, ?, 3999.90, 'AVAILABLE', CURRENT_TIMESTAMP)
				""", priceId, listingId);

		HttpResponse<String> deletion = send("DELETE", "/api/v1/stores/" + storeId, null);

		assertThat(deletion.statusCode()).isEqualTo(204);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM product_stores WHERE id = ?",
				Integer.class,
				listingId)).isOne();
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM prices WHERE id = ?",
				Integer.class,
				priceId)).isOne();
	}

	@Test
	void returnsNotFoundAndValidationErrorsWithAConsistentContract() throws Exception {
		HttpResponse<String> missing = send(
				"GET",
				"/api/v1/stores/" + UUID.randomUUID(),
				null);

		assertThat(missing.statusCode()).isEqualTo(404);
		assertThat(json(missing).get("code").stringValue()).isEqualTo("STORE_NOT_FOUND");

		HttpResponse<String> blankName = send(
				"POST",
				"/api/v1/stores",
				"{\"name\":\"   \"}");

		assertThat(blankName.statusCode()).isEqualTo(400);
		assertThat(json(blankName).get("code").stringValue()).isEqualTo("INVALID_REQUEST");

		HttpResponse<String> invalidUrl = send(
				"POST",
				"/api/v1/stores",
				"{\"name\":\"Trusted Store\",\"websiteUrl\":\"ftp://store.example\"}");

		assertThat(invalidUrl.statusCode()).isEqualTo(400);
		assertThat(json(invalidUrl).get("code").stringValue()).isEqualTo("INVALID_WEBSITE_URL");

		HttpResponse<String> invalidPageSize = send(
				"GET",
				"/api/v1/stores?size=101",
				null);

		assertThat(invalidPageSize.statusCode()).isEqualTo(400);
		assertThat(json(invalidPageSize).get("code").stringValue()).isEqualTo("INVALID_QUERY");
	}

	private String createStore(String name) throws Exception {
		HttpResponse<String> response = send(
				"POST",
				"/api/v1/stores",
				"{\"name\":\"" + name + "\"}");
		assertThat(response.statusCode()).isEqualTo(201);
		return json(response).get("id").stringValue();
	}

	private HttpResponse<String> send(String method, String path, String body) throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(
						method,
						body == null
								? HttpRequest.BodyPublishers.noBody()
								: HttpRequest.BodyPublishers.ofString(body))
				.build();

		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private JsonNode json(HttpResponse<String> response) throws Exception {
		return objectMapper.readTree(response.body());
	}
}
