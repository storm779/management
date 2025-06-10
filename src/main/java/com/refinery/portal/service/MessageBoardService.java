package com.refinery.portal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.refinery.portal.entity.MessageBoard;
import com.refinery.portal.repository.MessageBoardRepository;

@Service
@Transactional
public class MessageBoardService {

    @Autowired
    private MessageBoardRepository messageBoardRepository;

    // Get all active messages
    public List<MessageBoard> getAllActiveMessages() {
        return messageBoardRepository.findActiveMessages();
    }

    // Get active messages for display with pagination
    public Page<MessageBoard> getActiveMessagesForDisplay(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findActiveMessagesForDisplay(pageable);
    }

    // Get all messages with pagination (for management)
    public Page<MessageBoard> getAllMessages(int page, int size) {
        // First ensure all messages have display orders
        initializeDisplayOrdersIfNeeded();
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findAllOrderedForManagement(pageable);
    }

    // Get all messages with pagination - overloaded method
    public Page<MessageBoard> getAllMessages(Pageable pageable) {
        // First ensure all messages have display orders
        initializeDisplayOrdersIfNeeded();
        return messageBoardRepository.findAllOrderedForManagement(pageable);
    }

    // Get messages by enabled status
    public Page<MessageBoard> getMessagesByEnabled(Boolean enabled, int page, int size) {
        // First ensure all messages have display orders
        initializeDisplayOrdersIfNeeded();
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findByEnabledOrderByPriorityAscDisplayOrderAscValidFromDesc(enabled, pageable);
    }

    // Get messages by enabled status - overloaded method with Pageable
    public Page<MessageBoard> getMessagesByStatus(Boolean enabled, Pageable pageable) {
        // First ensure all messages have display orders
        initializeDisplayOrdersIfNeeded();
        return messageBoardRepository.findByEnabledOrderByPriorityAscDisplayOrderAscValidFromDesc(enabled, pageable);
    }

    // Get messages by date range
    public Page<MessageBoard> getMessagesByDateRange(LocalDate fromDate, LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findByValidFromBetween(fromDate, toDate, pageable);
    }

    // Search messages by header
    public Page<MessageBoard> searchByHeader(String header, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findByHeaderContainingIgnoreCase(header, pageable);
    }

    // Search messages by content
    public Page<MessageBoard> searchByMessage(String message, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findByMessageContainingIgnoreCase(message, pageable);
    }

    // General search (header or message)
    public Page<MessageBoard> search(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageBoardRepository.findByHeaderOrMessageContainingIgnoreCase(searchTerm, pageable);
    }

    // Search messages with enabled filter and pagination
    public Page<MessageBoard> searchMessages(String searchTerm, Boolean enabled, Pageable pageable) {
        if (enabled != null) {
            return messageBoardRepository.findByHeaderOrMessageContainingIgnoreCaseAndEnabled(searchTerm, enabled, pageable);
        } else {
            return messageBoardRepository.findByHeaderOrMessageContainingIgnoreCase(searchTerm, pageable);
        }
    }

    // Search messages returning list (for API)
    public List<MessageBoard> searchMessagesList(String searchTerm, Boolean enabled) {
        if (enabled != null) {
            return messageBoardRepository.findByHeaderOrMessageContainingIgnoreCaseAndEnabled(searchTerm, enabled);
        } else {
            return messageBoardRepository.findByHeaderOrMessageContainingIgnoreCase(searchTerm);
        }
    }

    // Get messages by priority
    public Page<MessageBoard> getMessagesByPriority(Integer priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<MessageBoard> messages = messageBoardRepository.findByPriorityOrderByDisplayOrder(priority);
        // Convert list to page manually
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        List<MessageBoard> pageContent = messages.subList(start, end);
        return new PageImpl<>(pageContent, pageable, messages.size());
    }

    // Get message by ID
    public Optional<MessageBoard> getMessageById(Long id) {
        return messageBoardRepository.findById(id);
    }

    // Save message with priority management
    public MessageBoard saveMessage(MessageBoard messageBoard) {
        if (messageBoard.getId() == null) {
            // New message - handle priority ordering
            handlePriorityForNewMessage(messageBoard);
            messageBoard.setCreatedDate(LocalDate.now());
            messageBoard.setCreatedBy("System"); // You can get this from security context
        } else {
            // Existing message - handle priority changes
            MessageBoard existingMessage = messageBoardRepository.findById(messageBoard.getId()).orElse(null);
            if (existingMessage != null) {
                Integer oldPriority = existingMessage.getPriority();
                Integer oldDisplayOrder = existingMessage.getDisplayOrder();
                
                if (!messageBoard.getPriority().equals(oldPriority)) {
                    handlePriorityForUpdatedMessage(messageBoard, oldPriority, oldDisplayOrder);
                }
            }
            messageBoard.setModifiedDate(LocalDate.now());
        }
        
        messageBoard.setDateTimeStamp(LocalDateTime.now());
        return messageBoardRepository.save(messageBoard);
    }

    private void handlePriorityForNewMessage(MessageBoard newMessage) {
        Integer targetPriority = newMessage.getPriority();
        
        // Calculate global display order based on all existing messages
        Integer globalDisplayOrder = calculateGlobalDisplayOrder(targetPriority);
        newMessage.setDisplayOrder(globalDisplayOrder);
        
        // Shift existing messages that should come after this one
        shiftMessagesAfterPosition(targetPriority, globalDisplayOrder);
    }

    private void handlePriorityForUpdatedMessage(MessageBoard updatedMessage, Integer oldPriority, Integer oldDisplayOrder) {
        Integer newPriority = updatedMessage.getPriority();
        
        // Remove from old position and shift messages down
        if (oldPriority != null && oldDisplayOrder != null) {
            shiftMessagesDownAfterRemoval(oldDisplayOrder);
        }
        
        // Calculate new global position and insert
        Integer globalDisplayOrder = calculateGlobalDisplayOrder(newPriority);
        updatedMessage.setDisplayOrder(globalDisplayOrder);
        
        // Shift existing messages that should come after this one
        shiftMessagesAfterPosition(newPriority, globalDisplayOrder);
    }

    private Integer calculateGlobalDisplayOrder(Integer targetPriority) {
        // Count all messages with higher priority (lower number = higher priority)
        int position = 1;
        
        for (int priority = 1; priority < targetPriority; priority++) {
            List<MessageBoard> messagesAtPriority = messageBoardRepository.findByPriorityOrderByDisplayOrder(priority);
            position += messagesAtPriority.size();
        }
        
        // Add count of existing messages at the same priority level
        List<MessageBoard> messagesAtSamePriority = messageBoardRepository.findByPriorityOrderByDisplayOrder(targetPriority);
        position += messagesAtSamePriority.size();
        
        return position;
    }

    private void shiftMessagesAfterPosition(Integer targetPriority, Integer fromPosition) {
        // Get all messages that should come after this position
        List<MessageBoard> allMessages = messageBoardRepository.findAll();
        
        for (MessageBoard message : allMessages) {
            if (message.getDisplayOrder() != null && message.getDisplayOrder() >= fromPosition) {
                // Skip the message we're currently inserting
                if (!message.getPriority().equals(targetPriority) || message.getDisplayOrder() > fromPosition) {
                    message.setDisplayOrder(message.getDisplayOrder() + 1);
                    messageBoardRepository.save(message);
                }
            }
        }
    }

    private void shiftMessagesDownAfterRemoval(Integer removedPosition) {
        // Get all messages that come after the removed position
        List<MessageBoard> allMessages = messageBoardRepository.findAll();
        
        for (MessageBoard message : allMessages) {
            if (message.getDisplayOrder() != null && message.getDisplayOrder() > removedPosition) {
                message.setDisplayOrder(message.getDisplayOrder() - 1);
                messageBoardRepository.save(message);
            }
        }
    }

    // Delete message
    public void deleteMessage(Long id) {
        messageBoardRepository.deleteById(id);
    }

    // Check if message exists
    public boolean messageExists(Long id) {
        return messageBoardRepository.existsById(id);
    }

    // Count active messages
    public long countActiveMessages() {
        return messageBoardRepository.countActiveMessages();
    }

    // Get messages for scrolling display
    public List<MessageBoard> getMessagesForScrolling() {
        return messageBoardRepository.findMessagesForScrolling();
    }

    // Get top N active messages for dashboard
    public List<MessageBoard> getTopActiveMessages(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageBoardRepository.findActiveMessagesForDisplay(pageable).getContent();
    }

    // Bulk operations

    public void deleteMessages(List<Long> ids) {
        System.out.println("=== SERVICE DELETE MESSAGES ===");
        System.out.println("Received IDs in service: " + ids);
        System.out.println("About to delete " + ids.size() + " messages");
        
        messageBoardRepository.deleteAllById(ids);
        
        System.out.println("Deletion completed");
    }

    /**
     * Recalculate all display orders to ensure global sequential numbering
     * This method can be called to fix any inconsistencies in the numbering system
     */
    @Transactional
    public void recalculateAllDisplayOrders() {
        // Get all messages ordered by priority and creation date
        List<MessageBoard> allMessages = messageBoardRepository.findAll();
        
        // Sort by priority first, then by creation date
        allMessages.sort((m1, m2) -> {
            int priorityCompare = m1.getPriority().compareTo(m2.getPriority());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // If same priority, sort by creation date (older first)
            if (m1.getCreatedDate() != null && m2.getCreatedDate() != null) {
                return m1.getCreatedDate().compareTo(m2.getCreatedDate());
            }
            return m1.getId().compareTo(m2.getId());
        });
        
        // Assign sequential display orders
        for (int i = 0; i < allMessages.size(); i++) {
            MessageBoard message = allMessages.get(i);
            message.setDisplayOrder(i + 1);
            messageBoardRepository.save(message);
        }
    }

    /**
     * Initialize display orders for existing records that don't have them
     */
    @Transactional
    public void initializeDisplayOrdersIfNeeded() {
        // Check if there are any messages without display orders
        List<MessageBoard> messagesWithoutOrder = messageBoardRepository.findAll()
            .stream()
            .filter(m -> m.getDisplayOrder() == null)
            .collect(java.util.stream.Collectors.toList());
        
        if (!messagesWithoutOrder.isEmpty()) {
            recalculateAllDisplayOrders();
        }
    }
} 