package io.github.xandewe.pricewatch.store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	boolean existsByNormalizedName(String normalizedName);

	boolean existsByNormalizedNameAndIdNot(String normalizedName, UUID id);

	@Query("""
			SELECT store
			FROM Store store
			WHERE (:active IS NULL OR store.active = :active)
				AND (:search IS NULL OR store.normalizedName LIKE CONCAT('%', :search, '%'))
			""")
	Page<Store> search(
			@Param("search") String search,
			@Param("active") Boolean active,
			Pageable pageable);
}
