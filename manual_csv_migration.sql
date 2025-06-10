-- Manual CSV Data Migration for Message Board
-- Based on actual CSV data analysis from vrp_scrollmsg.csv
-- CSV Structure: MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI

-- Clear existing data (optional - uncomment if needed)
-- DELETE FROM vrp_scrollmsg;

-- Record 1: NEC Award 2016
-- CSV: 397,"MR won the NEC Award 2016 - Second Prize for Energy Conservation from Government of India, Ministry of Power.",12/29/16,1,31947280,33:09.0,NEC Award - 2016,12/15/16,Y,,
INSERT INTO vrp_scrollmsg (
    MSG_HEADER, 
    MESSAGE, 
    VALIDFROM, 
    VALIDTO, 
    ENABLED, 
    PRIORITY, 
    MSGBY, 
    CREATED_DATE, 
    MODIFIED_DATE, 
    DTSTAMP,
    MESSAGE_HINDI,
    MSG_HEADER_HINDI,
    DISPLAY_ORDER,
    SPEED,
    COLOR,
    BGCOLOR
) VALUES (
    'NEC Award - 2016',
    'MR won the NEC Award 2016 - Second Prize for Energy Conservation from Government of India, Ministry of Power.',
    '2016-12-15',  -- VALIDFROM: 12/15/16
    '2016-12-29',  -- VALIDUPTO: 12/29/16  
    TRUE,          -- ENABLED: Y -> TRUE
    1,             -- PRIORITY: 1
    '31947280',    -- MSGBY: 31947280
    '2016-12-15',  -- CREATED_DATE
    NOW(),         -- MODIFIED_DATE
    '2016-12-15 00:33:09',  -- DTSTAMP: 33:09.0
    NULL,          -- MESSAGE_HINDI: empty
    NULL,          -- MSG_HEADER_HINDI: empty
    1,             -- DISPLAY_ORDER: 1
    'normal',      -- SPEED: default
    '#000000',     -- COLOR: default black
    '#FFFFFF'      -- BGCOLOR: default white
);

-- Record 2: PME Link Maintenance
-- CSV: 400,The link for viewing PME reports is under maintenance. Employees will be informed once the link is activated. ,12/29/16,1,31947280,16:57.0,PME Link,12/22/16,N,,
INSERT INTO vrp_scrollmsg (
    MSG_HEADER, 
    MESSAGE, 
    VALIDFROM, 
    VALIDTO, 
    ENABLED, 
    PRIORITY, 
    MSGBY, 
    CREATED_DATE, 
    MODIFIED_DATE, 
    DTSTAMP,
    MESSAGE_HINDI,
    MSG_HEADER_HINDI,
    DISPLAY_ORDER,
    SPEED,
    COLOR,
    BGCOLOR
) VALUES (
    'PME Link',
    'The link for viewing PME reports is under maintenance. Employees will be informed once the link is activated.',
    '2016-12-22',  -- VALIDFROM: 12/22/16
    '2016-12-29',  -- VALIDUPTO: 12/29/16
    FALSE,         -- ENABLED: N -> FALSE
    1,             -- PRIORITY: 1
    '31947280',    -- MSGBY: 31947280
    '2016-12-22',  -- CREATED_DATE
    NOW(),         -- MODIFIED_DATE
    '2016-12-22 00:16:57',  -- DTSTAMP: 16:57.0
    NULL,          -- MESSAGE_HINDI: empty
    NULL,          -- MSG_HEADER_HINDI: empty
    2,             -- DISPLAY_ORDER: 2
    'normal',      -- SPEED: default
    '#000000',     -- COLOR: default black
    '#FFFFFF'      -- BGCOLOR: default white
);

-- Record 3: PME Reports Available
-- CSV: 401,The link for viewing PME reports is now available on MR Portal under Reports,1/15/17,1,31947280,58:22.0,PME Reports link,12/24/16,Y,,
INSERT INTO vrp_scrollmsg (
    MSG_HEADER, 
    MESSAGE, 
    VALIDFROM, 
    VALIDTO, 
    ENABLED, 
    PRIORITY, 
    MSGBY, 
    CREATED_DATE, 
    MODIFIED_DATE, 
    DTSTAMP,
    MESSAGE_HINDI,
    MSG_HEADER_HINDI,
    DISPLAY_ORDER,
    SPEED,
    COLOR,
    BGCOLOR
) VALUES (
    'PME Reports link',
    'The link for viewing PME reports is now available on MR Portal under Reports',
    '2016-12-24',  -- VALIDFROM: 12/24/16
    '2017-01-15',  -- VALIDUPTO: 1/15/17
    TRUE,          -- ENABLED: Y -> TRUE
    1,             -- PRIORITY: 1
    '31947280',    -- MSGBY: 31947280
    '2016-12-24',  -- CREATED_DATE
    NOW(),         -- MODIFIED_DATE
    '2016-12-24 00:58:22',  -- DTSTAMP: 58:22.0
    NULL,          -- MESSAGE_HINDI: empty
    NULL,          -- MSG_HEADER_HINDI: empty
    3,             -- DISPLAY_ORDER: 3
    'normal',      -- SPEED: default
    '#000000',     -- COLOR: default black
    '#FFFFFF'      -- BGCOLOR: default white
);

-- Record 4: Maximo Maintenance (from line 5 of CSV if exists)
-- CSV: 409,Maximo Application will not be available due to maintenance related activities.,1/26/17,1,31969310,15:06.0,Maximo Maintenance,1/25/17,N,,
INSERT INTO vrp_scrollmsg (
    MSG_HEADER, 
    MESSAGE, 
    VALIDFROM, 
    VALIDTO, 
    ENABLED, 
    PRIORITY, 
    MSGBY, 
    CREATED_DATE, 
    MODIFIED_DATE, 
    DTSTAMP,
    MESSAGE_HINDI,
    MSG_HEADER_HINDI,
    DISPLAY_ORDER,
    SPEED,
    COLOR,
    BGCOLOR
) VALUES (
    'Maximo Maintenance',
    'Maximo Application will not be available due to maintenance related activities.',
    '2017-01-25',  -- VALIDFROM: 1/25/17
    '2017-01-26',  -- VALIDUPTO: 1/26/17
    FALSE,         -- ENABLED: N -> FALSE
    1,             -- PRIORITY: 1
    '31969310',    -- MSGBY: 31969310
    '2017-01-25',  -- CREATED_DATE
    NOW(),         -- MODIFIED_DATE
    '2017-01-25 00:15:06',  -- DTSTAMP: 15:06.0
    NULL,          -- MESSAGE_HINDI: empty
    NULL,          -- MSG_HEADER_HINDI: empty
    4,             -- DISPLAY_ORDER: 4
    'normal',      -- SPEED: default
    '#000000',     -- COLOR: default black
    '#FFFFFF'      -- BGCOLOR: default white
);

-- Verify the migration
SELECT 
    ID,
    MSG_HEADER as Header,
    LEFT(MESSAGE, 50) as MessagePreview,
    VALIDFROM as ValidFrom,
    VALIDTO as ValidTo,
    ENABLED as Enabled,
    PRIORITY as Priority,
    MSGBY as CreatedBy,
    CREATED_DATE as CreatedDate
FROM vrp_scrollmsg 
ORDER BY CREATED_DATE DESC;

-- Summary of migration
SELECT 
    COUNT(*) as TotalRecords,
    SUM(CASE WHEN ENABLED = TRUE THEN 1 ELSE 0 END) as ActiveRecords,
    SUM(CASE WHEN ENABLED = FALSE THEN 1 ELSE 0 END) as InactiveRecords
FROM vrp_scrollmsg; 