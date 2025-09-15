package com.tasawwur.rtc.api.repository;

import com.tasawwur.rtc.api.entity.Project;
import com.tasawwur.rtc.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Project entity operations.
 * 
 * Provides data access methods for project management, statistics,
 * and usage tracking.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find a project by its App ID.
     * 
     * @param appId The unique App ID
     * @return Optional containing the project if found
     */
    Optional<Project> findByAppId(String appId);

    /**
     * Find a project by App ID and App Secret (for authentication).
     * 
     * @param appId The App ID
     * @param appSecret The App Secret
     * @return Optional containing the project if found and credentials match
     */
    Optional<Project> findByAppIdAndAppSecret(String appId, String appSecret);

    /**
     * Find all projects owned by a specific user.
     * 
     * @param owner The project owner
     * @param pageable Pagination information
     * @return Page of projects owned by the user
     */
    Page<Project> findByOwner(User owner, Pageable pageable);

    /**
     * Find all projects owned by a specific user ID.
     * 
     * @param ownerId The owner's user ID
     * @param pageable Pagination information
     * @return Page of projects owned by the user
     */
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Find all enabled projects.
     * 
     * @param pageable Pagination information
     * @return Page of enabled projects
     */
    Page<Project> findByEnabledTrue(Pageable pageable);

    /**
     * Find all enabled projects owned by a user.
     * 
     * @param owner The project owner
     * @param pageable Pagination information
     * @return Page of enabled projects owned by the user
     */
    Page<Project> findByOwnerAndEnabledTrue(User owner, Pageable pageable);

    /**
     * Find projects by name containing search term (case insensitive).
     * 
     * @param name The search term
     * @param pageable Pagination information
     * @return Page of matching projects
     */
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find projects created after a specific date.
     * 
     * @param date The date threshold
     * @param pageable Pagination information
     * @return Page of projects created after the date
     */
    Page<Project> findByCreatedAtAfter(Instant date, Pageable pageable);

    /**
     * Search projects by name or description.
     * 
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching projects
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Project> searchProjects(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search projects owned by a specific user.
     * 
     * @param owner The project owner
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching projects owned by the user
     */
    @Query("SELECT p FROM Project p WHERE p.owner = :owner AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Project> searchUserProjects(@Param("owner") User owner, 
                                   @Param("searchTerm") String searchTerm, 
                                   Pageable pageable);

    /**
     * Check if App ID exists.
     * 
     * @param appId The App ID to check
     * @return true if App ID exists, false otherwise
     */
    boolean existsByAppId(String appId);

    /**
     * Count total number of projects.
     * 
     * @return Total project count
     */
    long count();

    /**
     * Count enabled projects.
     * 
     * @return Count of enabled projects
     */
    long countByEnabledTrue();

    /**
     * Count projects owned by a specific user.
     * 
     * @param owner The project owner
     * @return Count of projects owned by the user
     */
    long countByOwner(User owner);

    /**
     * Count enabled projects owned by a specific user.
     * 
     * @param owner The project owner
     * @return Count of enabled projects owned by the user
     */
    long countByOwnerAndEnabledTrue(User owner);

    /**
     * Count projects created after a specific date.
     * 
     * @param date The date threshold
     * @return Count of projects created after the date
     */
    long countByCreatedAtAfter(Instant date);

    /**
     * Find top projects by usage (total minutes).
     * 
     * @param pageable Pagination information
     * @return Page of projects ordered by total minutes used
     */
    Page<Project> findByOrderByTotalMinutesUsedDesc(Pageable pageable);

    /**
     * Find top projects by session count.
     * 
     * @param pageable Pagination information
     * @return Page of projects ordered by total sessions
     */
    Page<Project> findByOrderByTotalSessionsDesc(Pageable pageable);

    /**
     * Find projects with usage above a threshold.
     * 
     * @param minMinutes Minimum minutes threshold
     * @param pageable Pagination information
     * @return Page of projects with high usage
     */
    Page<Project> findByTotalMinutesUsedGreaterThan(Long minMinutes, Pageable pageable);

    /**
     * Find projects with no usage.
     * 
     * @param pageable Pagination information
     * @return Page of projects with zero usage
     */
    Page<Project> findByTotalMinutesUsedEquals(Long minutes, Pageable pageable);

    /**
     * Update project's enabled status.
     * 
     * @param projectId The project ID
     * @param enabled The enabled status
     */
    @Modifying
    @Query("UPDATE Project p SET p.enabled = :enabled WHERE p.id = :projectId")
    void updateEnabled(@Param("projectId") Long projectId, @Param("enabled") Boolean enabled);

    /**
     * Update project usage statistics.
     * 
     * @param projectId The project ID
     * @param additionalMinutes Additional minutes to add
     * @param newPeakUsers New peak concurrent users (if higher than current)
     * @param additionalSessions Additional sessions to add
     */
    @Modifying
    @Query("UPDATE Project p SET " +
           "p.totalMinutesUsed = p.totalMinutesUsed + :additionalMinutes, " +
           "p.peakConcurrentUsers = CASE WHEN :newPeakUsers > p.peakConcurrentUsers THEN :newPeakUsers ELSE p.peakConcurrentUsers END, " +
           "p.totalSessions = p.totalSessions + :additionalSessions " +
           "WHERE p.id = :projectId")
    void updateUsageStatistics(@Param("projectId") Long projectId,
                              @Param("additionalMinutes") Long additionalMinutes,
                              @Param("newPeakUsers") Integer newPeakUsers,
                              @Param("additionalSessions") Long additionalSessions);

    /**
     * Get project statistics by date range.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Project statistics
     */
    @Query("SELECT " +
           "COUNT(p) as totalProjects, " +
           "COUNT(CASE WHEN p.enabled = true THEN 1 END) as enabledProjects, " +
           "SUM(p.totalMinutesUsed) as totalMinutesUsed, " +
           "SUM(p.totalSessions) as totalSessions, " +
           "MAX(p.peakConcurrentUsers) as maxPeakUsers, " +
           "AVG(p.totalMinutesUsed) as avgMinutesPerProject " +
           "FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Object[] getProjectStatistics(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get usage statistics for a specific user's projects.
     * 
     * @param owner The project owner
     * @return Usage statistics for the user's projects
     */
    @Query("SELECT " +
           "COUNT(p) as totalProjects, " +
           "SUM(p.totalMinutesUsed) as totalMinutesUsed, " +
           "SUM(p.totalSessions) as totalSessions, " +
           "MAX(p.peakConcurrentUsers) as maxPeakUsers " +
           "FROM Project p WHERE p.owner = :owner AND p.enabled = true")
    Object[] getUserProjectStatistics(@Param("owner") User owner);

    /**
     * Find projects that haven't been used recently.
     * 
     * @param cutoffDate The cutoff date for last usage
     * @param pageable Pagination information
     * @return Page of inactive projects
     */
    @Query("SELECT p FROM Project p WHERE p.updatedAt < :cutoffDate AND p.totalMinutesUsed = 0")
    Page<Project> findInactiveProjects(@Param("cutoffDate") Instant cutoffDate, Pageable pageable);

    /**
     * Delete projects that are disabled and have no usage.
     * 
     * @param cutoffDate The cutoff date
     * @return Number of deleted projects
     */
    @Modifying
    @Query("DELETE FROM Project p WHERE p.enabled = false AND " +
           "p.totalMinutesUsed = 0 AND p.totalSessions = 0 AND " +
           "p.createdAt < :cutoffDate")
    int deleteUnusedDisabledProjects(@Param("cutoffDate") Instant cutoffDate);
}

