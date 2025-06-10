package com.refinery.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.refinery.portal.service.DataMigrationService;
import com.refinery.portal.service.DataMigrationService.MigrationResult;

@Controller
@RequestMapping("/admin/migration")
@PreAuthorize("hasRole('ADMIN')")
public class DataMigrationController {

    @Autowired
    private DataMigrationService dataMigrationService;

    @GetMapping
    public String migrationPage(Model model) {
        model.addAttribute("pageTitle", "Data Migration - Admin");
        model.addAttribute("statistics", dataMigrationService.getMigrationStatistics());
        return "admin/migration";
    }

    @PostMapping("/csv")
    public String migrateCsvData(@RequestParam(defaultValue = "vrp_scrollmsg.csv") String csvFileName,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Migration will look for the CSV file in the project root
            String csvFilePath = csvFileName;
            MigrationResult result = dataMigrationService.migrateCsvData(csvFilePath);
            
            if (result.getSuccessfulRecords() > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    String.format("Migration completed successfully! " +
                                  "Total: %d, Successful: %d, Failed: %d", 
                                  result.getTotalRecords(), 
                                  result.getSuccessfulRecords(), 
                                  result.getFailedRecords()));
                
                if (!result.getErrors().isEmpty()) {
                    redirectAttributes.addFlashAttribute("warning", 
                        "Some records failed to migrate: " + String.join(", ", result.getErrors()));
                }
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Migration failed. Errors: " + String.join(", ", result.getErrors()));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Migration failed with exception: " + e.getMessage());
        }
        
        return "redirect:/admin/migration";
    }

    @PostMapping("/clear")
    public String clearData(RedirectAttributes redirectAttributes) {
        try {
            dataMigrationService.clearAllMessageBoardData();
            redirectAttributes.addFlashAttribute("success", 
                "All message board data has been cleared successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to clear data: " + e.getMessage());
        }
        
        return "redirect:/admin/migration";
    }
} 