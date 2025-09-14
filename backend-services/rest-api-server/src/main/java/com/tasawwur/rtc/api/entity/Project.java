package com.tasawwur.rtc.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Project entity representing an RTC application created by a developer.
 * 
 * Each project has a unique App ID and App Secret for authentication,
 * and tracks usage statistics for billing and analytics.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_app_id", columnList = "appId", unique = true),
    @Index(name = "idx_project_owner", columnList = "owner_id"),
    @Index(name = "idx_project_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "app_id", nullable = false, unique = true, updatable = false)
    private String appId;

    @Column(name = "app_secret", nullable = false)
    private String appSecret;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "total_minutes_used", nullable = false)
    private Long totalMinutesUsed = 0L;

    @Column(name = "peak_concurrent_users", nullable = false)
    private Integer peakConcurrentUsers = 0;

    @Column(name = "total_sessions", nullable = false)
    private Long totalSessions = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UsageRecord> usageRecords = new HashSet<>();

    // Constructors
    public Project() {
        this.appId = generateAppId();
        this.appSecret = generateAppSecret();
    }

    public Project(String name, String description, User owner) {
        this();
        this.name = name;
        this.description = description;
        this.owner = owner;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getTotalMinutesUsed() {
        return totalMinutesUsed;
    }

    public void setTotalMinutesUsed(Long totalMinutesUsed) {
        this.totalMinutesUsed = totalMinutesUsed;
    }

    public Integer getPeakConcurrentUsers() {
        return peakConcurrentUsers;
    }

    public void setPeakConcurrentUsers(Integer peakConcurrentUsers) {
        this.peakConcurrentUsers = peakConcurrentUsers;
    }

    public Long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Long totalSessions) {
        this.totalSessions = totalSessions;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<UsageRecord> getUsageRecords() {
        return usageRecords;
    }

    public void setUsageRecords(Set<UsageRecord> usageRecords) {
        this.usageRecords = usageRecords;
    }

    // Utility methods
    public void regenerateAppSecret() {
        this.appSecret = generateAppSecret();
    }

    public void addUsageRecord(UsageRecord usageRecord) {
        usageRecords.add(usageRecord);
        usageRecord.setProject(this);
    }

    public void removeUsageRecord(UsageRecord usageRecord) {
        usageRecords.remove(usageRecord);
        usageRecord.setProject(null);
    }

    public void updateUsageStats(long minutesUsed, int concurrentUsers) {
        this.totalMinutesUsed += minutesUsed;
        this.totalSessions += 1;
        if (concurrentUsers > this.peakConcurrentUsers) {
            this.peakConcurrentUsers = concurrentUsers;
        }
    }

    // Private helper methods
    private String generateAppId() {
        // Generate a unique app ID in the format: app_xxxxxxxxxxxxxxxx
        return "app_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateAppSecret() {
        // Generate a secure app secret (64 characters)
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "");
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) && Objects.equals(appId, project.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId);
    }

    @Override
    public String toString() {
        return "Project{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", appId='" + appId + '\'' +
               ", enabled=" + enabled +
               ", totalMinutesUsed=" + totalMinutesUsed +
               ", peakConcurrentUsers=" + peakConcurrentUsers +
               ", totalSessions=" + totalSessions +
               ", createdAt=" + createdAt +
               '}';
    }
}
