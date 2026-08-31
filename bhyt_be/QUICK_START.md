# 🚀 BHYT Backend - Quick Start Guide

## Getting Started in 5 Minutes

### 1. Prerequisites Check
```bash
# Check Java version (should be 17+)
java -version

# Check Maven (or will use mvnw wrapper)
mvn -version
```

### 2. Build the Project
```bash
cd C:\Users\toanp\OneDrive\Desktop\bhyt_application_be\bhyt_be

# Compile
.\mvnw.cmd clean compile

# Or run directly with Maven
mvn clean compile
```

### 3. Run the Application
```bash
# Option 1: Using Maven Wrapper
.\mvnw.cmd spring-boot:run

# Option 2: Using Maven
mvn spring-boot:run

# Option 3: Build JAR and run
.\mvnw.cmd clean package -DskipTests
java -jar target/bhyt-0.0.1-SNAPSHOT.jar
```

### 4. Test API Endpoints
Once server starts on http://localhost:8080:

#### Create a Customer
```bash
curl -X POST http://localhost:8080/api/khach-hang \
  -H "Content-Type: application/json" \
  -d '{
    "cccd": "123456789",
    "hoVaTen": "Nguyễn Văn A",
    "ngaySinh": "01/01/1990",
    "diaChi": "Hà Nội",
    "soDienThoai": "0987654321"
  }'
```

#### List Customers
```bash
curl http://localhost:8080/api/khach-hang
```

#### Create BHYT
```bash
curl -X POST http://localhost:8080/api/bhyt \
  -H "Content-Type: application/json" \
  -d '{
    "idKhachHang": 1,
    "ngayMua": "01/08/2026",
    "soThangMua": 12,
    "loai": "mới"
  }'
```

#### Get Dashboard Statistics
```bash
curl http://localhost:8080/api/dashboard/statistics
```

---

## 📁 Project Structure

```
bhyt_be/
├── src/main/java/com/application/bhyt/
│   ├── controller/        # REST API Endpoints
│   ├── service/          # Business Logic
│   ├── repository/       # Data Access
│   ├── entity/           # JPA Entities
│   ├── dto/
│   │   ├── request/      # Request DTOs
│   │   └── response/     # Response DTOs
│   ├── enums/            # Enumerations
│   ├── exception/        # Error Handling
│   ├── util/             # Utilities
│   └── BhytApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── logback-spring.xml
├── pom.xml
├── database.db           # SQLite (auto-created)
├── API_DOCUMENTATION.md  # Full API docs
└── IMPLEMENTATION_SUMMARY.md  # Project overview
```

---

## 🔑 Key API Endpoints

### Dashboard
```
GET /api/dashboard/statistics
GET /api/dashboard/revenue?fromDate=01/01/2026&toDate=31/08/2026
```

### Customers
```
POST   /api/khach-hang
GET    /api/khach-hang
GET    /api/khach-hang/{id}
GET    /api/khach-hang/{id}/detail
GET    /api/khach-hang/search?keyword=nguyễn
PUT    /api/khach-hang/{id}
DELETE /api/khach-hang/{id}
```

### BHYT
```
POST   /api/bhyt
GET    /api/bhyt
GET    /api/bhyt/{id}
GET    /api/bhyt/search?keyword=nguyễn
PUT    /api/bhyt/{id}
DELETE /api/bhyt/{id}
POST   /api/bhyt/receive-commission (with body: {"ids": [1, 2, 3]})
```

### Households
```
POST   /api/ho-gia-dinh
GET    /api/ho-gia-dinh
GET    /api/ho-gia-dinh/{id}
POST   /api/ho-gia-dinh/{id}/thanh-vien
DELETE /api/ho-gia-dinh/{idHo}/thanh-vien/{idKhachHang}
```

---

## 🛠️ Configuration

### Database Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlite:database.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
```

### Logging
Edit `src/main/resources/logback-spring.xml` to adjust log levels.

### Port
To change port, add to `application.properties`:
```properties
server.port=8081
```

---

## 💡 Common Development Tasks

### Add New Endpoint
1. Create Request/Response DTO in `dto/request/` and `dto/response/`
2. Add business logic in Service (e.g., `KhachHangService`)
3. Add database query in Repository if needed
4. Expose endpoint in Controller (e.g., `KhachHangController`)

### Fix Compilation Error
```bash
.\mvnw.cmd clean compile -DskipTests
```

### Run Tests
```bash
mvn test
```

### Generate JAR
```bash
mvn clean package -DskipTests
```

---

## 🐛 Troubleshooting

### Java Version Error
```
error: release version XX not supported
```
**Solution:** Update pom.xml `<java.version>` to installed Java version (17 recommended)

### Port Already in Use
```
Caused by: java.net.BindException: Address already in use
```
**Solution:** Kill process on port 8080 or change port in `application.properties`

### Database Lock Error
```
Error: database is locked
```
**Solution:** Close other connections or delete `database.db` and restart

### Compilation Failure
```bash
# Clean and rebuild
.\mvnw.cmd clean compile -U

# Or clear Maven cache
rm -r %USERPROFILE%\.m2\repository
```

---

## 📚 Documentation

- **API_DOCUMENTATION.md** - Complete API documentation with examples
- **IMPLEMENTATION_SUMMARY.md** - Project overview and architecture
- **Source code** - Well-commented Java files
- **pom.xml** - Dependency documentation

---

## 🔗 Next Steps

1. **Frontend Integration** - Connect frontend to these APIs
2. **Authentication** - Add Spring Security for user authentication
3. **Additional Features** - Implement pending MSTB formula
4. **Performance** - Add Redis caching for dashboard
5. **Testing** - Create comprehensive unit/integration tests

---

## 📞 Support

For API details, see `API_DOCUMENTATION.md`
For project structure, see `IMPLEMENTATION_SUMMARY.md`

---

**Happy coding! 🎉**
