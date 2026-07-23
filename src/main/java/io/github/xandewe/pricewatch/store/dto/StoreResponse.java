package io.github.xandewe.pricewatch.store.dto;

import io.github.xandewe.pricewatch.store.Store;

import java.time.Instant;
import java.util.UUID;

public record StoreResponse(
		UUID id,
		String name,
		String websiteUrl,
		boolean active,
		Instant createdAt,
		Instant updatedAt,
		Instant deletedAt) {

	public static StoreResponse from(Store store) {
		return new StoreResponse(
				store.getId(),
				store.getName(),
				store.getWebsiteUrl(),
				store.isActive(),
				store.getCreatedAt(),
				store.getUpdatedAt(),
				store.getDeletedAt());
	}
}
