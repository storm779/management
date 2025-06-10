-- Fix Announcements Script
-- This script will restore current announcements and update historical data

-- Step 1: Add current announcements back (these were lost during migration)
INSERT INTO vrp_whatsnew (TITLE, DESCRIPTION, URL, VALIDFROM, VALIDTO, PRIORITY, ENABLED, CREATED_DATE, MODIFIED_DATE) VALUES

-- Portal Launch (High Priority)
('Portal Launch', 
 'Welcome to the new Visakh Refinery Portal! This platform will serve as your central hub for all refinery-related information and services.', 
 'https://www.visakhrefinery.com/portal', 
 CURDATE(), 
 DATE_ADD(CURDATE(), INTERVAL 30 DAY), 
 10, 
 TRUE, 
 CURDATE(), 
 CURDATE()),

-- System Maintenance Schedule (Medium Priority, No end date)
('System Maintenance Schedule', 
 'Scheduled maintenance will be performed on the first Sunday of every month from 2:00 AM to 6:00 AM. Please plan accordingly.', 
 NULL, 
 CURDATE(), 
 NULL, 
 5, 
 TRUE, 
 CURDATE(), 
 CURDATE()),

-- New Safety Protocols (High Priority)
('New Safety Protocols', 
 'Updated safety protocols are now available in the documents section. All personnel are required to review and acknowledge the new guidelines.', 
 '/documents/safety-protocols', 
 CURDATE(), 
 DATE_ADD(CURDATE(), INTERVAL 60 DAY), 
 8, 
 TRUE, 
 CURDATE(), 
 CURDATE()),

-- Holiday Schedule Updated (Low Priority)
('Holiday Schedule Updated', 
 'The holiday schedule for the upcoming year has been updated. Please check the calendar section for details.', 
 '/calendar/holidays', 
 DATE_SUB(CURDATE(), INTERVAL 5 DAY), 
 DATE_ADD(CURDATE(), INTERVAL 25 DAY), 
 3, 
 TRUE, 
 DATE_SUB(CURDATE(), INTERVAL 5 DAY), 
 CURDATE()),

-- Training Session Registration (Medium Priority)
('Training Session Registration', 
 'Registration is now open for the quarterly safety training sessions. Please register through the HR portal.', 
 'https://hr.visakhrefinery.com/training', 
 CURDATE(), 
 DATE_ADD(CURDATE(), INTERVAL 15 DAY), 
 5, 
 TRUE, 
 CURDATE(), 
 CURDATE());

-- Step 2: Update historical 2012 data to be valid for reference (but lower priority)
-- Make them valid but with lower priority so current announcements show first

UPDATE vrp_whatsnew 
SET VALIDFROM = DATE_SUB(CURDATE(), INTERVAL 7 DAY),
    VALIDTO = DATE_ADD(CURDATE(), INTERVAL 30 DAY),
    PRIORITY = 1,  -- Lower priority for historical data
    MODIFIED_DATE = CURDATE()
WHERE CREATED_DATE < '2020-01-01';  -- This catches all the 2012 data

-- Step 3: Verification queries
SELECT 'Current Active Announcements' as 'Query Type';
SELECT ID, TITLE, VALIDFROM, VALIDTO, PRIORITY, ENABLED 
FROM vrp_whatsnew 
WHERE ENABLED = 1 
  AND VALIDFROM <= CURDATE() 
  AND (VALIDTO IS NULL OR VALIDTO >= CURDATE())
ORDER BY PRIORITY DESC, VALIDFROM DESC;

SELECT '' as '';
SELECT 'Summary' as 'Query Type';
SELECT 
    COUNT(*) as 'Total Records',
    COUNT(CASE WHEN ENABLED = 1 THEN 1 END) as 'Enabled Records',
    COUNT(CASE WHEN ENABLED = 1 AND VALIDFROM <= CURDATE() AND (VALIDTO IS NULL OR VALIDTO >= CURDATE()) THEN 1 END) as 'Currently Active'
FROM vrp_whatsnew; 