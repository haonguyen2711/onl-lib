-- =====================================================
-- Insert Initial Admin User
-- =====================================================
-- Run this AFTER the application has started and created tables

USE OnlineLibrary;
GO

-- Insert admin user
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (username, email, password, full_name, role, is_active, created_at, updated_at)
VALUES (
    'admin', 
    'admin@library.com', 
    '$2a$12$LQv3c1yqBw2/9YjRKOZd5u5aGZzl4TtGrp8rKoZOBGPr.6x9aKKGG',
    'System Administrator',
    'ADMIN', 
    1, 
    GETDATE(), 
    GETDATE()
);

-- Insert some sample VIP user
-- Password: user123
INSERT INTO users (username, email, password, full_name, role, is_active, created_at, updated_at, vip_expires_at)
VALUES (
    'vipuser', 
    'vip@library.com', 
    '$2a$12$8gPQzL3lF4Q5P4TcK5J0YOr1G3xKq7h9s2m6U9n4c8s7e2x1w0q3p',
    'VIP User Example',
    'VIP', 
    1, 
    GETDATE(), 
    GETDATE(),
    DATEADD(year, 1, GETDATE())  -- VIP for 1 year
);

-- Insert sample standard user  
-- Password: user123
INSERT INTO users (username, email, password, full_name, role, is_active, created_at, updated_at)
VALUES (
    'standarduser', 
    'user@library.com', 
    '$2a$12$8gPQzL3lF4Q5P4TcK5J0YOr1G3xKq7h9s2m6U9n4c8s7e2x1w0q3p',
    'Standard User Example',
    'STANDARD', 
    1, 
    GETDATE(), 
    GETDATE()
);

PRINT 'Sample users inserted successfully!';
PRINT '';
PRINT 'Login credentials:';
PRINT 'Admin: admin / admin123';
PRINT 'VIP User: vipuser / user123';  
PRINT 'Standard User: standarduser / user123';
PRINT '';
PRINT 'Use these accounts to test the system.';
GO
