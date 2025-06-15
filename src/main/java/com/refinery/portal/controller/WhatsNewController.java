package com.refinery.portal.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.refinery.portal.entity.WhatsNew;
import com.refinery.portal.service.WhatsNewService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/whatsnew")
public class WhatsNewController {

    @Autowired
    private WhatsNewService whatsNewService;

    // Dashboard endpoint - returns fragment for inclusion in main page
    @GetMapping("/dashboard")
    public String getDashboardFragment(Model model) {
        List<WhatsNew> activeItems = whatsNewService.getActiveWhatsNewForDashboard();
        model.addAttribute("whatsNewItems", activeItems);
        return "fragments/whatsnew-dashboard :: whatsnew-panel";
    }

    // Full list page with pagination and filtering
    @GetMapping("/list")
    public String getWhatsNewList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "validFrom") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String title,
            Model model) {

        // Ensure display orders are properly initialized/recalculated
        whatsNewService.initializeDisplayOrdersIfNeeded();

        Page<WhatsNew> whatsNewPage;

        // Apply filters
        if (title != null && !title.trim().isEmpty()) {
            whatsNewPage = whatsNewService.searchWhatsNewByTitle(title.trim(), page, size);
        } else if (enabled != null && fromDate != null && toDate != null) {
            whatsNewPage = whatsNewService.getWhatsNewByEnabledAndDateRange(enabled, fromDate, toDate, page, size);
        } else if (fromDate != null && toDate != null) {
            whatsNewPage = whatsNewService.getWhatsNewByDateRange(fromDate, toDate, page, size);
        } else if (enabled != null) {
            whatsNewPage = whatsNewService.getWhatsNewByEnabled(enabled, page, size);
        } else {
            whatsNewPage = whatsNewService.getAllWhatsNew(page, size, sortBy, sortDir);
        }

        model.addAttribute("whatsNewPage", whatsNewPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        // Filter parameters
        model.addAttribute("enabled", enabled);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("title", title);
        
        // Add statistics for dashboard cards
        model.addAttribute("activeCount", whatsNewService.getActiveWhatsNewCount());
        model.addAttribute("totalCount", whatsNewPage.getTotalElements());
        model.addAttribute("totalPages", whatsNewPage.getTotalPages());

        return "whatsnew/list";
    }

    // Show add form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("whatsNew", new WhatsNew());
        model.addAttribute("isEdit", false);
        return "whatsnew/form";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<WhatsNew> whatsNew = whatsNewService.getWhatsNewById(id);
        if (whatsNew.isPresent()) {
            model.addAttribute("whatsNew", whatsNew.get());
            model.addAttribute("isEdit", true);
            return "whatsnew/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "WhatsNew item not found!");
            return "redirect:/whatsnew/list";
        }
    }

    // Save (create or update)
    @PostMapping("/save")
    public String saveWhatsNew(@Valid @ModelAttribute WhatsNew whatsNew, 
                              BindingResult bindingResult, 
                              @RequestParam(defaultValue = "list") String returnTo,
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", whatsNew.getId() != null);
            return "whatsnew/form";
        }

        try {
            WhatsNew savedItem = whatsNewService.saveWhatsNew(whatsNew);
            String message = whatsNew.getId() != null ? "WhatsNew item updated successfully!" : "WhatsNew item created successfully!";
            redirectAttributes.addFlashAttribute("success", message);
            
            // Redirect to view if it's an edit and returnTo is view
            if (whatsNew.getId() != null && "view".equals(returnTo)) {
                return "redirect:/whatsnew/view/" + savedItem.getId();
            }
            
            return "redirect:/whatsnew/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error saving WhatsNew item: " + e.getMessage());
            model.addAttribute("isEdit", whatsNew.getId() != null);
            return "whatsnew/form";
        }
    }

    // View details
    @GetMapping("/view/{id}")
    public String viewWhatsNew(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<WhatsNew> whatsNew = whatsNewService.getWhatsNewById(id);
        if (whatsNew.isPresent()) {
            model.addAttribute("whatsNew", whatsNew.get());
            return "whatsnew/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "WhatsNew item not found!");
            return "redirect:/whatsnew/list";
        }
    }

    // Delete
    @GetMapping("/delete/{id}")
    public String deleteWhatsNewGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return deleteWhatsNew(id, redirectAttributes);
    }

    @PostMapping("/delete/{id}")
    public String deleteWhatsNew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (whatsNewService.existsById(id)) {
                whatsNewService.deleteWhatsNew(id);
                redirectAttributes.addFlashAttribute("success", "WhatsNew item deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "WhatsNew item not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting WhatsNew item: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }

    // Toggle enabled status
    @PostMapping("/toggle/{id}")
    public String toggleEnabled(@PathVariable Long id, 
                               @RequestParam(defaultValue = "list") String returnTo,
                               RedirectAttributes redirectAttributes) {
        try {
            WhatsNew updatedItem = whatsNewService.toggleEnabled(id);
            String status = updatedItem.getEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("success", "WhatsNew item " + status + " successfully!");
            
            // Return to view page if requested, otherwise to list
            if ("view".equals(returnTo)) {
                return "redirect:/whatsnew/view/" + id;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating WhatsNew item: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }

    // Bulk operations
    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam("ids") List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            whatsNewService.deleteMultiple(ids);
            redirectAttributes.addFlashAttribute("success", "Selected items deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting items: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }

    @PostMapping("/bulk-enable")
    public String bulkEnable(@RequestParam("ids") List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            whatsNewService.enableMultiple(ids);
            redirectAttributes.addFlashAttribute("success", "Selected items enabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error enabling items: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }

    @PostMapping("/bulk-disable")
    public String bulkDisable(@RequestParam("ids") List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            whatsNewService.disableMultiple(ids);
            redirectAttributes.addFlashAttribute("success", "Selected items disabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error disabling items: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }

    @PostMapping("/admin/recalculate-order")
    @PreAuthorize("hasRole('ADMIN')")
    public String recalculateDisplayOrder(RedirectAttributes redirectAttributes) {
        try {
            whatsNewService.recalculateAllDisplayOrders();
            redirectAttributes.addFlashAttribute("successMessage", "Display orders recalculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recalculating display orders: " + e.getMessage());
        }
        return "redirect:/whatsnew/list";
    }
} 
