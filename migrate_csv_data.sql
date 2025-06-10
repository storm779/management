-- Migration script for VRP WhatsNew CSV data
-- This script migrates data from the legacy CSV format to the new database schema

-- First, clear existing data if any (optional - comment out if you want to keep existing data)
-- DELETE FROM vrp_whatsnew;

-- Reset auto-increment counter (optional)
-- ALTER TABLE vrp_whatsnew AUTO_INCREMENT = 1;

-- Insert legacy data with proper date format conversion and field mapping
-- CSV format: MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,URL,VALIDFROM,ENABLED,MESSAGE_HINDI

INSERT INTO vrp_whatsnew (TITLE, URL, VALIDFROM, VALIDTO, PRIORITY, ENABLED, CREATED_DATE, MODIFIED_DATE) VALUES
-- Record 228: Housekeeping - Refinery
('Housekeeping - Refinery', 'hr/Shared%20Documents/Ideas%20for%20housing%20keeping.doc', '2012-09-13', '2012-09-30', 5, TRUE, '2012-09-13', '2012-09-13'),

-- Record 227: Rainywear 2012-13 (Female)
('Rainywear 2012-13 (Female)', 'hr/Shared%20Documents/Rainywear%202012_13.pdf', '2012-09-13', '2012-09-30', 5, TRUE, '2012-09-13', '2012-09-13'),

-- Record 226: PME Overdue - Sep 2012
('PME Overdue - Sep 2012', 'hr/Shared%20Documents/PME%20Overdue%20in%20September%202012.pdf', '2012-09-13', '2012-09-30', 6, TRUE, '2012-09-13', '2012-09-13'),

-- Record 225: PME Due - Sep 2012
('PME Due - Sep 2012', 'hr/Shared%20Documents/PME%20Due%20in%20September%202012.pdf', '2012-09-13', '2012-09-30', 6, TRUE, '2012-09-13', '2012-09-13'),

-- Record 224: ED-MR Hindi Pakhwada Message
('ED-MR Hindi Pakhwada Message', 'document/ED-MR Hindi Pakhwada Message.jpg', '2012-09-13', '2012-09-15', 5, TRUE, '2012-09-13', '2012-09-13'),

-- Record 223: Lecture on Communal Harmony
('Lecture on Communal Harmony', 'hr/Shared%20Documents/Lecture%20on%20Communal%20Harmony.pdf', '2012-08-31', '2012-08-31', 1, TRUE, '2012-08-31', '2012-08-31');

-- Verify the migration
SELECT COUNT(*) as 'Total Records Migrated' FROM vrp_whatsnew;
SELECT * FROM vrp_whatsnew ORDER BY ID;

-- Notes:
-- 1. Date format converted from M/D/YY to YYYY-MM-DD
-- 2. ENABLED converted from Y/N to TRUE/FALSE
-- 3. URLs preserved as-is for legacy compatibility
-- 4. MSGBY and DTSTAMP fields not migrated (no corresponding fields in new schema)
-- 5. MESSAGE_HINDI not migrated (no Hindi support in current schema)
-- 6. Auto-increment ID will be assigned automatically 