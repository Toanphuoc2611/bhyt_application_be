# build-custom-app

## Description

Xây dựng hoàn chỉnh backend application/module theo yêu cầu người dùng bằng **Spring Boot + SQLite**, theo tiêu chuẩn của một Senior Backend Developer.

Skill này được thiết kế cho hệ thống quản lý BHYT và các ứng dụng có cấu trúc tương tự.

---

# 1. Vai trò của Agent

Agent phải hoạt động như một **Senior Java/Spring Boot Backend Developer**.

Agent chịu trách nhiệm:

* Phân tích yêu cầu nghiệp vụ.
* Phân tích source code hiện tại.
* Kiểm tra database/entity/repository/service/controller hiện có.
* Thiết kế hoặc điều chỉnh database.
* Xây dựng Entity.
* Xây dựng Repository.
* Xây dựng Service.
* Xây dựng Controller.
* Xây dựng DTO Request/Response.
* Xây dựng validation.
* Xây dựng exception handling.
* Xây dựng logging.
* Xây dựng audit/action log.
* Xây dựng pagination.
* Xây dựng search/filter.
* Viết test khi phù hợp.
* Kiểm tra compile.
* Kiểm tra các lỗi runtime có thể dự đoán được.
* Đảm bảo code có thể maintain và mở rộng.

Agent phải ưu tiên:

1. Đúng nghiệp vụ.
2. Đúng database.
3. Đúng API contract.
4. Tính nhất quán của code.
5. Khả năng maintain.
6. Khả năng mở rộng.
7. An toàn dữ liệu.
8. Không phá vỡ chức năng hiện tại.

---

# 2. NGUYÊN TẮC QUAN TRỌNG NHẤT

## 2.1 Không được tự đoán nghiệp vụ

Nếu gặp một trong các trường hợp:

* Yêu cầu không rõ.
* Có nhiều cách hiểu.
* Không biết cách tính.
* Không biết dữ liệu nào được ưu tiên.
* Không biết trạng thái nào cần cập nhật.
* Không biết khi thêm/sửa/xóa phải cập nhật bảng nào.
* Không biết quan hệ giữa các entity.
* Không biết hành vi khi dữ liệu đã tồn tại.
* Không biết xử lý trường hợp null.
* Không biết xử lý trường hợp trùng dữ liệu.
* Không biết có được phép xóa hay không.
* Không biết nghiệp vụ BHYT.

Agent **PHẢI DỪNG LẠI VÀ HỎI USER**.

Không được tự lựa chọn một phương án rồi tiếp tục triển khai.

Ví dụ:

> Khi thêm BHYT mới, nếu khách hàng đã có BHYT hiện tại thì có được phép thêm không?

Agent phải hỏi user.

Không được tự suy luận rằng:

> "Chắc chắn là update BHYT cũ."

---

# 3. Quy trình bắt buộc

Khi nhận yêu cầu xây dựng một chức năng mới, Agent phải thực hiện theo thứ tự:

## STEP 1 — Phân tích yêu cầu

Xác định:

* Input.
* Output.
* Business rule.
* Database cần sử dụng.
* Entity liên quan.
* API cần tạo.
* Validation.
* Error case.
* Logging.
* Audit logging.

Nếu có điểm không rõ:

**DỪNG VÀ HỎI USER.**

---

# STEP 2 — Kiểm tra source code hiện tại

Trước khi tạo bất kỳ file nào:

* Kiểm tra package hiện tại.
* Kiểm tra Entity.
* Kiểm tra DTO.
* Kiểm tra Repository.
* Kiểm tra Service.
* Kiểm tra Controller.
* Kiểm tra Exception.
* Kiểm tra Utils.
* Kiểm tra Config.
* Kiểm tra logging.
* Kiểm tra dependency.
* Kiểm tra database schema hiện tại.

Không được tạo class mới nếu source đã có class tương ứng.

Nếu class đã tồn tại:

1. Kiểm tra implementation.
2. So sánh với requirement.
3. Modify nếu cần.
4. Chỉ tạo mới nếu thực sự chưa tồn tại.

---

# STEP 3 — Kiểm tra database

Database sử dụng:

**SQLite**

Phải đảm bảo:

* Không tự ý đổi tên column.
* Không tự ý đổi kiểu dữ liệu.
* Không tự ý thêm column nếu requirement chưa yêu cầu.
* Không tự ý xóa column.
* Không tự ý thay đổi relationship.

Nếu cần thay đổi database để đáp ứng nghiệp vụ nhưng requirement chưa nói rõ:

**DỪNG VÀ HỎI USER.**

---

# STEP 4 — Entity

Cấu trúc package:

```text
bhyt_be
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── repository
├── services
└── utils
```

Entity phải:

* Mapping chính xác database.
* Có `@Entity`.
* Có `@Table`.
* Mapping column rõ ràng.
* Không đưa business logic phức tạp vào Entity.
* Relationship phải rõ ràng.
* Tránh `EAGER` nếu không cần thiết.
* Tránh vòng lặp JSON khi serialize relationship.
* Không expose Entity trực tiếp ra Controller.

---

# 5. Database Schema

## 5.1 khach_hang

| Column        | Type      | Rule               |
| ------------- | --------- | ------------------ |
| id            | Integer   | PK, auto increment |
| cccd          | String    | Unique             |
| ho_va_ten     | String    | Not Null           |
| ngay_sinh     | LocalDate |                    |
| dia_chi       | String    |                    |
| so_dien_thoai | String    |                    |
| lien_lac_khac | String    |                    |
| hinh_anh      | String    |                    |
| ghi_chu       | String    |                    |
| ngay_tao      | LocalDate | Auto               |
| ngay_cap_nhat | LocalDate | Auto               |
| bhyt_khac     | String    |                    |
| co_mstb       | Integer   | Default 0          |
| da_xoa        | Integer   | Default 0          |

---

## 5.2 ho_gia_dinh

| Column        | Type      | Rule               |
| ------------- | --------- | ------------------ |
| id            | Integer   | PK, auto increment |
| so_thanh_vien | Integer   | Auto update        |
| hinh_anh      | String    |                    |
| ngay_tao      | LocalDate | Auto               |
| ngay_cap_nhat | LocalDate | Auto               |

---

## 5.3 thanh_vien_ho_gia_dinh

| Column         | Type      | Rule               |
| -------------- | --------- | ------------------ |
| id             | Integer   | PK, auto increment |
| id_thanh_vien  | Integer   | FK khach_hang      |
| id_ho_gia_dinh | Integer   | FK ho_gia_dinh     |
| ngay_tao       | LocalDate | Auto               |
| ngay_ket_thuc  | LocalDate | Default null       |

---

## 5.4 bao_hiem_y_te

| Column            | Type      | Rule                     |
| ----------------- | --------- | ------------------------ |
| id                | Integer   | PK, auto increment       |
| id_khach_hang     | Integer   | FK khach_hang            |
| ngay_mua          | LocalDate | Not Null                 |
| ngay_co_han       | LocalDate |                          |
| so_thang_mua      | Integer   | Not Null                 |
| han_the           | LocalDate | Not Null, auto calculate |
| so_lan_mua_cua_ho | Integer   | Default 1                |
| so_tien_thu       | Integer   | Auto calculate           |
| ngay_tao          | LocalDate | Auto                     |
| ngay_cap_nhat     | LocalDate | Auto                     |
| da_nhan_hoa_hong  | Integer   | Default 0                |
| bhyt_moi_nhat     | Integer   | Default 1                |
| da_xoa            | Integer   | Default 0                |
| loai              | String    | Default "mới"            |
| noi_dang_ky       | String    |                          |

---

# 6. API Response

Tất cả API phải trả về format thống nhất:

```json
{
  "message": "Success",
  "code": "SUCCESS",
  "data": {}
}
```

Tạo:

```text
MyApiResponse
```

Không trả Entity trực tiếp.

---

# 7. Exception Architecture

Tạo:

```text
exception
├── MyException
├── ErrorCode
└── GlobalExceptionHandler
```

## ErrorCode

Mỗi lỗi phải có:

* code
* message

Ví dụ:

```text
SUCCESS
CUSTOMER_NOT_FOUND
BHYT_NOT_FOUND
CUSTOMER_ALREADY_EXISTS
INVALID_REQUEST
INVALID_DATE
HOUSEHOLD_NOT_FOUND
```

Không hard-code message lỗi ở Controller.

---

# 8. Validation

Request DTO phải validate input.

Ví dụ:

```java
@NotBlank
private String hoVaTen;
```

```java
@NotNull
private LocalDate ngayMua;
```

```java
@Min(1)
private Integer soThangMua;
```

Validation error phải được xử lý bởi `GlobalExceptionHandler`.

---

# 9. Date Format

Backend sử dụng:

```java
LocalDate
```

API nhận và trả:

```text
dd/MM/yyyy
```

Ví dụ:

```json
{
  "ngaySinh": "22/08/2026"
}
```

Không sử dụng:

```text
2026-08-22
```

nếu API contract yêu cầu `dd/MM/yyyy`.

Phải cấu hình Jackson global để:

* FE gửi `dd/MM/yyyy` → `LocalDate`
* Backend trả `LocalDate` → `dd/MM/yyyy`

Không xử lý format ngày thủ công ở từng Controller.

---

# 10. DTO Architecture

Không dùng Entity làm Request/Response.

Cấu trúc:

```text
dto
├── request
│   ├── KhachHangRequest
│   ├── BhytRequest
│   └── ...
└── response
    ├── KhachHangResponse
    ├── BhytResponse
    ├── HouseholdResponse
    └── ...
```

Nếu request và response khác nhau phải tạo DTO riêng.

---

# 11. Service Architecture

Business logic phải nằm trong Service.

Controller chỉ chịu trách nhiệm:

* nhận request;
* validate;
* gọi service;
* trả response.

Không viết business logic trong Controller.

Ví dụ:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
SQLite
```

---

# 12. Transaction

Các nghiệp vụ thay đổi nhiều bảng phải sử dụng transaction.

Ví dụ:

```java
@Transactional
public void addBhyt(...) {
    ...
}
```

Nếu một bước fail:

> toàn bộ transaction phải rollback.

Đặc biệt áp dụng cho:

* Thêm khách hàng + hộ.
* Thêm thành viên hộ.
* Thêm BHYT.
* Cập nhật BHYT.
* Xóa dữ liệu liên quan.
* Cập nhật số thành viên hộ.

---

# 13. Soft Delete

Các bảng có:

```text
da_xoa
```

phải ưu tiên soft delete.

Không dùng:

```java
repository.delete(...)
```

trừ khi requirement xác nhận rõ cần hard delete.

Sau khi soft delete:

```text
da_xoa = 1
```

Các query thông thường phải loại bỏ:

```text
da_xoa = 1
```

---

# 14. Logging Architecture

Backend phải có 2 loại log:

## 14.1 System Log

Dùng để ghi:

* Application start.
* Application stop.
* Error.
* Exception.
* Database error.
* Unexpected error.
* Technical information.

Ví dụ:

```text
logs/system.log
```

---

# 15. Action / Audit Log

Dùng để ghi hành động của người dùng.

Ví dụ:

```text
logs/audit.log
```

Các action:

```text
CREATE
UPDATE
DELETE
```

Có thể mở rộng:

```text
LOGIN
EXPORT
IMPORT
VIEW
```

nếu nghiệp vụ yêu cầu.

---

# 16. Audit Log Format

Mỗi action log nên chứa:

```text
timestamp
action
module
entity
entityId
description
```

Ví dụ:

```text
2026-08-22 10:30:15
action=CREATE
module=BHYT
entity=BaoHiemYTe
entityId=15
description=Thêm BHYT cho khách hàng
```

Nếu hệ thống có user/account:

có thể bổ sung:

```text
userId
username
```

Nhưng nếu requirement chưa có user/account thì **không tự thêm authentication/user system**.

---

# 17. Không log dữ liệu nhạy cảm

Không ghi toàn bộ:

* CCCD.
* Ảnh CCCD.
* Password.
* Token.
* Access token.
* Refresh token.

Nếu cần reference:

```text
customerId=15
```

thay vì ghi toàn bộ thông tin khách hàng.

---

# 18. Logging nguyên tắc

Không log kiểu:

```java
System.out.println(...)
```

Phải sử dụng logging framework của Spring Boot.

Ví dụ:

```java
private static final Logger log =
        LoggerFactory.getLogger(BhytService.class);
```

Log level:

```text
ERROR
WARN
INFO
DEBUG
```

Không sử dụng `DEBUG` cho thông tin nghiệp vụ quan trọng cần audit.

---

# 19. BHYT — Chức năng

## LƯU ý
Xem thêm "chuc_nang.md" để tìm hiểu thêm

Backend phải hỗ trợ:

## 19.1 Danh sách BHYT

Cho phép:

* Pagination.
* Search.
* Filter.
* Sort nếu requirement có.

---

## 19.2 Search

Search theo:

```text
hoVaTen
cccd
```

Search phải kết hợp được với filter.

Ví dụ:

```text
/search?keyword=nguyen&filter=EXPIRED
```

---

# 20. Filter BHYT

Các trạng thái:

```text
EXPIRED
EXPIRING_7_DAYS
EXPIRING_14_DAYS
EXPIRING_30_DAYS
EXPIRING_60_DAYS
UNRECEIVED_COMMISSION
```

Phải tạo enum thay vì hard-code string ở nhiều nơi.

Ví dụ:

```java
public enum BhytFilterType {
    EXPIRED,
    EXPIRING_7_DAYS,
    EXPIRING_14_DAYS,
    EXPIRING_30_DAYS,
    EXPIRING_60_DAYS,
    UNRECEIVED_COMMISSION
}
```

Nếu nghiệp vụ giữa:

> gần hết hạn 7 ngày

và:

> còn hạn trong 7 ngày

không rõ cách tính inclusive/exclusive:

**PHẢI HỎI USER.**

Không tự đoán.

---

# 21. Dashboard BHYT

Phải hỗ trợ thống kê:

### Sắp hết hạn 30 ngày

Tính từ ngày hiện tại đến 30 ngày sau.

### Chưa nhận hoa hồng

Đếm:

```text
da_nhan_hoa_hong = 0
```

### Thống kê doanh thu

Cho phép chọn:

```text
fromDate
toDate
```

Trả về:

```text
totalMoney
totalBhyt
totalNew
totalRenew
```

Việc xác định:

> "mới"

và

> "gia hạn"

phải dựa trên field:

```text
loai
```

Nếu có quy tắc khác:

**HỎI USER.**

---

# 22. Thêm BHYT

Khi thêm BHYT:

Agent phải kiểm tra requirement về:

* Customer có tồn tại không.
* Customer đã bị xóa chưa.
* Ngày mua.
* Ngày có hạn.
* Số tháng.
* Hạn thẻ.
* Số tiền.
* Loại BHYT.
* BHYT hiện tại.
* BHYT cũ.
* Số lần mua của hộ.
* Commission.
* `bhyt_moi_nhat`.

Nếu bất kỳ rule nào chưa rõ:

**DỪNG VÀ HỎI USER.**

Không tự đoán.

---

# 23. Cập nhật BHYT

Khi update:

* Kiểm tra BHYT tồn tại.
* Kiểm tra `da_xoa`.
* Validate request.
* Recalculate các field auto-calc nếu nghiệp vụ yêu cầu.
* Update `ngay_cap_nhat`.
* Ghi audit log.

Không tự ý cho phép update các field auto-calc nếu nghiệp vụ chưa xác định.

---

# 24. Xóa BHYT

Mặc định:

```text
soft delete
```

Set:

```text
da_xoa = 1
```

Không hard delete nếu user chưa yêu cầu.

Phải ghi:

```text
action=DELETE
```

vào audit log.

---

# 25. Chi tiết BHYT

Khi click tên khách hàng:

Backend phải trả:

### Customer information

```text
Thông tin khách hàng
```

### Current BHYT

```text
BHYT hiện tại
```

### BHYT history

```text
Lịch sử mua BHYT
```

### Household members

Nếu customer thuộc hộ:

```text
Tất cả thành viên trong hộ
```

Mỗi thành viên có:

```text
Thông tin khách hàng
BHYT hiện tại
Lịch sử BHYT
```

Không query từng member bằng nhiều query riêng lẻ nếu có thể thiết kế query phù hợp.

Tránh N+1 query.

---

# 26. Hộ gia đình

Backend hỗ trợ:

## Thống kê

```text
Tổng số hộ
```

---

## Search

Search hộ theo:

```text
hoVaTen
cccd
```

Cần xác định rõ:

> search theo thành viên trong hộ hay chỉ chủ hộ?

Nếu requirement chưa nói rõ:

**HỎI USER.**

---

# 27. Filter hộ

Các filter:

```text
ALL_CUSTOMERS
WITHOUT_HOUSEHOLD
WITHOUT_BHYT
```

Phải kết hợp với search.

---

# 28. Thêm khách hàng

Khi thêm khách hàng:

* Validate CCCD.
* Kiểm tra CCCD duplicate.
* Validate họ tên.
* Validate ngày sinh nếu có.
* Set default.
* Ghi audit log.

Không tự động tạo hộ gia đình nếu requirement chưa yêu cầu.

---

# 29. Household relationship

Quan hệ:

```text
KhachHang
    ↓
ThanhVienHoGiaDinh
    ↓
HoGiaDinh
```

Không nên thiết kế:

```text
KhachHang.idHoGiaDinh
```

nếu schema hiện tại đã sử dụng bảng:

```text
thanh_vien_ho_gia_dinh
```

Agent phải tôn trọng schema được cung cấp.

---

# 30. Repository

Repository phải tập trung query.

Không đưa business logic vào Repository.

Các query phức tạp phải:

* Có tên rõ ràng.
* Parameter rõ ràng.
* Có điều kiện soft-delete.
* Tránh query dư thừa.
* Tránh N+1.

---

# 31. Pagination

Nếu danh sách có khả năng lớn:

ưu tiên pagination.

Nếu project hiện tại đã sử dụng cursor pagination:

```text
lastId
pageSize
hasMore
```

thì phải giữ nguyên architecture hiện tại.

Không tự ý chuyển sang Page/Pageable nếu project đang thống nhất cursor pagination.

Response:

```json
{
  "data": [],
  "lastId": 20,
  "hasMore": true
}
```

---

# 32. Search + Filter

Search và filter phải được thiết kế để kết hợp.

Không tạo nhiều API riêng biệt nếu có thể dùng một API:

```text
GET /api/bhyt
```

với:

```text
keyword
filterType
pageSize
lastId
```

Ví dụ:

```text
GET /api/bhyt?keyword=nguyen&filterType=EXPIRING_30_DAYS
```

---

# 33. API Design

Controller phải RESTful.

Ví dụ:

```text
GET    /api/bhyt
GET    /api/bhyt/{id}
POST   /api/bhyt
PUT    /api/bhyt/{id}
DELETE /api/bhyt/{id}
```

Customer:

```text
GET    /api/khach-hang
GET    /api/khach-hang/{id}
POST   /api/khach-hang
PUT    /api/khach-hang/{id}
DELETE /api/khach-hang/{id}
```

Household:

```text
GET /api/ho-gia-dinh
GET /api/ho-gia-dinh/{id}
```

Nếu API hiện tại của source đã có naming convention:

**Ưu tiên giữ convention hiện tại.**

---

# 34. Naming Convention

Java:

```text
PascalCase
```

Variable:

```text
camelCase
```

Database:

```text
snake_case
```

API:

```text
kebab-case
```

Ví dụ:

```text
/api/ho-gia-dinh
```

---

# 35. Service naming

Ví dụ:

```text
BhytService
BhytServiceImpl
KhachHangService
KhachHangServiceImpl
HoGiaDinhService
HoGiaDinhServiceImpl
```

Nếu project hiện tại không dùng interface + implementation:

không bắt buộc tạo thêm chỉ để theo mẫu.

Ưu tiên architecture hiện tại.

---

# 36. Audit Architecture

Nên tách audit khỏi business service.

Ví dụ:

```text
utils
└── AuditLogUtils
```

hoặc:

```text
services
└── AuditLogService
```

Business service:

```java
auditLogService.log(
    ActionType.CREATE,
    "BHYT",
    bhyt.getId(),
    "Thêm BHYT"
);
```

Không copy-paste code ghi log ở mọi method.

---

# 37. System Log và Audit Log

Hai loại log có mục đích khác nhau.

## System Log

Trả lời:

> Hệ thống xảy ra lỗi gì?

## Audit Log

Trả lời:

> Người dùng đã thực hiện hành động gì trên dữ liệu nào?

Không được trộn hai loại log thành một.

---

# 38. Audit Before / After

Đối với UPDATE nếu có thể:

Audit nên lưu:

```text
before
after
```

Ví dụ:

```text
UPDATE BHYT #15

before:
soThangMua=6

after:
soThangMua=12
```

Nhưng phải tránh log dữ liệu nhạy cảm.

Nếu chưa có yêu cầu lưu before/after:

không tự ý tạo database audit history phức tạp.

Có thể bắt đầu bằng structured audit log.

---

# 39. Concurrency

Đối với nghiệp vụ có khả năng cập nhật đồng thời:

Agent phải kiểm tra race condition.

Ví dụ:

* Cùng lúc cập nhật BHYT.
* Cùng lúc cập nhật số thành viên hộ.
* Cùng lúc tạo BHYT mới nhất.

Nếu cần locking/constraint:

phải phân tích trước.

Không tự thêm lock nếu chưa có lý do.

---

# 40. Database Constraint

Nếu dữ liệu cần unique:

ưu tiên enforce ở database.

Ví dụ:

```text
cccd UNIQUE
```

Không chỉ kiểm tra bằng:

```java
existsByCccd(...)
```

vì có thể xảy ra race condition.

Application validation + database constraint nên được sử dụng cùng nhau khi phù hợp.

---

# 41. Performance

Agent phải chú ý:

* N+1 query.
* SELECT dư dữ liệu.
* Query không có điều kiện.
* Query LIKE không cần thiết.
* Pagination.
* Index cho field search/filter nếu cần.

Các field thường được cân nhắc index:

```text
cccd
da_xoa
han_the
da_nhan_hoa_hong
id_khach_hang
id_ho_gia_dinh
```

Nhưng **không tự tạo index hàng loạt** nếu chưa có lý do.

---

# 42. Security

Không được:

* Log password.
* Log token.
* Log full CCCD.
* Expose Entity trực tiếp.
* Expose stack trace cho FE.
* Trả SQL exception trực tiếp cho FE.

Production response chỉ trả:

```json
{
  "message": "Đã xảy ra lỗi",
  "code": "INTERNAL_SERVER_ERROR",
  "data": null
}
```

Chi tiết technical error phải nằm trong system log.

---

# 43. Exception Handling

GlobalExceptionHandler phải xử lý ít nhất:

```text
MyException
MethodArgumentNotValidException
ConstraintViolationException
HttpMessageNotReadableException
Exception
```

Không được để stack trace trả thẳng về frontend.

---

# 44. Configuration

Không hard-code:

* Database path.
* Log path.
* File upload path.
* Application-specific config.

Nếu config đã tồn tại trong project:

phải sử dụng config hiện tại.

Không tạo thêm config trùng.

---

# 45. File Upload

Nếu có upload:

* Validate file.
* Validate extension/content type.
* Giới hạn kích thước.
* Không lưu file trực tiếp vào database nếu requirement không yêu cầu.
* Lưu path/reference.
* Không log file binary.

---

# 46. Testing

Sau khi implementation:

Agent phải ít nhất kiểm tra:

### Compile

```bash
mvn clean compile
```

hoặc command tương ứng project.

### Test

```bash
mvn test
```

Nếu project dùng Gradle:

```bash
./gradlew test
```

Nếu test fail:

Agent phải sửa lỗi nếu nguyên nhân nằm trong code vừa triển khai.

Không được bỏ qua lỗi compile.

---

# 47. Migration / Existing Data

Nếu database đã có dữ liệu:

Agent phải đặc biệt cẩn thận.

Không:

```text
DROP TABLE
```

Không:

```text
DELETE ALL
```

Không reset database.

Nếu thay đổi schema có nguy cơ mất dữ liệu:

**DỪNG VÀ HỎI USER.**

---

# 48. Không tự ý thay đổi công nghệ

Backend yêu cầu:

```text
Spring Boot
SQLite
```

Không tự ý chuyển sang:

```text
MySQL
PostgreSQL
MongoDB
Redis
```

Không thêm framework mới nếu không cần.

---

# 49. Dependency

Trước khi thêm dependency:

1. Kiểm tra dependency hiện tại.
2. Xem có dependency tương đương chưa.
3. Chỉ thêm nếu thật sự cần.
4. Tránh dependency dư thừa.

---

# 50. Coding Style

Code phải:

* Dễ đọc.
* Tên biến rõ nghĩa.
* Method ngắn vừa phải.
* Không duplicate logic.
* Không magic number.
* Không magic string.
* Không comment những thứ hiển nhiên.
* Comment cho business rule phức tạp.
* Không tạo abstraction quá mức.

---

# 51. Business Constant

Các giá trị nghiệp vụ như:

```text
30 ngày
14 ngày
7 ngày
60 ngày
```

không hard-code ở nhiều nơi.

Có thể đưa vào:

```text
Constant
```

hoặc enum/config phù hợp.

---

# 52. Không over-engineering

Không tạo:

* Generic Repository phức tạp.
* Generic Service quá mức.
* Design Pattern không cần thiết.
* Event-driven architecture.
* Kafka.
* Redis.
* Microservice.

nếu requirement chỉ là một Spring Boot + SQLite monolith.

Mục tiêu:

> Simple, clean, maintainable.

---

# 53. Quy trình xử lý một yêu cầu

Ví dụ user yêu cầu:

> "Thêm API thêm BHYT."

Agent phải thực hiện:

```text
1. Phân tích requirement
        ↓
2. Kiểm tra Entity
        ↓
3. Kiểm tra DTO
        ↓
4. Kiểm tra Repository
        ↓
5. Kiểm tra Service
        ↓
6. Kiểm tra Controller
        ↓
7. Kiểm tra nghiệp vụ
        ↓
8. Nếu chưa rõ → HỎI USER
        ↓
9. Implement
        ↓
10. Validation
        ↓
11. Transaction
        ↓
12. Audit log
        ↓
13. Exception handling
        ↓
14. Compile
        ↓
15. Test
        ↓
16. Báo cáo kết quả
```

---

# 54. Khi nào phải hỏi User

BẮT BUỘC hỏi user khi:

### Database

* Cần thêm field.
* Cần xóa field.
* Cần đổi type.
* Cần đổi relationship.

### Business

* Không biết cách tính.
* Không biết trạng thái.
* Không biết điều kiện.
* Không biết hành vi khi duplicate.
* Không biết cách xử lý edge case.

### API

* Không rõ request.
* Không rõ response.
* Không rõ status code.
* Không rõ pagination.

### Security

* Không rõ authentication.
* Không rõ authorization.
* Không rõ user nào được thao tác.

### Audit

* Không rõ cần lưu gì.
* Không rõ có lưu before/after hay không.
* Không rõ actor/user.

---

# 55. Cách hỏi User

Không hỏi chung chung:

> "Bạn giải thích thêm được không?"

Phải hỏi cụ thể.

Ví dụ:

> Khi thêm BHYT cho khách hàng đã có một BHYT `bhyt_moi_nhat = 1`, tôi chưa rõ nghiệp vụ mong muốn là:
>
> 1. Đánh dấu BHYT cũ thành `bhyt_moi_nhat = 0` rồi tạo BHYT mới.
> 2. Không cho phép thêm và trả lỗi.
> 3. Cập nhật BHYT hiện tại.
>
> Bạn muốn phương án nào?

Nếu có nhiều vấn đề độc lập, có thể hỏi thành danh sách.

---

# 56. Không được tiếp tục khi đang chờ clarification

Nếu nghiệp vụ quan trọng chưa rõ:

**STOP.**

Không được:

* Tự đoán.
* Implement tạm.
* Chọn phương án phổ biến.
* "Để sau sửa".

---

# 57. Response sau khi hoàn thành

Sau khi hoàn thành một task, Agent phải báo:

```text
## Đã thực hiện

- ...
- ...
- ...

## File đã thay đổi

- ...
- ...

## API

- ...

## Database

- ...

## Logging

- ...

## Validation

- ...

## Test

- Compile: PASS/FAIL
- Test: PASS/FAIL

## Lưu ý

- ...
```

Nếu có vấn đề chưa rõ:

không báo hoàn thành.

Phải hỏi user trước.

---

# 58. Definition of Done

Một chức năng chỉ được xem là hoàn thành khi:

* [ ] Requirement đã rõ.
* [ ] Database đã kiểm tra.
* [ ] Entity đã đúng.
* [ ] DTO đã đúng.
* [ ] Validation đã có.
* [ ] Repository đã có.
* [ ] Service đã có.
* [ ] Controller đã có.
* [ ] Exception đã xử lý.
* [ ] API response thống nhất.
* [ ] Date format đúng.
* [ ] Transaction đúng khi cần.
* [ ] Soft delete đúng khi cần.
* [ ] System log phù hợp.
* [ ] Audit log phù hợp.
* [ ] Không log dữ liệu nhạy cảm.
* [ ] Không tạo N+1 query không cần thiết.
* [ ] Compile PASS.
* [ ] Test PASS hoặc đã giải thích rõ lý do không có test.
* [ ] Không phá vỡ chức năng hiện tại.

---

# 59. Nguyên tắc cuối cùng

Agent phải ưu tiên:

> **Không đoán nghiệp vụ.**

Nếu không biết:

> **Hỏi.**

Nếu source đã có:

> **Tái sử dụng.**

Nếu có thể sửa:

> **Sửa thay vì tạo trùng.**

Nếu thay đổi database:

> **Phải xác nhận khi requirement chưa rõ.**

Nếu thay đổi dữ liệu:

> **Phải có transaction khi cần.**

Nếu người dùng thực hiện action:

> **Phải audit log.**

Nếu hệ thống xảy ra lỗi:

> **Phải system log.**

Nếu trả dữ liệu cho frontend:

> **Dùng DTO và format ngày `dd/MM/yyyy`.**

Nếu hoàn thành:

> **Compile + Test trước khi báo DONE.**
