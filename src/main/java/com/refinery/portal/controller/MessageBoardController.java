package com.refinery.portal.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.refinery.portal.entity.MessageBoard;
import com.refinery.portal.service.MessageBoardService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/messageboard")
public class MessageBoardController {

    @Autowired
    private MessageBoardService messageBoardService;

    // List all messages with pagination and search
    @GetMapping("/list")
    public String listMessages(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "5") int size,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) Boolean enabled,
                              @RequestParam(required = false) String sort) {
        
        Page<MessageBoard> messagesPage;
        
        // Apply search and filter logic
        if (search != null && !search.trim().isEmpty()) {
            messagesPage = messageBoardService.search(search.trim(), page, size);
            model.addAttribute("search", search);
        } else if (enabled != null) {
            messagesPage = messageBoardService.getMessagesByEnabled(enabled, page, size);
            model.addAttribute("enabled", enabled);
        } else {
            messagesPage = messageBoardService.getAllMessages(page, size);
        }
        
        model.addAttribute("messagesPage", messagesPage != null ? messagesPage : new PageImpl<>(new ArrayList<>()));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", messagesPage != null ? messagesPage.getTotalPages() : 0);
        model.addAttribute("totalElements", messagesPage != null ? messagesPage.getTotalElements() : 0);
        model.addAttribute("hasNext", messagesPage != null ? messagesPage.hasNext() : false);
        model.addAttribute("hasPrevious", messagesPage != null ? messagesPage.hasPrevious() : false);
        
        // Count statistics
        long activeCount = messageBoardService.countActiveMessages();
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalCount", messagesPage != null ? messagesPage.getTotalElements() : 0);
        
        return "messageboard/list";
    }

    // Show form for adding new message
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddForm(Model model) {
        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setValidFrom(LocalDate.now());
        messageBoard.setEnabled(true);
        messageBoard.setPriority(1);
        
        model.addAttribute("messageBoard", messageBoard);
        model.addAttribute("isEdit", false);
        model.addAttribute("formTitle", "Add New Message");
        
        return "messageboard/form";
    }

    // Show form for editing existing message
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<MessageBoard> optionalMessage = messageBoardService.getMessageById(id);
        
        if (optionalMessage.isPresent()) {
            model.addAttribute("messageBoard", optionalMessage.get());
            model.addAttribute("isEdit", true);
            model.addAttribute("formTitle", "Edit Message");
            return "messageboard/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Message not found!");
            return "redirect:/messageboard/list";
        }
    }

    // View message details
    @GetMapping("/view/{id}")
    public String viewMessage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<MessageBoard> optionalMessage = messageBoardService.getMessageById(id);
        
        if (optionalMessage.isPresent()) {
            model.addAttribute("messageBoard", optionalMessage.get());
            return "messageboard/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Message not found!");
            return "redirect:/messageboard/list";
        }
    }

    // Save message (both add and edit)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public String saveMessage(@Valid @ModelAttribute MessageBoard messageBoard,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", messageBoard.getId() != null);
            model.addAttribute("formTitle", messageBoard.getId() != null ? "Edit Message" : "Add New Message");
            return "messageboard/form";
        }
        
        try {
            // Save the message
            MessageBoard savedMessage = messageBoardService.saveMessage(messageBoard);
            
            String action = messageBoard.getId() != null ? "updated" : "added";
            redirectAttributes.addFlashAttribute("success", 
                "Message '" + savedMessage.getHeader() + "' has been " + action + " successfully!");
            
            return "redirect:/messageboard/list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error saving message: " + e.getMessage());
            model.addAttribute("isEdit", messageBoard.getId() != null);
            model.addAttribute("formTitle", messageBoard.getId() != null ? "Edit Message" : "Add New Message");
            return "messageboard/form";
        }
    }

    // Delete message
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("=== INDIVIDUAL DELETE CALLED ===");
        System.out.println("Delete message ID: " + id);
        
        try {
            Optional<MessageBoard> optionalMessage = messageBoardService.getMessageById(id);
            
            if (optionalMessage.isPresent()) {
                MessageBoard message = optionalMessage.get();
                System.out.println("Found message: " + message.getHeader());
                messageBoardService.deleteMessage(id);
                System.out.println("Successfully deleted message: " + message.getHeader());
                redirectAttributes.addFlashAttribute("success", 
                    "Message '" + message.getHeader() + "' has been deleted successfully!");
            } else {
                System.out.println("Message not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Message not found!");
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting message: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting message: " + e.getMessage());
        }
        
        return "redirect:/messageboard/list";
    }



    // API endpoint for getting active messages (for AJAX/JSON responses)
    @GetMapping("/api/active")
    public ResponseEntity<List<MessageBoard>> getActiveMessages() {
        try {
            List<MessageBoard> activeMessages = messageBoardService.getAllActiveMessages();
            return ResponseEntity.ok(activeMessages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // API endpoint for scrolling messages
    @GetMapping("/api/scrolling")
    public ResponseEntity<List<MessageBoard>> getScrollingMessages() {
        try {
            List<MessageBoard> scrollingMessages = messageBoardService.getMessagesForScrolling();
            return ResponseEntity.ok(scrollingMessages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // API endpoint for all messages with pagination
    @GetMapping("/api/messages")
    public ResponseEntity<Page<MessageBoard>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageBoard> messagesPage;
            
            if (search != null && !search.trim().isEmpty()) {
                messagesPage = messageBoardService.searchMessages(search, enabled, pageable);
            } else if (enabled != null) {
                messagesPage = messageBoardService.getMessagesByStatus(enabled, pageable);
            } else {
                messagesPage = messageBoardService.getAllMessages(pageable);
            }
            
            return ResponseEntity.ok(messagesPage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // API endpoint for search
    @GetMapping("/api/search")
    public ResponseEntity<List<MessageBoard>> searchMessages(
            @RequestParam String query,
            @RequestParam(required = false) Boolean enabled) {
        try {
            List<MessageBoard> messages = messageBoardService.searchMessagesList(query, enabled);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Bulk actions

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk/delete")
    public String bulkDelete(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        System.out.println("=== BULK DELETE CALLED ===");
        System.out.println("Received IDs: " + ids);
        System.out.println("Number of IDs: " + (ids != null ? ids.size() : "null"));
        
        try {
            if (ids == null || ids.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No messages selected for deletion!");
                return "redirect:/messageboard/list";
            }
            
            messageBoardService.deleteMessages(ids);
            System.out.println("Successfully deleted " + ids.size() + " messages");
            redirectAttributes.addFlashAttribute("success", 
                ids.size() + " message(s) have been deleted successfully!");
        } catch (Exception e) {
            System.out.println("Error deleting messages: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting messages: " + e.getMessage());
        }
        
        return "redirect:/messageboard/list";
    }

    @PostMapping("/admin/recalculate-order")
    @PreAuthorize("hasRole('ADMIN')")
    public String recalculateDisplayOrder(RedirectAttributes redirectAttributes) {
        try {
            messageBoardService.recalculateAllDisplayOrders();
            redirectAttributes.addFlashAttribute("successMessage", "Display orders recalculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recalculating display orders: " + e.getMessage());
        }
        return "redirect:/messageboard/list";
    }
} 