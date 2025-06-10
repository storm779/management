-- Database setup script for Visakh Refinery Portal

-- Create database
CREATE DATABASE IF NOT EXISTS refweb_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user for the application
CREATE USER IF NOT EXISTS 'portal_user'@'localhost' IDENTIFIED BY 'portal_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON refweb_portal.* TO 'portal_user'@'localhost';

-- Also grant to the system user for easier management
GRANT ALL PRIVILEGES ON refweb_portal.* TO 'storm'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Show databases to confirm
SHOW DATABASES;

-- Show users
SELECT User, Host FROM mysql.user WHERE User IN ('portal_user', 'storm');

-- Use the database
USE refweb_portal;

-- Show tables (will be empty initially)
SHOW TABLES; 