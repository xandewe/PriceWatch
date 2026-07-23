package io.github.xandewe.pricewatch.store.application;

import io.github.xandewe.pricewatch.store.DuplicateStoreException;
import io.github.xandewe.pricewatch.store.Store;
import io.github.xandewe.pricewatch.store.StoreNameNormalizer;
import io.github.xandewe.pricewatch.store.StoreRepository;
import io.github.xandewe.pricewatch.store.dto.StoreRequest;
import io.github.xandewe.pricewatch.store.dto.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

	private static final Instant NOW = Instant.parse("2026-07-23T12:00:00Z");

	@Mock
	private StoreRepository repository;

	private StoreService service;

	@BeforeEach
	void setUp() {
		service = new StoreService(
				repository,
				new StoreNameNormalizer(),
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void createsAnActiveStoreWithACleanAndNormalizedName() {
		when(repository.existsByNormalizedName("trusted store")).thenReturn(false);
		when(repository.saveAndFlush(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.create(new StoreRequest(
				"  Trusted   Store ",
				" https://store.example "));

		assertThat(response.name()).isEqualTo("Trusted Store");
		assertThat(response.websiteUrl()).isEqualTo("https://store.example");
		assertThat(response.active()).isTrue();
	}

	@Test
	void rejectsADuplicateNormalizedName() {
		when(repository.existsByNormalizedName("trusted store")).thenReturn(true);

		assertThatThrownBy(() -> service.create(new StoreRequest(" TRUSTED   Store ", null)))
				.isInstanceOf(DuplicateStoreException.class)
				.hasMessage("A store with this name already exists");

		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void rejectsAConflictingUpdate() {
		var storeId = UUID.randomUUID();
		var store = new Store("First Store", "first store", null);
		when(repository.findById(storeId)).thenReturn(Optional.of(store));
		when(repository.existsByNormalizedNameAndIdNot("second store", storeId)).thenReturn(true);

		assertThatThrownBy(() -> service.update(
				storeId,
				new StoreRequest("Second Store", null)))
				.isInstanceOf(DuplicateStoreException.class);
	}

	@Test
	void deactivatesAndRestoresWithoutDeletingTheStore() {
		var storeId = UUID.randomUUID();
		var store = new Store("Trusted Store", "trusted store", null);
		when(repository.findById(storeId)).thenReturn(Optional.of(store));

		service.deactivate(storeId);

		assertThat(store.isActive()).isFalse();
		assertThat(store.getDeletedAt()).isEqualTo(NOW);
		verify(repository, never()).delete(any());

		service.restore(storeId);

		assertThat(store.isActive()).isTrue();
		assertThat(store.getDeletedAt()).isNull();
	}

	@Test
	void listsActiveStoresByDefaultWithNormalizedPartialSearchAndPagination() {
		var store = new Store("Trusted Store", "trusted store", null);
		when(repository.search(eq("trusted store"), eq(true), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(store)));

		var response = service.list(
				0,
				20,
				"name",
				"asc",
				"  TRUSTED   Store ",
				StoreStatus.ACTIVE);

		assertThat(response.content())
				.extracting(item -> item.name())
				.containsExactly("Trusted Store");
		assertThat(response.page()).isZero();
		assertThat(response.size()).isEqualTo(1);

		var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(repository).search(eq("trusted store"), eq(true), pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("name").isAscending()).isTrue();
	}
}
