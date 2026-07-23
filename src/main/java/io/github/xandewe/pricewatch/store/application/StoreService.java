package io.github.xandewe.pricewatch.store.application;

import io.github.xandewe.pricewatch.store.DuplicateStoreException;
import io.github.xandewe.pricewatch.store.InvalidStoreQueryException;
import io.github.xandewe.pricewatch.store.InvalidStoreWebsiteUrlException;
import io.github.xandewe.pricewatch.store.Store;
import io.github.xandewe.pricewatch.store.StoreNameNormalizer;
import io.github.xandewe.pricewatch.store.StoreNotFoundException;
import io.github.xandewe.pricewatch.store.StoreRepository;
import io.github.xandewe.pricewatch.store.dto.StorePageResponse;
import io.github.xandewe.pricewatch.store.dto.StoreRequest;
import io.github.xandewe.pricewatch.store.dto.StoreResponse;
import io.github.xandewe.pricewatch.store.dto.StoreStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class StoreService {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"name", "name",
			"websiteUrl", "websiteUrl",
			"active", "active",
			"createdAt", "createdAt",
			"updatedAt", "updatedAt");

	private final StoreRepository repository;
	private final StoreNameNormalizer nameNormalizer;
	private final Clock clock;

	public StoreService(StoreRepository repository, StoreNameNormalizer nameNormalizer, Clock clock) {
		this.repository = repository;
		this.nameNormalizer = nameNormalizer;
		this.clock = clock;
	}

	@Transactional
	public StoreResponse create(StoreRequest request) {
		String normalizedName = nameNormalizer.normalize(request.name());
		if (repository.existsByNormalizedName(normalizedName)) {
			throw new DuplicateStoreException();
		}

		var store = new Store(
				nameNormalizer.clean(request.name()),
				normalizedName,
				normalizeWebsiteUrl(request.websiteUrl()));

		return StoreResponse.from(save(store));
	}

	@Transactional(readOnly = true)
	public StoreResponse get(UUID storeId) {
		return StoreResponse.from(find(storeId));
	}

	@Transactional
	public StoreResponse update(UUID storeId, StoreRequest request) {
		var store = find(storeId);
		String normalizedName = nameNormalizer.normalize(request.name());
		if (repository.existsByNormalizedNameAndIdNot(normalizedName, storeId)) {
			throw new DuplicateStoreException();
		}

		store.update(
				nameNormalizer.clean(request.name()),
				normalizedName,
				normalizeWebsiteUrl(request.websiteUrl()));

		return StoreResponse.from(save(store));
	}

	@Transactional
	public void deactivate(UUID storeId) {
		find(storeId).deactivate(Instant.now(clock));
	}

	@Transactional
	public void restore(UUID storeId) {
		find(storeId).restore();
	}

	@Transactional(readOnly = true)
	public StorePageResponse list(
			int page,
			int size,
			String sort,
			String direction,
			String search,
			StoreStatus status) {
		validatePagination(page, size);
		String property = SORT_FIELDS.get(sort);
		if (property == null) {
			throw new InvalidStoreQueryException("Unsupported sort field: " + sort);
		}

		Sort.Direction sortDirection;
		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException exception) {
			throw new InvalidStoreQueryException("direction must be asc or desc");
		}

		String normalizedSearch = search == null || search.isBlank()
				? null
				: nameNormalizer.normalize(search);
		Boolean active = switch (status) {
			case ACTIVE -> true;
			case INACTIVE -> false;
			case ALL -> null;
		};
		var pageable = PageRequest.of(page, size, Sort.by(sortDirection, property));

		return StorePageResponse.from(repository.search(normalizedSearch, active, pageable)
				.map(StoreResponse::from));
	}

	private Store find(UUID storeId) {
		return repository.findById(storeId)
				.orElseThrow(() -> new StoreNotFoundException(storeId));
	}

	private Store save(Store store) {
		try {
			return repository.saveAndFlush(store);
		} catch (DataIntegrityViolationException exception) {
			throw new DuplicateStoreException();
		}
	}

	private static String normalizeWebsiteUrl(String websiteUrl) {
		if (websiteUrl == null || websiteUrl.isBlank()) {
			return null;
		}

		String cleanedUrl = websiteUrl.trim();
		try {
			URI uri = URI.create(cleanedUrl);
			if (uri.getHost() == null
					|| (!"http".equalsIgnoreCase(uri.getScheme())
					&& !"https".equalsIgnoreCase(uri.getScheme()))) {
				throw new InvalidStoreWebsiteUrlException();
			}
			return uri.toString();
		} catch (IllegalArgumentException exception) {
			throw new InvalidStoreWebsiteUrlException();
		}
	}

	private static void validatePagination(int page, int size) {
		if (page < 0) {
			throw new InvalidStoreQueryException("page must be greater than or equal to 0");
		}
		if (size < 1 || size > 100) {
			throw new InvalidStoreQueryException("size must be between 1 and 100");
		}
	}
}
