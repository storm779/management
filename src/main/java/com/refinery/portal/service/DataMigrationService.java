package com.refinery.portal.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.refinery.portal.entity.MessageBoard;
import com.refinery.portal.repository.MessageBoardRepository;

@Service
@Transactional
public class DataMigrationService {

    @Autowired
    private MessageBoardRepository messageBoardRepository;

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yy");
    private static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static class MigrationResult {
        private int totalRecords;
        private int successfulRecords;
        private int failedRecords;
        private List<String> errors;

        public MigrationResult() {
            this.errors = new ArrayList<>();
        }

        // Getters and setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getSuccessfulRecords() { return successfulRecords; }
        public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }
        
        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public void addError(String error) { this.errors.add(error); }
        
        public void incrementSuccessful() { this.successfulRecords++; }
        public void incrementFailed() { this.failedRecords++; }
    }

    /**
     * Migrate data from CSV file to Message Board
     * CSV Format: MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI
     */
    public MigrationResult migrateCsvData(String csvFilePath) {
        MigrationResult result = new MigrationResult();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                try {
                    MessageBoard messageBoard = parseCsvLine(line, lineNumber);
                    if (messageBoard != null) {
                        messageBoardRepository.save(messageBoard);
                        result.incrementSuccessful();
                    }
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            result.setTotalRecords(result.getSuccessfulRecords() + result.getFailedRecords());
            
        } catch (IOException e) {
            result.addError("Failed to read CSV file: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Parse a single CSV line and create MessageBoard entity
     */
    private MessageBoard parseCsvLine(String line, int lineNumber) throws Exception {
        // Handle CSV parsing with proper quote handling
        String[] fields = parseCsvFields(line);
        
        if (fields.length < 11) {
            throw new Exception("Invalid CSV format - expected 11 fields, got " + fields.length);
        }
        
        try {
            MessageBoard messageBoard = new MessageBoard();
            
            // Map CSV fields to entity fields
            // CSV: MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI
            //      0     1       2        3        4     5       6          7         8       9             10
            
            // Original MSGID (not used - we use auto-generated ID)
            // String originalMsgId = fields[0].trim();
            
            // Message content (required)
            String message = fields[1].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
            if (message.isEmpty()) {
                throw new Exception("Message content is required");
            }
            messageBoard.setMessage(message);
            
            // Valid To date
            String validUptoStr = fields[2].trim();
            if (!validUptoStr.isEmpty()) {
                LocalDate validTo = parseDate(validUptoStr);
                messageBoard.setValidTo(validTo);
            }
            
            // Priority
            String priorityStr = fields[3].trim();
            if (!priorityStr.isEmpty()) {
                messageBoard.setPriority(Integer.parseInt(priorityStr));
            } else {
                messageBoard.setPriority(1); // Default priority
            }
            
            // Created by (MSGBY)
            String createdBy = fields[4].trim();
            messageBoard.setCreatedBy(createdBy);
            
            // Date timestamp (DTSTAMP) - not directly used, but can set created date
            // String dtstamp = fields[5].trim();
            
            // Message header (required)
            String header = fields[6].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
            if (header.isEmpty()) {
                throw new Exception("Message header is required");
            }
            messageBoard.setHeader(header);
            
            // Valid From date (required)
            String validFromStr = fields[7].trim();
            if (validFromStr.isEmpty()) {
                throw new Exception("Valid from date is required");
            }
            LocalDate validFrom = parseDate(validFromStr);
            messageBoard.setValidFrom(validFrom);
            
            // Enabled status
            String enabledStr = fields[8].trim().toUpperCase();
            boolean enabled = "Y".equals(enabledStr) || "YES".equals(enabledStr) || "TRUE".equals(enabledStr);
            messageBoard.setEnabled(enabled);
            
            // Hindi message (optional)
            if (fields.length > 9) {
                String messageHindi = fields[9].trim().replaceAll("^\"|\"$", "");
                if (!messageHindi.isEmpty()) {
                    messageBoard.setMessageHindi(messageHindi);
                }
            }
            
            // Hindi header (optional)
            if (fields.length > 10) {
                String headerHindi = fields[10].trim().replaceAll("^\"|\"$", "");
                if (!headerHindi.isEmpty()) {
                    messageBoard.setHeaderHindi(headerHindi);
                }
            }
            
            // Set default values for new fields
            messageBoard.setDisplayOrder(lineNumber - 1); // Use line number as display order
            messageBoard.setSpeed("normal");
            messageBoard.setColor("#000000");
            messageBoard.setBackgroundColor("#FFFFFF");
            
            // Set dates
            messageBoard.setCreatedDate(validFrom);
            messageBoard.setModifiedDate(LocalDate.now());
            messageBoard.setDateTimeStamp(LocalDateTime.now());
            
            return messageBoard;
            
        } catch (NumberFormatException e) {
            throw new Exception("Invalid number format: " + e.getMessage());
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format: " + e.getMessage());
        }
    }

    /**
     * Parse CSV fields handling quoted strings properly
     */
    private String[] parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }

    /**
     * Parse date from CSV format (M/d/yy) to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            // First try MM/dd/yy format
            return LocalDate.parse(dateStr, CSV_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            // Try other common formats
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e3) {
                    throw new DateTimeParseException("Unable to parse date: " + dateStr, dateStr, 0);
                }
            }
        }
    }

    /**
     * Clear all existing message board data (use with caution!)
     */
    public void clearAllMessageBoardData() {
        messageBoardRepository.deleteAll();
    }

    /**
     * Get migration statistics
     */
    public String getMigrationStatistics() {
        long totalMessages = messageBoardRepository.count();
        long activeMessages = messageBoardRepository.countByEnabledTrue();
        long inactiveMessages = messageBoardRepository.countByEnabledFalse();
        
        return String.format(
            "Migration Statistics:\n" +
            "Total Messages: %d\n" +
            "Active Messages: %d\n" +
            "Inactive Messages: %d",
            totalMessages, activeMessages, inactiveMessages
        );
    }
} 