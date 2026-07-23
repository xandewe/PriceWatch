package io.github.xandewe.pricewatch.store.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record StorePageResponse(
		List<StoreResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last) {

	public static StorePageResponse from(Page<StoreResponse> page) {
		return new StorePageResponse(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}
}
