package io.github.xandewe.pricewatch.store.web;

import io.github.xandewe.pricewatch.store.application.StoreService;
import io.github.xandewe.pricewatch.store.dto.StorePageResponse;
import io.github.xandewe.pricewatch.store.dto.StoreRequest;
import io.github.xandewe.pricewatch.store.dto.StoreResponse;
import io.github.xandewe.pricewatch.store.dto.StoreStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

	private final StoreService service;

	public StoreController(StoreService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request) {
		var response = service.create(request);
		return ResponseEntity
				.created(URI.create("/api/v1/stores/" + response.id()))
				.body(response);
	}

	@GetMapping
	public StorePageResponse list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "name") String sort,
			@RequestParam(defaultValue = "asc") String direction,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "ACTIVE") StoreStatus status) {
		return service.list(page, size, sort, direction, search, status);
	}

	@GetMapping("/{storeId}")
	public StoreResponse get(@PathVariable UUID storeId) {
		return service.get(storeId);
	}

	@PutMapping("/{storeId}")
	public StoreResponse update(
			@PathVariable UUID storeId,
			@Valid @RequestBody StoreRequest request) {
		return service.update(storeId, request);
	}

	@DeleteMapping("/{storeId}")
	public ResponseEntity<Void> deactivate(@PathVariable UUID storeId) {
		service.deactivate(storeId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{storeId}/restore")
	public ResponseEntity<Void> restore(@PathVariable UUID storeId) {
		service.restore(storeId);
		return ResponseEntity.noContent().build();
	}
}
