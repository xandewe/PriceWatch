package io.github.xandewe.pricewatch.pricing;

import io.github.xandewe.pricewatch.store.ProductStore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prices")
public class Price {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_store_id", nullable = false)
	private ProductStore productStore;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Availability availability;

	@Column(columnDefinition = "text")
	private String note;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Price() {
	}

	public Price(
			ProductStore productStore,
			BigDecimal amount,
			Availability availability,
			String note,
			Instant recordedAt) {
		productStore.ensureStoreActive();
		this.productStore = productStore;
		this.amount = amount;
		this.availability = availability;
		this.note = note;
		this.recordedAt = recordedAt;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getRecordedAt() {
		return recordedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
