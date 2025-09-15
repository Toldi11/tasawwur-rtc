package com.tasawwur.rtc.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * API Key entity for managing user authentication tokens.
 * 
 * Provides secure API key management with expiration, scoping,
 * and usage tracking for the REST API endpoints.
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key_hash", columnList = "keyHash", unique = true),
    @Index(name = "idx_api_key_user", columnList = "user_id"),
    @Index(name = "idx_api_key_expires_at", columnList = "expiresAt")
})
@EntityListeners(AuditingEntityListener.class)
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "API key name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private ApiKeyScope scope = ApiKeyScope.READ_WRITE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Enums
    public enum ApiKeyScope {
        READ_ONLY,
        READ_WRITE,
        ADMIN
    }

    // Constructors
    public ApiKey() {}

    public ApiKey(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
        this.keyHash = generateKeyHash();
    }

    public ApiKey(String name, String description, User user, ApiKeyScope scope, Instant expiresAt) {
        this(name, description, user);
        this.scope = scope;
        this.expiresAt = expiresAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public ApiKeyScope getScope() {
        return scope;
    }

    public void setScope(ApiKeyScope scope) {
        this.scope = scope;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return enabled && !isExpired();
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
        this.usageCount += 1;
    }

    public void regenerateKey() {
        this.keyHash = generateKeyHash();
    }

    public long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return Long.MAX_VALUE; // Never expires
        }
        return java.time.Duration.between(Instant.now(), expiresAt).toDays();
    }

    // Private helper methods
    private String generateKeyHash() {
        // Generate a secure API key hash (48 characters)
        return "trk_" + UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return Objects.equals(id, apiKey.id) && Objects.equals(keyHash, apiKey.keyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keyHash);
    }

    @Override
    public String toString() {
        return "ApiKey{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", enabled=" + enabled +
               ", scope=" + scope +
               ", expiresAt=" + expiresAt +
               ", usageCount=" + usageCount +
               ", createdAt=" + createdAt +
               '}';
    }
}

