package io.github.xandewe.pricewatch.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreRequest(
		@NotBlank
		@Size(max = 255)
		String name,

		@Size(max = 2048)
		String websiteUrl) {
}
