@echo off
echo =====================================================
echo   Online Library Encryption System
echo   Starting Application...
echo =====================================================
echo.

REM Kiểm tra Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found! Please install Java 21 or higher.
    pause
    exit /b 1
)

REM Kiểm tra Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found! Please install Maven 3.8 or higher.
    pause
    exit /b 1
)

REM Tạo thư mục storage nếu chưa có
if not exist "storage" mkdir storage
if not exist "storage\books" mkdir storage\books
if not exist "storage\temp" mkdir storage\temp
if not exist "keys" mkdir keys

echo Creating storage directories...
echo ✓ storage/books
echo ✓ storage/temp  
echo ✓ keys
echo.

REM Build và chạy application
echo Building application...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo =====================================================
echo   🚀 Starting Online Library System...
echo   📚 Access at: http://localhost:8080
echo   🔌 API at: http://localhost:8080/api
echo =====================================================
echo.
echo Press Ctrl+C to stop the server
echo.

REM Chạy application
call mvn spring-boot:run

pause
