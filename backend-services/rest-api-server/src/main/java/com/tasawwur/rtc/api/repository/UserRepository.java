package com.tasawwur.rtc.api.repository;

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
 * Repository interface for User entity operations.
 * 
 * Provides data access methods for user management, authentication,
 * and user statistics.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * 
     * @param email The user's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by email address (case insensitive).
     * 
     * @param email The user's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if a user exists with the given email.
     * 
     * @param email The email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists with the given email (case insensitive).
     * 
     * @param email The email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find all enabled users.
     * 
     * @param pageable Pagination information
     * @return Page of enabled users
     */
    Page<User> findByEnabledTrue(Pageable pageable);

    /**
     * Find all users with verified emails.
     * 
     * @param pageable Pagination information
     * @return Page of users with verified emails
     */
    Page<User> findByEmailVerifiedTrue(Pageable pageable);

    /**
     * Find users by company name.
     * 
     * @param companyName The company name
     * @param pageable Pagination information
     * @return Page of users from the company
     */
    Page<User> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    /**
     * Find users created after a specific date.
     * 
     * @param date The date threshold
     * @param pageable Pagination information
     * @return Page of users created after the date
     */
    Page<User> findByCreatedAtAfter(Instant date, Pageable pageable);

    /**
     * Find users who logged in recently.
     * 
     * @param date The date threshold
     * @param pageable Pagination information
     * @return Page of users who logged in after the date
     */
    Page<User> findByLastLoginAtAfter(Instant date, Pageable pageable);

    /**
     * Search users by name or email.
     * 
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count total number of users.
     * 
     * @return Total user count
     */
    long count();

    /**
     * Count enabled users.
     * 
     * @return Count of enabled users
     */
    long countByEnabledTrue();

    /**
     * Count users with verified emails.
     * 
     * @return Count of users with verified emails
     */
    long countByEmailVerifiedTrue();

    /**
     * Count users created after a specific date.
     * 
     * @param date The date threshold
     * @return Count of users created after the date
     */
    long countByCreatedAtAfter(Instant date);

    /**
     * Count users who logged in after a specific date.
     * 
     * @param date The date threshold
     * @return Count of users who logged in after the date
     */
    long countByLastLoginAtAfter(Instant date);

    /**
     * Update user's last login timestamp.
     * 
     * @param userId The user ID
     * @param lastLoginAt The last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") Long userId, @Param("lastLoginAt") Instant lastLoginAt);

    /**
     * Update user's email verification status.
     * 
     * @param userId The user ID
     * @param verified The verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified WHERE u.id = :userId")
    void updateEmailVerified(@Param("userId") Long userId, @Param("verified") Boolean verified);

    /**
     * Update user's enabled status.
     * 
     * @param userId The user ID
     * @param enabled The enabled status
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Find top users by project count.
     * 
     * @param limit The maximum number of results
     * @return List of users with the most projects
     */
    @Query("SELECT u FROM User u LEFT JOIN u.projects p " +
           "GROUP BY u.id ORDER BY COUNT(p) DESC")
    List<User> findTopUsersByProjectCount(Pageable pageable);

    /**
     * Find users with no projects.
     * 
     * @param pageable Pagination information
     * @return Page of users with no projects
     */
    @Query("SELECT u FROM User u WHERE u.projects IS EMPTY")
    Page<User> findUsersWithNoProjects(Pageable pageable);

    /**
     * Get user statistics by date range.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return User statistics
     */
    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "COUNT(CASE WHEN u.emailVerified = true THEN 1 END) as verifiedUsers, " +
           "COUNT(CASE WHEN u.enabled = true THEN 1 END) as enabledUsers, " +
           "COUNT(CASE WHEN u.lastLoginAt > :startDate THEN 1 END) as activeUsers " +
           "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Object[] getUserStatistics(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Delete users who have been inactive for a long time and have no projects.
     * 
     * @param cutoffDate The cutoff date for inactivity
     * @return Number of deleted users
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.enabled = false AND " +
           "u.projects IS EMPTY AND " +
           "(u.lastLoginAt IS NULL OR u.lastLoginAt < :cutoffDate) AND " +
           "u.createdAt < :cutoffDate")
    int deleteInactiveUsersWithoutProjects(@Param("cutoffDate") Instant cutoffDate);
}
