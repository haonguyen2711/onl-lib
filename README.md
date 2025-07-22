# 📚🔐 Online Library Encryption System

Hệ thống thư viện số an toàn với mã hóa và watermark cá nhân hóa.

## 🎯 Tính Năng Chính

### 🔐 Bảo Mật Cao Cấp
- **Mã hóa AES-256-GCM**: File PDF gốc luôn được mã hóa
- **RSA-2048**: Bảo vệ AES key bằng public/private key
- **Zero-knowledge**: Admin không thể xem nội dung PDF mà không có quyền

### 💧 Watermark Cá Nhân Hóa
- Mỗi trang có watermark riêng: `email | tên | thời gian`
- Ngăn chặn chia sẻ trái phép
- Watermark trong suốt, không ảnh hưởng trải nghiệm đọc

### 👥 Phân Quyền 3 Cấp
- **ADMIN**: Upload sách, quản lý người dùng
- **VIP**: Tải file PDF gốc (không watermark)  
- **STANDARD**: Chỉ xem ảnh có watermark

### 📊 Theo Dõi & Thống Kê
- Log tất cả hoạt động download/view
- Thống kê truy cập theo sách
- IP tracking và User-Agent logging

## 🚀 Cài Đặt & Chạy

### 1. Yêu Cầu Hệ Thống
- Java 21+
- Maven 3.8+
- SQL Server 2019+
- Ram: 2GB+
- Disk: 10GB+ (cho storage)

### 2. Cấu Hình Database
```sql
-- Tạo database
CREATE DATABASE OnlineLibrary;

-- Tạo user (tùy chọn)
CREATE LOGIN onllib_user WITH PASSWORD = 'YourPassword123';
USE OnlineLibrary;
CREATE USER onllib_user FOR LOGIN onllib_user;
ALTER ROLE db_owner ADD MEMBER onllib_user;
```

### 3. Cấu Hình Application
Sửa file `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true
    username: sa  # hoặc onllib_user
    password: YourPassword123
```

### 4. Build & Run
```bash
# Build project
mvn clean package

# Run application
mvn spring-boot:run

# Hoặc chạy JAR file
java -jar target/onllib-1.0-SNAPSHOT.jar
```

### 5. Tạo Admin User Đầu Tiên
```bash
# Sau khi app chạy, tables sẽ được tạo tự động
# Vào SQL Server và tạo admin user:
```

```sql
USE OnlineLibrary;

-- Hash password 'admin123' với BCrypt
INSERT INTO users (username, email, password, full_name, role, is_active, created_at, updated_at)
VALUES (
    'admin', 
    'admin@library.com', 
    '$2a$12$LQv3c1yqBw2/9YjRKOZd5u5aGZzl4TtGrp8rKoZOBGPr.6x9aKKGG', -- admin123
    'System Administrator',
    'ADMIN', 
    1, 
    GETDATE(), 
    GETDATE()
);
```

## 📡 API Documentation

### Authentication

#### Đăng Ký
```bash
POST /api/auth/register
Content-Type: application/json

{
    "username": "user1",
    "email": "user1@example.com", 
    "password": "password123",
    "fullName": "Nguyen Van A"
}
```

#### Đăng Nhập
```bash
POST /api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

# Response:
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "username": "admin",
        "email": "admin@library.com",
        "role": "ADMIN",
        "isVip": false,
        "isAdmin": true
    }
}
```

### Books Management

#### Upload Sách (Admin Only)
```bash
POST /api/books/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
- file: [PDF file]
- title: "Tên sách"
- author: "Tác giả" 
- description: "Mô tả sách"
```

#### Danh Sách Sách
```bash
GET /api/books/list?page=0&size=10
Authorization: Bearer <token>
```

#### Xem Trang Sách (có watermark)
```bash
GET /api/books/{bookId}/page/{pageNumber}
Authorization: Bearer <token>

# Response: JPEG image data
```

#### Tải PDF Gốc (VIP Only)
```bash
GET /api/books/{bookId}/download  
Authorization: Bearer <token>

# Response: PDF file data
```

#### Tìm Kiếm Sách
```bash
GET /api/books/search?keyword=java&page=0&size=10
Authorization: Bearer <token>
```

### User Management

#### Cập nhật Role (Admin Only)
```bash
POST /api/users/manage/{userId}/role
Authorization: Bearer <token>
Content-Type: application/json

{
    "role": "VIP",
    "vipExpiresAt": "2024-12-31T23:59:59"
}
```

#### Lấy Danh Sách Users (Admin Only)
```bash
GET /api/users/manage/all
Authorization: Bearer <token>
```

#### Đổi Mật Khẩu
```bash
POST /api/users/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
    "oldPassword": "old123", 
    "newPassword": "new123"
}
```

## 🗂️ Cấu Trúc File Storage

```
storage/
├── books/
│   ├── book1.pdf.enc          # PDF mã hóa bằng AES
│   ├── book1.key.enc          # AES key mã hóa bằng RSA  
│   ├── book1_meta.json        # Metadata (IV, AuthTag...)
│   └── book1_images/          # Ảnh từng trang có watermark
│       ├── page_001.jpg
│       ├── page_002.jpg
│       └── ...
├── temp/                      # Thư mục tạm
└── keys/                      # RSA keys
    ├── public.pem
    └── private.pem
```

## 🔧 Cấu Hình Nâng Cao

### Watermark Customization
```yaml
watermark:
  font-size: 12
  opacity: 0.5
  color: "rgba(128,128,128,0.7)"
  position: "bottom-left"
```

### Storage Configuration  
```yaml
storage:
  base-path: "storage"
  books-path: "storage/books"
  keys-path: "keys"
  temp-path: "storage/temp"
```

### JWT Configuration
```yaml
jwt:
  secret: "YourSecretKey256Bits"
  expiration: 86400000 # 24 hours
```

## 🛡️ Bảo Mật

### Encryption Flow
1. **Upload**: PDF → AES encrypt → Save .pdf.enc
2. **Key Protection**: AES key → RSA encrypt → Save .key.enc  
3. **Metadata**: IV, AuthTag → Save meta.json
4. **Image Generation**: PDF → PNG → Add watermark → Save JPG

### Download Flow (VIP)
1. **Authentication**: Verify JWT + VIP role
2. **Decrypt Key**: RSA private key → AES key
3. **Decrypt PDF**: AES key + IV → Original PDF
4. **Stream**: Send PDF directly (no disk storage)
5. **Logging**: Record download activity

## 🧪 Testing

### Test với Postman
1. Import collection từ `docs/postman/`
2. Set environment variable `{{baseUrl}}` = `http://localhost:8080/api`
3. Login để lấy token
4. Set token vào Authorization header

### Test với cURL
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Upload book (replace TOKEN)
curl -X POST http://localhost:8080/api/books/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "file=@test.pdf" \
  -F "title=Test Book" \
  -F "author=Test Author"
```

## 📈 Performance & Scaling

### Optimization Tips
- Enable database connection pooling
- Use Redis for session/cache
- Configure file storage on SSD
- Monitor memory usage for large PDFs

### Production Deployment
```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 20
      connection-timeout: 30000

logging:
  level:
    com.cmc: INFO
  file:
    name: /var/log/onllib/app.log
```

## 🤝 Contributing

1. Fork repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push branch: `git push origin feature/new-feature`
5. Submit Pull Request

## 📄 License

MIT License - see LICENSE file for details.

## 📞 Support

- 📧 Email: support@library.com
- 📱 Issues: GitHub Issues
- 📖 Wiki: Project Wiki

---

**🚀 Happy Coding! 📚🔐**
