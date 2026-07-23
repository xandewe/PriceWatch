package io.github.xandewe.pricewatch.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class Store {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(name = "normalized_name", nullable = false, unique = true)
	private String normalizedName;

	@Column(name = "website_url", length = 2048)
	private String websiteUrl;

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

	protected Store() {
	}

	public Store(String name, String normalizedName, String websiteUrl) {
		this.name = name;
		this.normalizedName = normalizedName;
		this.websiteUrl = websiteUrl;
	}

	public void update(String name, String normalizedName, String websiteUrl) {
		this.name = name;
		this.normalizedName = normalizedName;
		this.websiteUrl = websiteUrl;
	}

	public void deactivate(Instant deletionTime) {
		this.active = false;
		this.deletedAt = deletionTime;
	}

	public void restore() {
		this.active = true;
		this.deletedAt = null;
	}

	public void ensureActive() {
		if (!active) {
			throw new InactiveStoreException();
		}
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
}
