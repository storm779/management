package com.refinery.portal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.refinery.portal.entity.WhatsNew;

@Repository
public interface WhatsNewRepository extends JpaRepository<WhatsNew, Long> {

    // Find all enabled items, ordered by global display order
    @Query("SELECT w FROM WhatsNew w WHERE w.enabled = true AND " +
           "(w.validTo IS NULL OR w.validTo >= CURRENT_DATE) AND " +
           "w.validFrom <= CURRENT_DATE " +
           "ORDER BY w.displayOrder ASC")
    List<WhatsNew> findActiveWhatsNew();

    // Find top N enabled items for dashboard
    @Query("SELECT w FROM WhatsNew w WHERE w.enabled = true AND " +
           "(w.validTo IS NULL OR w.validTo >= CURRENT_DATE) AND " +
           "w.validFrom <= CURRENT_DATE " +
           "ORDER BY w.displayOrder ASC")
    Page<WhatsNew> findActiveWhatsNewForDashboard(Pageable pageable);

    // Find by enabled status with pagination
    @Query("SELECT w FROM WhatsNew w WHERE w.enabled = :enabled " +
           "ORDER BY w.displayOrder ASC")
    Page<WhatsNew> findByEnabledOrderByPriorityAscDisplayOrderAscValidFromDesc(@Param("enabled") Boolean enabled, Pageable pageable);

    // Find by priority and get max display order
    @Query("SELECT COALESCE(MAX(w.displayOrder), 0) FROM WhatsNew w WHERE w.priority = :priority")
    Integer findMaxDisplayOrderByPriority(@Param("priority") Integer priority);

    // Find items by priority ordered by display order
    @Query("SELECT w FROM WhatsNew w WHERE w.priority = :priority " +
           "ORDER BY w.displayOrder ASC")
    List<WhatsNew> findByPriorityOrderByDisplayOrder(@Param("priority") Integer priority);

    // Update display order for items with same priority
    @Modifying
    @Query("UPDATE WhatsNew w SET w.displayOrder = w.displayOrder + 1 " +
           "WHERE w.priority = :priority AND w.displayOrder >= :fromOrder")
    void incrementDisplayOrderFromPosition(@Param("priority") Integer priority, @Param("fromOrder") Integer fromOrder);

    // Find by date range
    @Query("SELECT w FROM WhatsNew w WHERE w.validFrom >= :fromDate AND w.validFrom <= :toDate " +
           "ORDER BY w.validFrom DESC")
    Page<WhatsNew> findByValidFromBetween(@Param("fromDate") LocalDate fromDate, 
                                          @Param("toDate") LocalDate toDate, 
                                          Pageable pageable);

    // Find by enabled status and date range
    @Query("SELECT w FROM WhatsNew w WHERE w.enabled = :enabled AND " +
           "w.validFrom >= :fromDate AND w.validFrom <= :toDate " +
           "ORDER BY w.validFrom DESC")
    Page<WhatsNew> findByEnabledAndValidFromBetween(@Param("enabled") Boolean enabled,
                                                     @Param("fromDate") LocalDate fromDate, 
                                                     @Param("toDate") LocalDate toDate, 
                                                     Pageable pageable);

    // Find by title containing (for search)
    @Query("SELECT w FROM WhatsNew w WHERE LOWER(w.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY w.validFrom DESC")
    Page<WhatsNew> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    // Count active items
    @Query("SELECT COUNT(w) FROM WhatsNew w WHERE w.enabled = true AND " +
           "(w.validTo IS NULL OR w.validTo >= CURRENT_DATE) AND " +
           "w.validFrom <= CURRENT_DATE")
    long countActiveWhatsNew();

    // Find all items (enabled and disabled) ordered by display order
    @Query("SELECT w FROM WhatsNew w ORDER BY w.displayOrder ASC")
    Page<WhatsNew> findAllOrderByDisplayOrder(Pageable pageable);
} 