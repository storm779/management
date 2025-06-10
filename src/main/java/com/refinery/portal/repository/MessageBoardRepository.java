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

import com.refinery.portal.entity.MessageBoard;

@Repository
public interface MessageBoardRepository extends JpaRepository<MessageBoard, Long> {

    // Find all enabled messages, ordered by global display order
    @Query("SELECT m FROM MessageBoard m WHERE m.enabled = true AND " +
           "(m.validTo IS NULL OR m.validTo >= CURRENT_DATE) AND " +
           "m.validFrom <= CURRENT_DATE " +
           "ORDER BY m.displayOrder ASC")
    List<MessageBoard> findActiveMessages();

    // Find top N enabled messages for display
    @Query("SELECT m FROM MessageBoard m WHERE m.enabled = true AND " +
           "(m.validTo IS NULL OR m.validTo >= CURRENT_DATE) AND " +
           "m.validFrom <= CURRENT_DATE " +
           "ORDER BY m.displayOrder ASC")
    Page<MessageBoard> findActiveMessagesForDisplay(Pageable pageable);

    // Find by enabled status with pagination
    @Query("SELECT m FROM MessageBoard m WHERE m.enabled = :enabled " +
           "ORDER BY m.displayOrder ASC")
    Page<MessageBoard> findByEnabledOrderByPriorityAscDisplayOrderAscValidFromDesc(@Param("enabled") Boolean enabled, Pageable pageable);

    // Find all with pagination ordered for management
    @Query("SELECT m FROM MessageBoard m ORDER BY m.displayOrder ASC")
    Page<MessageBoard> findAllOrderedForManagement(Pageable pageable);

    // Find by date range
    @Query("SELECT m FROM MessageBoard m WHERE m.validFrom >= :fromDate AND m.validFrom <= :toDate " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByValidFromBetween(@Param("fromDate") LocalDate fromDate, 
                                              @Param("toDate") LocalDate toDate, 
                                              Pageable pageable);

    // Find by enabled status and date range
    @Query("SELECT m FROM MessageBoard m WHERE m.enabled = :enabled AND " +
           "m.validFrom >= :fromDate AND m.validFrom <= :toDate " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByEnabledAndValidFromBetween(@Param("enabled") Boolean enabled,
                                                         @Param("fromDate") LocalDate fromDate, 
                                                         @Param("toDate") LocalDate toDate, 
                                                         Pageable pageable);

    // Find by header containing (for search)
    @Query("SELECT m FROM MessageBoard m WHERE LOWER(m.header) LIKE LOWER(CONCAT('%', :header, '%')) " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByHeaderContainingIgnoreCase(@Param("header") String header, Pageable pageable);

    // Find by message content containing (for search)
    @Query("SELECT m FROM MessageBoard m WHERE LOWER(m.message) LIKE LOWER(CONCAT('%', :message, '%')) " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByMessageContainingIgnoreCase(@Param("message") String message, Pageable pageable);

    // Find by header or message containing (for general search)
    @Query("SELECT m FROM MessageBoard m WHERE " +
           "LOWER(m.header) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByHeaderOrMessageContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find by header or message containing with enabled filter (pageable)
    @Query("SELECT m FROM MessageBoard m WHERE " +
           "(LOWER(m.header) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "m.enabled = :enabled " +
           "ORDER BY m.validFrom DESC")
    Page<MessageBoard> findByHeaderOrMessageContainingIgnoreCaseAndEnabled(@Param("searchTerm") String searchTerm, 
                                                                            @Param("enabled") Boolean enabled, 
                                                                            Pageable pageable);

    // Find by header or message containing with enabled filter (list)
    @Query("SELECT m FROM MessageBoard m WHERE " +
           "(LOWER(m.header) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "m.enabled = :enabled " +
           "ORDER BY m.validFrom DESC")
    List<MessageBoard> findByHeaderOrMessageContainingIgnoreCaseAndEnabled(@Param("searchTerm") String searchTerm, 
                                                                            @Param("enabled") Boolean enabled);

    // Find by header or message containing (list version)
    @Query("SELECT m FROM MessageBoard m WHERE " +
           "LOWER(m.header) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY m.validFrom DESC")
    List<MessageBoard> findByHeaderOrMessageContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Count active messages
    @Query("SELECT COUNT(m) FROM MessageBoard m WHERE m.enabled = true AND " +
           "(m.validTo IS NULL OR m.validTo >= CURRENT_DATE) AND " +
           "m.validFrom <= CURRENT_DATE")
    long countActiveMessages();

    // Count messages by enabled status
    long countByEnabledTrue();
    
    long countByEnabledFalse();

    // Find by priority and get max display order
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MessageBoard m WHERE m.priority = :priority")
    Integer findMaxDisplayOrderByPriority(@Param("priority") Integer priority);

    // Find messages by priority ordered by display order
    @Query("SELECT m FROM MessageBoard m WHERE m.priority = :priority " +
           "ORDER BY m.displayOrder ASC")
    List<MessageBoard> findByPriorityOrderByDisplayOrder(@Param("priority") Integer priority);

    // Find messages with priority greater than or equal to specified priority
    @Query("SELECT m FROM MessageBoard m WHERE m.priority >= :priority " +
           "ORDER BY m.priority ASC, m.displayOrder ASC")
    List<MessageBoard> findByPriorityGreaterThanEqualOrderByPriorityAscDisplayOrderAsc(@Param("priority") Integer priority);

    // Update display order for messages with priority greater than specified
    @Modifying
    @Query("UPDATE MessageBoard m SET m.displayOrder = m.displayOrder + 1 " +
           "WHERE m.priority = :priority AND m.displayOrder >= :fromOrder")
    void incrementDisplayOrderFromPosition(@Param("priority") Integer priority, @Param("fromOrder") Integer fromOrder);

    // Shift all messages with higher priority down by incrementing their priority
    @Modifying
    @Query("UPDATE MessageBoard m SET m.priority = m.priority + 1 " +
           "WHERE m.priority >= :fromPriority")
    void shiftPrioritiesDown(@Param("fromPriority") Integer fromPriority);

    // Find messages for scrolling display (active, ordered by display order)
    @Query("SELECT m FROM MessageBoard m WHERE m.enabled = true AND " +
           "(m.validTo IS NULL OR m.validTo >= CURRENT_DATE) AND " +
           "m.validFrom <= CURRENT_DATE " +
           "ORDER BY m.displayOrder ASC")
    List<MessageBoard> findMessagesForScrolling();
} 