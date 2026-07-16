package io.github.xandewe.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DatabaseConfigurationIntegrationTests.PostgreSQLTestConfiguration.class)
class ActuatorEndpointIntegrationTests {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@LocalServerPort
	private int port;

	@Test
	void reportsApplicationAndDatabaseHealthAsUp() throws Exception {
		HttpResponse<String> response = get("/actuator/health");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	void exposesInfoEndpoint() throws Exception {
		HttpResponse<String> response = get("/actuator/info");

		assertThat(response.statusCode()).isEqualTo(200);
	}

	@Test
	void doesNotExposeUnconfiguredActuatorEndpoints() throws Exception {
		HttpResponse<String> response = get("/actuator/beans");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private HttpResponse<String> get(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.GET()
				.build();

		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}
}
