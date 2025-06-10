-- Database schema for Visakh Refinery Portal
-- MariaDB/MySQL DDL Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS refweb_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE refweb_portal;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS vrp_whatsnew;
DROP TABLE IF EXISTS vrp_events;
DROP TABLE IF EXISTS vrp_scrollmsg;
DROP TABLE IF EXISTS vrp_imp_msg;
DROP TABLE IF EXISTS vrp_portal_info;
DROP TABLE IF EXISTS pinfo_mtype;
DROP TABLE IF EXISTS mrp_hindi_word;
DROP TABLE IF EXISTS vrp_telugu_word;
DROP TABLE IF EXISTS hit_counters;
DROP TABLE IF EXISTS vrp_users;

-- Table: vrp_whatsnew (What's New announcements)
CREATE TABLE vrp_whatsnew (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TITLE VARCHAR(200) NOT NULL,
    DESCRIPTION TEXT,
    URL VARCHAR(500),
    VALIDFROM DATE NOT NULL,
    VALIDTO DATE,
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    PRIORITY INTEGER DEFAULT 0,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_enabled (ENABLED),
    INDEX idx_validfrom (VALIDFROM),
    INDEX idx_priority (PRIORITY),
    INDEX idx_created_date (CREATED_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_events (Events and calendar items)
CREATE TABLE vrp_events (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TITLE VARCHAR(200) NOT NULL,
    DESCRIPTION TEXT,
    EVENT_DATE DATE NOT NULL,
    EVENT_TIME TIME,
    LOCATION VARCHAR(200),
    CATEGORY VARCHAR(50),
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_event_date (EVENT_DATE),
    INDEX idx_enabled (ENABLED),
    INDEX idx_category (CATEGORY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_scrollmsg (Scrolling messages/tickers) - Enhanced for Message Board System
CREATE TABLE vrp_scrollmsg (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    MSG_HEADER VARCHAR(200) NOT NULL,
    MESSAGE TEXT NOT NULL,
    MESSAGE_HINDI TEXT,
    MSG_HEADER_HINDI VARCHAR(200),
    VALIDFROM DATE NOT NULL,
    VALIDTO DATE,
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    PRIORITY INTEGER DEFAULT 1,
    DISPLAY_ORDER INTEGER DEFAULT 0,
    SPEED VARCHAR(20) DEFAULT 'normal',
    COLOR VARCHAR(20) DEFAULT '#000000',
    BGCOLOR VARCHAR(20) DEFAULT '#FFFFFF',
    MSGBY VARCHAR(50),
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    DTSTAMP DATETIME,
    
    INDEX idx_enabled (ENABLED),
    INDEX idx_display_order (DISPLAY_ORDER),
    INDEX idx_validfrom (VALIDFROM),
    INDEX idx_priority (PRIORITY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_imp_msg (Important messages/alerts)
CREATE TABLE vrp_imp_msg (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TITLE VARCHAR(200) NOT NULL,
    MESSAGE TEXT NOT NULL,
    MSG_TYPE VARCHAR(20) DEFAULT 'info', -- info, warning, error, success
    PRIORITY INTEGER DEFAULT 0,
    SHOW_POPUP BOOLEAN DEFAULT FALSE,
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    VALIDFROM DATE,
    VALIDTO DATE,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_enabled (ENABLED),
    INDEX idx_msg_type (MSG_TYPE),
    INDEX idx_priority (PRIORITY),
    INDEX idx_validfrom (VALIDFROM)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_portal_info (Portal information and configurations)
CREATE TABLE vrp_portal_info (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    INFO_KEY VARCHAR(100) NOT NULL UNIQUE,
    INFO_VALUE TEXT,
    INFO_TYPE VARCHAR(50),
    CATEGORY VARCHAR(50),
    DESCRIPTION VARCHAR(500),
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_info_key (INFO_KEY),
    INDEX idx_category (CATEGORY),
    INDEX idx_enabled (ENABLED)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: pinfo_mtype (Portal info message types)
CREATE TABLE pinfo_mtype (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE_CODE VARCHAR(20) NOT NULL UNIQUE,
    TYPE_NAME VARCHAR(100) NOT NULL,
    DESCRIPTION VARCHAR(500),
    ICON_CLASS VARCHAR(50),
    COLOR_CLASS VARCHAR(50),
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_DATE DATE,
    
    INDEX idx_type_code (TYPE_CODE),
    INDEX idx_enabled (ENABLED)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: mrp_hindi_word (Hindi word of the day)
CREATE TABLE mrp_hindi_word (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    HINDI_WORD VARCHAR(200) NOT NULL,
    ENGLISH_MEANING VARCHAR(500) NOT NULL,
    PRONUNCIATION VARCHAR(200),
    USAGE_EXAMPLE TEXT,
    DISPLAY_DATE DATE,
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_display_date (DISPLAY_DATE),
    INDEX idx_enabled (ENABLED)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_telugu_word (Telugu word of the day)
CREATE TABLE vrp_telugu_word (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TELUGU_WORD VARCHAR(200) NOT NULL,
    ENGLISH_MEANING VARCHAR(500) NOT NULL,
    PRONUNCIATION VARCHAR(200),
    USAGE_EXAMPLE TEXT,
    DISPLAY_DATE DATE,
    ENABLED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_DATE DATE,
    MODIFIED_DATE DATE,
    
    INDEX idx_display_date (DISPLAY_DATE),
    INDEX idx_enabled (ENABLED)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: hit_counters (Page hit counters and analytics)
CREATE TABLE hit_counters (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    PAGE_NAME VARCHAR(100) NOT NULL,
    HIT_DATE DATE NOT NULL,
    HIT_COUNT BIGINT DEFAULT 0,
    IP_ADDRESS VARCHAR(45),
    USER_AGENT TEXT,
    REFERER VARCHAR(500),
    
    UNIQUE KEY uk_page_date (PAGE_NAME, HIT_DATE),
    INDEX idx_page_name (PAGE_NAME),
    INDEX idx_hit_date (HIT_DATE),
    INDEX idx_ip_address (IP_ADDRESS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: vrp_users (Users table for authentication)
CREATE TABLE vrp_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'ADMIN',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default data for pinfo_mtype
INSERT INTO pinfo_mtype (TYPE_CODE, TYPE_NAME, DESCRIPTION, ICON_CLASS, COLOR_CLASS, ENABLED, CREATED_DATE) VALUES
('INFO', 'Information', 'General information messages', 'bi-info-circle', 'text-info', TRUE, CURDATE()),
('WARNING', 'Warning', 'Warning messages requiring attention', 'bi-exclamation-triangle', 'text-warning', TRUE, CURDATE()),
('ERROR', 'Error', 'Error messages indicating problems', 'bi-x-circle', 'text-danger', TRUE, CURDATE()),
('SUCCESS', 'Success', 'Success messages for completed actions', 'bi-check-circle', 'text-success', TRUE, CURDATE()),
('NOTICE', 'Notice', 'Important notices and announcements', 'bi-bell', 'text-primary', TRUE, CURDATE());

-- Insert default portal configuration
INSERT INTO vrp_portal_info (INFO_KEY, INFO_VALUE, INFO_TYPE, CATEGORY, DESCRIPTION, ENABLED, CREATED_DATE, MODIFIED_DATE) VALUES
('PORTAL_TITLE', 'Visakh Refinery Portal', 'STRING', 'GENERAL', 'Main title of the portal', TRUE, CURDATE(), CURDATE()),
('PORTAL_SUBTITLE', 'Welcome to the Refinery Management System', 'STRING', 'GENERAL', 'Subtitle displayed on the homepage', TRUE, CURDATE(), CURDATE()),
('CONTACT_EMAIL', 'admin@visakhrefinery.com', 'EMAIL', 'CONTACT', 'Main contact email address', TRUE, CURDATE(), CURDATE()),
('CONTACT_PHONE', '+91-1234567890', 'PHONE', 'CONTACT', 'Main contact phone number', TRUE, CURDATE(), CURDATE()),
('COPYRIGHT_TEXT', '2024 Visakh Refinery Portal. All rights reserved.', 'STRING', 'GENERAL', 'Copyright text for footer', TRUE, CURDATE(), CURDATE()),
('MAINTENANCE_MODE', 'FALSE', 'BOOLEAN', 'SYSTEM', 'Enable/disable maintenance mode', TRUE, CURDATE(), CURDATE()),
('MAX_UPLOAD_SIZE', '10485760', 'NUMBER', 'SYSTEM', 'Maximum file upload size in bytes (10MB)', TRUE, CURDATE(), CURDATE()),
('SESSION_TIMEOUT', '1800', 'NUMBER', 'SYSTEM', 'Session timeout in seconds (30 minutes)', TRUE, CURDATE(), CURDATE());

-- Sample data for vrp_whatsnew
INSERT INTO vrp_whatsnew (TITLE, DESCRIPTION, URL, VALIDFROM, VALIDTO, ENABLED, PRIORITY, CREATED_DATE, MODIFIED_DATE) VALUES
('Portal Launch', 'Welcome to the new Visakh Refinery Portal! This platform will serve as your central hub for all refinery-related information and services.', 'https://www.visakhrefinery.com/portal', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 10, CURDATE(), CURDATE()),
('System Maintenance Schedule', 'Scheduled maintenance will be performed on the first Sunday of every month from 2:00 AM to 6:00 AM. Please plan accordingly.', NULL, CURDATE(), NULL, TRUE, 5, CURDATE(), CURDATE()),
('New Safety Protocols', 'Updated safety protocols are now available in the documents section. All personnel are required to review and acknowledge the new guidelines.', '/documents/safety-protocols', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), TRUE, 8, CURDATE(), CURDATE()),
('Training Session Registration', 'Registration is now open for the quarterly safety training sessions. Please register through the portal by the end of this month.', '/training/register', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), TRUE, 7, CURDATE(), CURDATE()),
('Holiday Schedule Updated', 'The holiday schedule for the upcoming year has been updated. Please check the calendar section for details.', '/calendar/holidays', DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), TRUE, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), CURDATE());

-- Sample data for vrp_events
INSERT INTO vrp_events (TITLE, DESCRIPTION, EVENT_DATE, EVENT_TIME, LOCATION, CATEGORY, ENABLED, CREATED_DATE, MODIFIED_DATE) VALUES
('Safety Training Session', 'Quarterly safety training for all personnel', DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 'Main Conference Hall', 'TRAINING', TRUE, CURDATE(), CURDATE()),
('Annual Inspection', 'Government regulatory inspection', DATE_ADD(CURDATE(), INTERVAL 14 DAY), '10:00:00', 'Entire Facility', 'INSPECTION', TRUE, CURDATE(), CURDATE()),
('Team Building Event', 'Annual team building activities', DATE_ADD(CURDATE(), INTERVAL 21 DAY), '14:00:00', 'Recreation Center', 'SOCIAL', TRUE, CURDATE(), CURDATE());

-- Sample data for vrp_scrollmsg
INSERT INTO vrp_scrollmsg (MSG_HEADER, MESSAGE, MESSAGE_HINDI, MSG_HEADER_HINDI, VALIDFROM, VALIDTO, ENABLED, PRIORITY, DISPLAY_ORDER, SPEED, COLOR, BGCOLOR, MSGBY, CREATED_DATE, MODIFIED_DATE, DTSTAMP) VALUES
('Welcome Notice', 'Welcome to Visakh Refinery Portal - Your gateway to efficient refinery management!', NULL, NULL, CURDATE(), NULL, TRUE, 1, 1, 'normal', '#FFFFFF', '#198754', 'System', CURDATE(), CURDATE(), NOW()),
('Emergency Contact Update', 'Important: Please update your emergency contact information in the employee portal.', NULL, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 2, 2, 'slow', '#FFFFFF', '#FFC107', 'System', CURDATE(), CURDATE(), NOW()),
('New Features Available', 'New features have been added to the portal. Check out the What\'s New section for details.', NULL, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), TRUE, 3, 3, 'normal', '#FFFFFF', '#0D6EFD', 'System', CURDATE(), CURDATE(), NOW());

-- Sample data for vrp_imp_msg
INSERT INTO vrp_imp_msg (TITLE, MESSAGE, MSG_TYPE, PRIORITY, SHOW_POPUP, ENABLED, VALIDFROM, VALIDTO, CREATED_DATE, MODIFIED_DATE) VALUES
('System Maintenance Notice', 'The portal will be under maintenance this Sunday from 2:00 AM to 6:00 AM. Some services may be temporarily unavailable.', 'warning', 8, TRUE, TRUE, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), CURDATE(), CURDATE()),
('Security Update', 'A security update has been applied to the portal. Please log out and log back in to ensure all security features are active.', 'info', 5, FALSE, TRUE, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), CURDATE(), CURDATE()),
('Emergency Contact Update Required', 'All employees must update their emergency contact information by the end of this month. This is mandatory for safety compliance.', 'error', 10, TRUE, TRUE, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), CURDATE(), CURDATE());

-- Grant necessary permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON refweb_portal.* TO 'storm'@'localhost' IDENTIFIED BY 'stormpass';
-- FLUSH PRIVILEGES;

-- Show table creation summary
SELECT 
    TABLE_NAME as 'Table',
    TABLE_ROWS as 'Rows',
    CREATE_TIME as 'Created'
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'refweb_portal' 
ORDER BY TABLE_NAME;

-- Set charset and collation
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4; 