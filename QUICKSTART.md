# 🚀 Quick Start Guide

## Cách 1: Chạy với Docker (Khuyến Nghị)

### Yêu Cầu
- Docker Desktop
- 4GB RAM trống

### Bước 1: Clone & Start
```bash
git clone <repository-url>
cd onl-lib
docker-compose up -d
```

### Bước 2: Tạo Admin User
```bash
# Đợi 30 giây để SQL Server khởi động
timeout 30

# Tạo admin user
docker exec -it onllib-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P OnlineLib123! -i /docker-entrypoint-initdb.d/insert_admin.sql
```

### Bước 3: Test
- Web UI: http://localhost
- API: http://localhost/api
- Login: admin / admin123

---

## Cách 2: Chạy Local (Development)

### Yêu Cầu
- Java 21+
- Maven 3.8+
- SQL Server

### Quick Setup
```bash
# 1. Setup database
sqlcmd -S localhost -i sql\setup_database.sql

# 2. Start application
start.bat

# 3. Create admin (mở terminal mới)
sqlcmd -S localhost -d OnlineLibrary -i sql\insert_admin.sql
```

### Test
- Web: http://localhost:8080
- API: http://localhost:8080/api

---

## 🧪 Test API

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Upload Book (Admin)
```bash
curl -X POST http://localhost:8080/api/books/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@sample.pdf" \
  -F "title=Sample Book" \
  -F "author=John Doe"
```

### 3. List Books
```bash
curl -X GET http://localhost:8080/api/books/list \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. View Page (Standard Users)
```bash
curl -X GET http://localhost:8080/api/books/1/page/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output page1.jpg
```

### 5. Download PDF (VIP Only)
```bash
curl -X GET http://localhost:8080/api/books/1/download \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output book.pdf
```

---

## 👥 Test Accounts

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | Full access |
| vipuser | user123 | VIP | Download PDFs |
| standarduser | user123 | STANDARD | View images only |

---

## 🔧 Troubleshooting

### App không start
```bash
# Kiểm tra logs
docker logs onllib-app

# Hoặc local
tail -f logs/application.log
```

### Database connection failed
```bash
# Test SQL Server
docker exec -it onllib-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P OnlineLib123!
```

### Port 8080 đã sử dụng
```bash
# Tìm process
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

---

## 📱 Next Steps

1. **Frontend**: Tích hợp React/Vue.js frontend
2. **Security**: Setup HTTPS với SSL certificates  
3. **Scaling**: Use Redis cho session storage
4. **Monitoring**: Setup logging với ELK stack
5. **Backup**: Tự động backup database và files

---

**🎉 Happy Coding! 📚🔐**
