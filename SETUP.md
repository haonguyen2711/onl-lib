# 🛠️ Setup Guide - Online Library Encryption System

## 📋 Yêu Cầu Hệ Thống

### Phần Mềm Cần Thiết
- **Java 21+** - [Download Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Download Maven](https://maven.apache.org/download.cgi)
- **SQL Server 2019+** - [Download SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)
- **Git** (tùy chọn) - [Download Git](https://git-scm.com/downloads)

### Tài Nguyên Hệ Thống
- **RAM**: 4GB+ (khuyến nghị 8GB)
- **Disk**: 10GB+ trống
- **CPU**: 2 cores+
- **Network**: Internet (để download dependencies)

## ⚙️ Cài Đặt Chi Tiết

### 1. Cài Đặt Java 21

1. Tải Oracle JDK 21 từ trang chính thức
2. Chạy file installer `.exe`
3. Thêm Java vào PATH (nếu chưa có):
   ```
   JAVA_HOME=C:\Program Files\Java\jdk-21
   PATH=%PATH%;%JAVA_HOME%\bin
   ```
4. Kiểm tra cài đặt:
   ```bash
   java -version
   javac -version
   ```

### 2. Cài Đặt Maven

1. Tải Maven Binary zip từ trang chính thức
2. Giải nén vào `C:\apache-maven-3.9.x`
3. Thêm Maven vào PATH:
   ```
   MAVEN_HOME=C:\apache-maven-3.9.x
   PATH=%PATH%;%MAVEN_HOME%\bin
   ```
4. Kiểm tra cài đặt:
   ```bash
   mvn -version
   ```

### 3. Cài Đặt SQL Server

#### Tùy Chọn 1: SQL Server Express (Miễn Phí)
1. Tải SQL Server Express
2. Chạy installer và chọn cấu hình cơ bản
3. Ghi nhớ instance name (thường là `SQLEXPRESS`)
4. Tải SQL Server Management Studio (SSMS)

#### Tùy Chọn 2: SQL Server Developer (Miễn Phí)
1. Tải SQL Server Developer Edition
2. Cài đặt với cấu hình mặc định
3. Cài đặt SSMS để quản lý database

### 4. Cấu Hình SQL Server

1. Mở SQL Server Configuration Manager
2. Enable TCP/IP protocol:
   - SQL Server Network Configuration > Protocols for [Instance]
   - Right-click TCP/IP > Enable
3. Restart SQL Server service
4. Tạo SQL Authentication login (nếu cần):
   ```sql
   CREATE LOGIN sa WITH PASSWORD = 'YourPassword123';
   ALTER LOGIN sa ENABLE;
   ```

## 🚀 Chạy Ứng Dụng

### Cách 1: Sử dụng Script Tự Động
```bash
# Chạy file start.bat
start.bat
```

### Cách 2: Chạy Thủ Công

1. **Tạo Database**:
   ```bash
   # Mở SSMS và chạy:
   sqlcmd -S localhost -i sql\setup_database.sql
   ```

2. **Cấu hình Connection String**:
   Sửa `src\main\resources\application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true
       username: sa
       password: YourPassword123
   ```

3. **Build & Run**:
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```

4. **Tạo Admin User**:
   ```bash
   # Sau khi app chạy, chạy script:
   sqlcmd -S localhost -d OnlineLibrary -i sql\insert_admin.sql
   ```

## 🧪 Test Hệ Thống

### 1. Kiểm Tra Web Interface
- Mở browser: `http://localhost:8080`
- Bạn sẽ thấy trang chào mừng

### 2. Test API với cURL

#### Login Admin:
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

#### Lấy danh sách sách:
```bash
curl -X GET http://localhost:8080/api/books/list ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Test Upload PDF

#### Sử dụng Postman:
1. POST `http://localhost:8080/api/books/upload`
2. Headers: `Authorization: Bearer YOUR_TOKEN`
3. Body (form-data):
   - `file`: [Select PDF file]
   - `title`: "Test Book"
   - `author`: "Test Author"
   - `description`: "Test Description"

## 🐛 Xử Lý Lỗi Thường Gặp

### Lỗi: "mvn command not found"
- **Nguyên nhân**: Maven chưa được cài đặt hoặc chưa có trong PATH
- **Giải pháp**: Cài đặt Maven và thêm vào PATH

### Lỗi: "Cannot connect to SQL Server"
- **Nguyên nhân**: SQL Server không chạy hoặc cấu hình connection string sai
- **Giải pháp**: 
  1. Kiểm tra SQL Server service đang chạy
  2. Kiểm tra connection string trong `application.yml`
  3. Kiểm tra firewall/network

### Lỗi: "Access denied for user"
- **Nguyên nhân**: Sai username/password hoặc user chưa có quyền
- **Giải pháp**:
  1. Kiểm tra username/password trong `application.yml`
  2. Tạo database user với quyền phù hợp

### Lỗi: "Port 8080 already in use"
- **Nguyên nhân**: Port 8080 đang được sử dụng bởi ứng dụng khác
- **Giải pháp**: 
  1. Thay đổi port trong `application.yml`: `server.port: 8081`
  2. Hoặc kill process đang sử dụng port 8080

### Lỗi: "OutOfMemoryError"
- **Nguyên nhân**: Không đủ heap memory cho JVM
- **Giải pháp**: Tăng memory cho JVM:
  ```bash
  export MAVEN_OPTS="-Xmx2g -Xms1g"
  mvn spring-boot:run
  ```

## 📁 Cấu Trúc Thư Mục

```
d:\onl-lib\
├── src\
│   ├── main\
│   │   ├── java\com\cmc\
│   │   │   ├── entity\           # Database entities
│   │   │   ├── repository\       # Data access layer
│   │   │   ├── service\          # Business logic
│   │   │   ├── controller\       # REST endpoints
│   │   │   ├── config\           # Configuration
│   │   │   └── dto\              # Data transfer objects
│   │   └── resources\
│   │       ├── application.yml   # App configuration
│   │       └── static\           # Static web files
│   └── test\                     # Unit tests
├── storage\                      # File storage (auto-created)
│   ├── books\                    # Encrypted PDFs & images
│   └── temp\                     # Temporary files
├── keys\                         # RSA keys (auto-created)
├── sql\                          # Database scripts
├── logs\                         # Application logs (auto-created)
├── pom.xml                       # Maven configuration
├── README.md                     # Documentation
└── start.bat                     # Quick start script
```

## 🔧 Cấu Hình Nâng Cao

### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### File Upload Size
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```

### Logging Configuration
```yaml
logging:
  level:
    com.cmc: DEBUG
    org.springframework.security: INFO
  file:
    name: logs/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra file `logs/application.log`
2. Đọc phần troubleshooting trong README.md
3. Tạo issue trên GitHub với thông tin chi tiết về lỗi

---

**🎯 Chúc bạn setup thành công! 🚀**
