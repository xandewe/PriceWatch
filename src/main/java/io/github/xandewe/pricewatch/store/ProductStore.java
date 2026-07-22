package io.github.xandewe.pricewatch.store;

import io.github.xandewe.pricewatch.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_stores")
public class ProductStore {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false, length = 2048)
	private String url;

	@Column(name = "normalized_url", nullable = false, length = 2048)
	private String normalizedUrl;

	@Column(name = "external_code")
	private String externalCode;

	@Column(nullable = false)
	private boolean active = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected ProductStore() {
	}

	public ProductStore(Product product, Store store, String url, String normalizedUrl, String externalCode) {
		this.product = product;
		this.store = store;
		this.url = url;
		this.normalizedUrl = normalizedUrl;
		this.externalCode = externalCode;
	}

	public UUID getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public Store getStore() {
		return store;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
