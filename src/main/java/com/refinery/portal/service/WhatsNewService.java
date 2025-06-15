package com.refinery.portal.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.refinery.portal.entity.WhatsNew;
import com.refinery.portal.repository.WhatsNewRepository;

@Service
@Transactional
public class WhatsNewService {

    @Autowired
    private WhatsNewRepository whatsNewRepository;

    // Get active items for dashboard (top 5)
    public List<WhatsNew> getActiveWhatsNewForDashboard() {
        Pageable pageable = PageRequest.of(0, 5);
        return whatsNewRepository.findActiveWhatsNewForDashboard(pageable).getContent();
    }

    // Get all active items
    public List<WhatsNew> getAllActiveWhatsNew() {
        return whatsNewRepository.findActiveWhatsNew();
    }

    // Get all items with pagination
    public Page<WhatsNew> getAllWhatsNew(int page, int size, String sortBy, String sortDir) {
        // First ensure all items have display orders
        initializeDisplayOrdersIfNeeded();
        
        // Use proper repository method that orders by displayOrder for global sequential numbering
        Pageable pageable = PageRequest.of(page, size);
        return whatsNewRepository.findAllOrderByDisplayOrder(pageable);
    }

    // Get items by enabled status with pagination
    public Page<WhatsNew> getWhatsNewByEnabled(Boolean enabled, int page, int size) {
        // First ensure all items have display orders
        initializeDisplayOrdersIfNeeded();
        Pageable pageable = PageRequest.of(page, size);
        return whatsNewRepository.findByEnabledOrderByPriorityAscDisplayOrderAscValidFromDesc(enabled, pageable);
    }

    // Get items by date range
    public Page<WhatsNew> getWhatsNewByDateRange(LocalDate fromDate, LocalDate toDate, 
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return whatsNewRepository.findByValidFromBetween(fromDate, toDate, pageable);
    }

    // Get items by enabled status and date range
    public Page<WhatsNew> getWhatsNewByEnabledAndDateRange(Boolean enabled, LocalDate fromDate, 
                                                          LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return whatsNewRepository.findByEnabledAndValidFromBetween(enabled, fromDate, toDate, pageable);
    }

    // Search by title
    public Page<WhatsNew> searchWhatsNewByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return whatsNewRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    // Get item by ID
    public Optional<WhatsNew> getWhatsNewById(Long id) {
        return whatsNewRepository.findById(id);
    }

    // Save or update item with priority management
    public WhatsNew saveWhatsNew(WhatsNew whatsNew) {
        if (whatsNew.getId() == null) {
            // New item - handle priority ordering
            handlePriorityForNewItem(whatsNew);
            whatsNew.setCreatedDate(LocalDate.now());
        } else {
            // Existing item - handle priority changes
            WhatsNew existingItem = whatsNewRepository.findById(whatsNew.getId()).orElse(null);
            if (existingItem != null) {
                Integer oldPriority = existingItem.getPriority();
                Integer oldDisplayOrder = existingItem.getDisplayOrder();
                
                if (!whatsNew.getPriority().equals(oldPriority)) {
                    handlePriorityForUpdatedItem(whatsNew, oldPriority, oldDisplayOrder);
                }
        }
        whatsNew.setModifiedDate(LocalDate.now());
        }
        
        return whatsNewRepository.save(whatsNew);
    }

    private void handlePriorityForNewItem(WhatsNew newItem) {
        Integer targetPriority = newItem.getPriority();
        
        // Calculate global display order based on all existing items
        Integer globalDisplayOrder = calculateGlobalDisplayOrder(targetPriority);
        newItem.setDisplayOrder(globalDisplayOrder);
        
        // Shift existing items that should come after this one
        shiftItemsAfterPosition(targetPriority, globalDisplayOrder);
    }

    private void handlePriorityForUpdatedItem(WhatsNew updatedItem, Integer oldPriority, Integer oldDisplayOrder) {
        Integer newPriority = updatedItem.getPriority();
        
        // Remove from old position and shift items down
        if (oldPriority != null && oldDisplayOrder != null) {
            shiftItemsDownAfterRemoval(oldDisplayOrder);
        }
        
        // Calculate new global position and insert
        Integer globalDisplayOrder = calculateGlobalDisplayOrder(newPriority);
        updatedItem.setDisplayOrder(globalDisplayOrder);
        
        // Shift existing items that should come after this one
        shiftItemsAfterPosition(newPriority, globalDisplayOrder);
    }

    private Integer calculateGlobalDisplayOrder(Integer targetPriority) {
        // Count all items with higher priority (lower number = higher priority)
        int position = 1;
        
        for (int priority = 1; priority < targetPriority; priority++) {
            List<WhatsNew> itemsAtPriority = whatsNewRepository.findByPriorityOrderByDisplayOrder(priority);
            position += itemsAtPriority.size();
        }
        
        // Add count of existing items at the same priority level
        List<WhatsNew> itemsAtSamePriority = whatsNewRepository.findByPriorityOrderByDisplayOrder(targetPriority);
        position += itemsAtSamePriority.size();
        
        return position;
    }

    private void shiftItemsAfterPosition(Integer targetPriority, Integer fromPosition) {
        // Get all items that should come after this position
        List<WhatsNew> allItems = whatsNewRepository.findAll();
        
        for (WhatsNew item : allItems) {
            if (item.getDisplayOrder() != null && item.getDisplayOrder() >= fromPosition) {
                // Skip the item we're currently inserting
                if (!item.getPriority().equals(targetPriority) || item.getDisplayOrder() > fromPosition) {
                    item.setDisplayOrder(item.getDisplayOrder() + 1);
                    whatsNewRepository.save(item);
                }
            }
        }
    }

    private void shiftItemsDownAfterRemoval(Integer removedPosition) {
        // Get all items that come after the removed position
        List<WhatsNew> allItems = whatsNewRepository.findAll();
        
        for (WhatsNew item : allItems) {
            if (item.getDisplayOrder() != null && item.getDisplayOrder() > removedPosition) {
                item.setDisplayOrder(item.getDisplayOrder() - 1);
                whatsNewRepository.save(item);
            }
        }
    }

    // Delete item
    public void deleteWhatsNew(Long id) {
        whatsNewRepository.deleteById(id);
    }

    // Check if item exists
    public boolean existsById(Long id) {
        return whatsNewRepository.existsById(id);
    }

    // Get count of active items
    public long getActiveWhatsNewCount() {
        return whatsNewRepository.countActiveWhatsNew();
    }

    // Toggle enabled status
    public WhatsNew toggleEnabled(Long id) {
        Optional<WhatsNew> optionalWhatsNew = whatsNewRepository.findById(id);
        if (optionalWhatsNew.isPresent()) {
            WhatsNew whatsNew = optionalWhatsNew.get();
            whatsNew.setEnabled(!whatsNew.getEnabled());
            whatsNew.setModifiedDate(LocalDate.now());
            return whatsNewRepository.save(whatsNew);
        }
        throw new RuntimeException("WhatsNew item not found with id: " + id);
    }

    // Bulk operations
    public void deleteMultiple(List<Long> ids) {
        whatsNewRepository.deleteAllById(ids);
    }

    public void enableMultiple(List<Long> ids) {
        List<WhatsNew> items = whatsNewRepository.findAllById(ids);
        items.forEach(item -> {
            item.setEnabled(true);
            item.setModifiedDate(LocalDate.now());
        });
        whatsNewRepository.saveAll(items);
    }

    public void disableMultiple(List<Long> ids) {
        List<WhatsNew> items = whatsNewRepository.findAllById(ids);
        items.forEach(item -> {
            item.setEnabled(false);
            item.setModifiedDate(LocalDate.now());
        });
        whatsNewRepository.saveAll(items);
    }

    /**
     * Recalculate all display orders to ensure global sequential numbering
     * This method can be called to fix any inconsistencies in the numbering system
     */
    @Transactional
    public void recalculateAllDisplayOrders() {
        // Get all items ordered by priority and creation date
        List<WhatsNew> allItems = whatsNewRepository.findAll();
        
        // Sort by priority first, then by creation date
        allItems.sort((i1, i2) -> {
            int priorityCompare = i1.getPriority().compareTo(i2.getPriority());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // If same priority, sort by creation date (older first)
            if (i1.getCreatedDate() != null && i2.getCreatedDate() != null) {
                return i1.getCreatedDate().compareTo(i2.getCreatedDate());
            }
            return i1.getId().compareTo(i2.getId());
        });
        
        // Assign sequential display orders
        for (int i = 0; i < allItems.size(); i++) {
            WhatsNew item = allItems.get(i);
            item.setDisplayOrder(i + 1);
            whatsNewRepository.save(item);
        }
    }

    public Page<WhatsNew> getAllWhatsNew(Pageable pageable) {
        // First ensure all items have display orders
        initializeDisplayOrdersIfNeeded();
        return whatsNewRepository.findAll(pageable);
    }

    /**
     * Initialize display orders for existing records that don't have them or have incorrect ordering
     */
    @Transactional
    public void initializeDisplayOrdersIfNeeded() {
        List<WhatsNew> allItems = whatsNewRepository.findAll();
        
        // Check if there are any items without display orders
        boolean hasItemsWithoutOrder = allItems.stream()
            .anyMatch(w -> w.getDisplayOrder() == null);
        
        // Check if display orders are properly sequential (1, 2, 3, 4, 5...)
        boolean hasIncorrectSequence = false;
        if (!hasItemsWithoutOrder && !allItems.isEmpty()) {
            // Sort by priority first, then creation date (same as recalculation logic)
            allItems.sort((i1, i2) -> {
                int priorityCompare = i1.getPriority().compareTo(i2.getPriority());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                if (i1.getCreatedDate() != null && i2.getCreatedDate() != null) {
                    return i1.getCreatedDate().compareTo(i2.getCreatedDate());
                }
                return i1.getId().compareTo(i2.getId());
            });
            
            // Check if display orders match expected sequence
            for (int i = 0; i < allItems.size(); i++) {
                if (allItems.get(i).getDisplayOrder() == null || 
                    !allItems.get(i).getDisplayOrder().equals(i + 1)) {
                    hasIncorrectSequence = true;
                    break;
                }
            }
        }
        
        // Recalculate if needed
        if (hasItemsWithoutOrder || hasIncorrectSequence) {
            recalculateAllDisplayOrders();
        }
    }
} 