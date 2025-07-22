-- =====================================================
-- Online Library Encryption System - Database Setup
-- =====================================================

-- Tạo database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'OnlineLibrary')
BEGIN
    CREATE DATABASE OnlineLibrary
    COLLATE SQL_Latin1_General_CP1_CI_AS;
END
GO

USE OnlineLibrary;
GO

-- Tạo user cho application (tùy chọn)
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'onllib_user')
BEGIN
    CREATE LOGIN onllib_user WITH PASSWORD = 'OnlLib123!@#';
END
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'onllib_user')
BEGIN
    CREATE USER onllib_user FOR LOGIN onllib_user;
    ALTER ROLE db_owner ADD MEMBER onllib_user;
END
GO

-- Tạo directories cho file storage (Windows)
-- Chạy trong Command Prompt as Administrator:
-- mkdir C:\OnlineLibrary\storage\books
-- mkdir C:\OnlineLibrary\storage\temp  
-- mkdir C:\OnlineLibrary\keys

PRINT 'Database setup completed successfully!';
PRINT 'Next steps:';
PRINT '1. Update application.yml with database connection';
PRINT '2. Run the Spring Boot application';
PRINT '3. Tables will be created automatically by Hibernate';
PRINT '4. Insert initial admin user using insert_admin.sql';
GO
