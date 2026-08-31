# BHYT Application Backend - API Documentation

## 1. Project Overview

This is a comprehensive backend system built with **Spring Boot 4.1.0** and **SQLite** for managing Health Insurance (BHYT) applications in Vietnam.

### Key Features
- Customer management (Khách Hàng)
- Health Insurance BHYT management
- Household management (Hộ Gia Đình)
- Dashboard with statistics
- Audit logging
- Soft delete functionality
- Pagination support

## 2. Technology Stack

- **Framework**: Spring Boot 4.1.0
- **Database**: SQLite
- **ORM**: Hibernate with Spring Data JPA
- **Language**: Java 25
- **Build Tool**: Maven
- **Logging**: Logback with SLF4J
- **Lombok**: For code generation

## 3. Project Structure

```
bhyt_be/
├── src/main/java/com/application/bhyt/
│   ├── controller/          # REST API Controllers
│   ├── service/            # Business Logic Services
│   ├── repository/         # JPA Repositories
│   ├── entity/             # JPA Entities
│   ├── dto/
│   │   ├── request/        # Request DTOs
│   │   └── response/       # Response DTOs
│   ├── enums/              # Enumerations
│   ├── exception/          # Exception Handling
│   ├── util/               # Utility Classes
│   └── BhytApplication.java # Main Application Class
├── src/main/resources/
│   ├── application.properties
│   └── logback-spring.xml
└── pom.xml
```

## 4. Database Schema

### khach_hang (Customer)
- id (Integer, PK)
- cccd (String, Unique)
- ho_va_ten (String, Required)
- ngay_sinh (LocalDate)
- dia_chi (String)
- so_dien_thoai (String)
- lien_lac_khac (String)
- hinh_anh (String)
- ghi_chu (String)
- ngay_tao (LocalDate, Auto)
- ngay_cap_nhat (LocalDate, Auto)
- bhyt_khac (String)
- co_mstb (Integer, Default 0)
- da_xoa (Integer, Default 0)

### bao_hiem_y_te (BHYT)
- id (Integer, PK)
- id_khach_hang (Integer, FK)
- ngay_mua (LocalDate)
- ngay_co_han (LocalDate, Nullable)
- so_thang_mua (Integer)
- han_the (LocalDate, Auto-calculated)
- so_lan_mua_cua_ho (Integer)
- so_tien_thu (Integer, Auto-calculated)
- ngay_tao (LocalDate, Auto)
- ngay_cap_nhat (LocalDate, Auto)
- da_nhan_hoa_hong (Integer, Default 0)
- bhyt_moi_nhat (Integer, Default 1)
- da_xoa (Integer, Default 0)
- loai (String: "mới" or "gia hạn")
- noi_dang_ky (String)

### ho_gia_dinh (Household)
- id (Integer, PK)
- so_thanh_vien (Integer, Auto-updated)
- hinh_anh (String)
- ngay_tao (LocalDate, Auto)
- ngay_cap_nhat (LocalDate, Auto)

### thanh_vien_ho_gia_dinh (Household Member)
- id (Integer, PK)
- id_thanh_vien (Integer, FK to khach_hang)
- id_ho_gia_dinh (Integer, FK to ho_gia_dinh)
- ngay_tao (LocalDate, Auto)
- ngay_ket_thuc (LocalDate, Nullable - when member leaves)

## 5. API Endpoints

### Dashboard
```
GET  /api/dashboard/statistics        # Get dashboard statistics
GET  /api/dashboard/revenue            # Get revenue statistics (fromDate, toDate)
```

### Customer Management (Khách Hàng)
```
POST   /api/khach-hang                 # Create new customer
GET    /api/khach-hang                 # List customers (pagination)
GET    /api/khach-hang/{id}            # Get customer by ID
GET    /api/khach-hang/{id}/detail     # Get customer detail with BHYT history
GET    /api/khach-hang/search          # Search customers (keyword)
PUT    /api/khach-hang/{id}            # Update customer
DELETE /api/khach-hang/{id}            # Soft delete customer
```

### BHYT Management
```
POST   /api/bhyt                       # Create new BHYT
GET    /api/bhyt                       # List BHYT (pagination)
GET    /api/bhyt/{id}                  # Get BHYT by ID
GET    /api/bhyt/search                # Search BHYT (keyword)
PUT    /api/bhyt/{id}                  # Update BHYT
DELETE /api/bhyt/{id}                  # Soft delete BHYT
POST   /api/bhyt/receive-commission    # Receive commission (batch)
```

### Household Management (Hộ Gia Đình)
```
POST   /api/ho-gia-dinh                                    # Create household
GET    /api/ho-gia-dinh                                    # List households
GET    /api/ho-gia-dinh/{id}                               # Get household detail
POST   /api/ho-gia-dinh/{id}/thanh-vien                    # Add member to household
DELETE /api/ho-gia-dinh/{idHoGiaDinh}/thanh-vien/{idKhachHang}  # Remove member from household
```

## 6. Request/Response Examples

### Create Customer
```json
POST /api/khach-hang
{
  "cccd": "123456789",
  "hoVaTen": "Nguyễn Văn A",
  "ngaySinh": "01/01/1990",
  "diaChi": "Hà Nội",
  "soLanMuaCuaHo" 2,
  "soDienThoai": "0987654321",
  "coMstb": 0
}

Response:
{
  "message": "Success",
  "code": "00000",
  "data": {
    "id": 1,
    "cccd": "123456789",
    "hoVaTen": "Nguyễn Văn A",
    ...
  }
}
```

### Create BHYT
```json
POST /api/bhyt
{
  "idKhachHang": 1,
  "ngayMua": "01/08/2026",
  "soThangMua": 12,
  "loai": "mới",
  "noiDangKy": "BHXH Hà Nội"
}

Response:
{
  "message": "Success",
  "code": "00000",
  "data": {
    "id": 1,
    "idKhachHang": 1,
    "ngayMua": "01/08/2026",
    "hanThe": "01/08/2027",
    "soTienThu": 105300,
    ...
  }
}
```

### Get Dashboard Statistics
```json
GET /api/dashboard/statistics

Response:
{
  "message": "Success",
  "code": "00000",
  "data": {
    "bhytSapHetHan30Ngay": 5,
    "tongHoGiaDinh": 10,
    "bhytChuaNhanHoaHong": 3,
    "tongKhachHang": 50,
    "khachHangChuaCoHo": 20
  }
}
```

### Receive Commission (Batch)
```json
POST /api/bhyt/receive-commission
{
  "ids": [1, 2, 3, 4]
}

Response:
{
  "message": "Success",
  "code": "00000",
  "data": null
}
```

## 7. Business Logic

### BHYT Expiration Filter
- **EXPIRED**: han_the < today
- **EXPIRING_7_DAYS**: han_the >= today AND han_the <= today + 7 days
- **EXPIRING_14_DAYS**: han_the >= today AND han_the <= today + 14 days
- **EXPIRING_30_DAYS**: han_the >= today AND han_the <= today + 30 days
- **EXPIRING_60_DAYS**: han_the >= today AND han_the <= today + 60 days
- **UNRECEIVED_COMMISSION**: da_nhan_hoa_hong = 0

### Insurance Amount Calculation (without MSTB)
```
Người thứ nhất  = 4.5% × 2.340.000 × 100% = 105,300
Người thứ hai   = 4.5% × 2.340.000 × 70%  = 73,710
Người thứ ba    = 4.5% × 2.340.000 × 60%  = 63,180
Người thứ tư    = 4.5% × 2.340.000 × 50%  = 52,650
Người thứ năm+  = 4.5% × 2.340.000 × 40%  = 42,120
```

### Insurance Expiration Date (han_the)
- If `ngay_co_han` is present: han_the = ngay_co_han + so_thang_mua
- If `ngay_co_han` is null: han_the = ngay_mua + so_thang_mua

## 8. Pagination

All list endpoints support cursor-based pagination:
- `lastId`: Last ID from previous result (starts from 0)
- `pageSize`: Number of items per page (default: 20)
- `hasMore`: Boolean indicating if more data exists

## 9. Error Handling

All errors follow a standard response format:
```json
{
  "message": "Error description",
  "code": "ERROR_CODE",
  "data": null
}
```

### Common Error Codes
- 00000: SUCCESS
- 40000: INVALID_REQUEST
- 40001: CUSTOMER_NOT_FOUND
- 40002: CUSTOMER_ALREADY_DELETED
- 40003: CCCD_ALREADY_EXISTS
- 40004: BHYT_NOT_FOUND
- 40005: BHYT_ALREADY_DELETED
- 40006: HOUSEHOLD_NOT_FOUND
- 40007: HOUSEHOLD_MEMBER_NOT_FOUND
- 50000: DATABASE_ERROR
- 50001: INTERNAL_SERVER_ERROR

## 10. Running the Application

### Prerequisites
- Java 25
- Maven (or use mvnw wrapper)

### Build
```bash
cd bhyt_be
./mvnw.cmd clean compile
```

### Run
```bash
./mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`

### Database
- SQLite database file: `database.db` (auto-created)
- DDL auto-update: enabled in application.properties

## 11. Features Implemented

✅ Customer Management (CRUD)
✅ BHYT Management (CRUD)
✅ Soft Delete
✅ Pagination with cursor-based approach
✅ Search and Filter
✅ Household Management
✅ Dashboard Statistics
✅ Revenue Statistics
✅ Audit Logging
✅ Exception Handling
✅ Transaction Support
✅ Automatic Calculations (han_the, so_tien_thu)
✅ Current BHYT tracking
✅ BHYT History tracking

## 12. Notes

- All timestamps are automatically managed by JPA PrePersist/PreUpdate methods
- CCCD (Citizen ID) is unique and required
- Soft delete is used throughout (da_xoa = 1 marks as deleted)
- Commission (hoa hồng) tracking is implemented
- Household members can be added/removed with date tracking
- BHYT formula with MSTB is pending specification from business

## 13. Future Enhancements

- Implement authentication/authorization
- Add more detailed audit trail storage
- Implement Redis caching for dashboard statistics
- Add bulk import functionality
- Enhanced search with full-text indexing
- Export to Excel functionality
