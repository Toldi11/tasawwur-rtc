package com.tasawwur.rtc.api.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * Usage record entity for tracking RTC usage statistics.
 * 
 * Records detailed usage metrics for billing and analytics purposes,
 * including session duration, participant counts, and data transfer.
 */
@Entity
@Table(name = "usage_records", indexes = {
    @Index(name = "idx_usage_project_date", columnList = "project_id, recordDate"),
    @Index(name = "idx_usage_record_date", columnList = "recordDate"),
    @Index(name = "idx_usage_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "duration_minutes", nullable = false)
    private Long durationMinutes;

    @Column(name = "peak_participants", nullable = false)
    private Integer peakParticipants;

    @Column(name = "total_participants", nullable = false)
    private Integer totalParticipants;

    @Column(name = "bytes_sent", nullable = false)
    private Long bytesSent = 0L;

    @Column(name = "bytes_received", nullable = false)
    private Long bytesReceived = 0L;

    @Column(name = "audio_minutes", nullable = false)
    private Long audioMinutes = 0L;

    @Column(name = "video_minutes", nullable = false)
    private Long videoMinutes = 0L;

    @Column(name = "record_date", nullable = false)
    private Instant recordDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Constructors
    public UsageRecord() {
        this.recordDate = Instant.now();
    }

    public UsageRecord(String sessionId, String channelName, String userId, 
                      Long durationMinutes, Integer peakParticipants, 
                      Integer totalParticipants, Project project) {
        this();
        this.sessionId = sessionId;
        this.channelName = channelName;
        this.userId = userId;
        this.durationMinutes = durationMinutes;
        this.peakParticipants = peakParticipants;
        this.totalParticipants = totalParticipants;
        this.project = project;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPeakParticipants() {
        return peakParticipants;
    }

    public void setPeakParticipants(Integer peakParticipants) {
        this.peakParticipants = peakParticipants;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public Long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public Long getAudioMinutes() {
        return audioMinutes;
    }

    public void setAudioMinutes(Long audioMinutes) {
        this.audioMinutes = audioMinutes;
    }

    public Long getVideoMinutes() {
        return videoMinutes;
    }

    public void setVideoMinutes(Long videoMinutes) {
        this.videoMinutes = videoMinutes;
    }

    public Instant getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Instant recordDate) {
        this.recordDate = recordDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // Utility methods
    public Long getTotalBytes() {
        return bytesSent + bytesReceived;
    }

    public Long getTotalMediaMinutes() {
        return audioMinutes + videoMinutes;
    }

    public Double getAverageParticipants() {
        return totalParticipants > 0 ? (double) totalParticipants / durationMinutes : 0.0;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsageRecord that = (UsageRecord) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(recordDate, that.recordDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sessionId, recordDate);
    }

    @Override
    public String toString() {
        return "UsageRecord{" +
               "id=" + id +
               ", sessionId='" + sessionId + '\'' +
               ", channelName='" + channelName + '\'' +
               ", userId='" + userId + '\'' +
               ", durationMinutes=" + durationMinutes +
               ", peakParticipants=" + peakParticipants +
               ", totalParticipants=" + totalParticipants +
               ", recordDate=" + recordDate +
               '}';
    }
}
