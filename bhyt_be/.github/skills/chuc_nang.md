# 3. Yêu cầu chức năng Backend

## 3.1. Nguyên tắc

Backend chịu trách nhiệm cung cấp toàn bộ:

* API nghiệp vụ.
* Xử lý dữ liệu.
* Validation.
* Tìm kiếm.
* Lọc.
* Thống kê.
* Tính toán nghiệp vụ BHYT.
* Quản lý khách hàng.
* Quản lý hộ gia đình.
* Quản lý thành viên hộ.
* Quản lý lịch sử BHYT.
* Quản lý trạng thái BHYT.
* Soft delete.
* Transaction.
* System log.
* Action/Audit log.

Frontend chịu trách nhiệm:

* Hiển thị giao diện.
* Hiển thị bảng.
* Form nhập liệu.
* Popup/modal.
* Tab.
* Checkbox.
* Icon thao tác.
* Xuất Excel.
* Xử lý giao diện liên quan đến Excel.

Backend **không chịu trách nhiệm tạo giao diện Excel**, trừ khi sau này user có yêu cầu riêng về API/file export.

---

# 3.2. Dashboard / Thống kê

Backend phải cung cấp API phục vụ trang thống kê.

## 3.2.1. Số BHYT sắp hết hạn

Thống kê số BHYT sắp hết hạn trong vòng 30 ngày.

Điều kiện được xác định dựa trên:

```text
han_the
```

và ngày hiện tại.

Khoảng thời gian:

```text
today → today + 30 days
```

Chỉ tính các BHYT hợp lệ theo trạng thái dữ liệu hiện tại.

Không tự đưa BHYT đã xóa vào thống kê nếu chưa có yêu cầu khác.

---

## 3.2.2. Tổng số hộ gia đình

Backend cung cấp tổng số hộ gia đình hiện có.

Kết quả trả về dạng số.

---

## 3.2.3. Tổng số BHYT chưa nhận hoa hồng

Dựa trên:

```text
da_nhan_hoa_hong = 0
```

Backend trả về tổng số BHYT chưa nhận hoa hồng.

---

## 3.2.4. Tổng số khách hàng

Backend cung cấp tổng số khách hàng chưa bị xóa:

```text
da_xoa = 0
```

---

## 3.2.5. Tổng số khách hàng chưa có hộ

Backend cung cấp số khách hàng hiện chưa thuộc hộ gia đình.

Quan hệ hộ gia đình phải dựa trên bảng:

```text
thanh_vien_ho_gia_dinh
```

và trạng thái thành viên hiện tại.

Nếu `ngay_ket_thuc` có giá trị thì phải xác định thành viên đó đã rời hộ.

Nếu nghiệp vụ xác định "chưa có hộ" khác với cách hiểu trên, Agent phải hỏi User trước khi implement.

---

## 3.2.6. Thống kê doanh thu

Backend cung cấp API thống kê theo khoảng thời gian:

```text
fromDate
toDate
```

Thống kê:

```text
totalMoney
totalBhyt
totalNew
totalRenew
```

Trong đó:

* `totalMoney`: tổng số tiền BHYT.
* `totalBhyt`: tổng số BHYT.
* `totalNew`: tổng số BHYT loại mới.
* `totalRenew`: tổng số BHYT loại gia hạn.

Phân loại mới/gia hạn dựa trên:

```text
loai
```

với giá trị:

```text
mới
gia hạn
```

Ngày dùng để lọc thống kê phải được xác định thống nhất theo nghiệp vụ BHYT.

Nếu chưa xác định rõ sử dụng `ngay_mua`, `ngay_tao` hoặc ngày khác:

**Agent phải hỏi User, không tự đoán.**

---

# 3.3. Quản lý BHYT

## 3.3.1. Lấy danh sách BHYT

Backend cung cấp API lấy danh sách BHYT.

Danh sách phải hỗ trợ:

* Pagination.
* Search.
* Filter.
* Kết hợp search + filter.

Thông tin trả về phải là DTO, không trả Entity trực tiếp.

---

# 3.3.2. Tìm kiếm BHYT

Cho phép tìm kiếm theo:

```text
ho_va_ten
cccd
```

Search phải có thể kết hợp với filter trạng thái.

Ví dụ:

```text
GET /api/bhyt?keyword=nguyen&filterType=EXPIRING_30_DAYS
```

Nếu không truyền keyword thì trả về toàn bộ dữ liệu phù hợp với filter.

Search không phân biệt chữ hoa/chữ thường nếu database/query hỗ trợ.

---

# 3.3.3. Lọc BHYT

Backend hỗ trợ các trạng thái:

```text
EXPIRED
EXPIRING_7_DAYS
EXPIRING_14_DAYS
EXPIRING_30_DAYS
EXPIRING_60_DAYS
UNRECEIVED_COMMISSION
```

Ý nghĩa:

### EXPIRED

BHYT đã hết hạn.

```text
han_the < today
```

### EXPIRING_7_DAYS

BHYT gần hết hạn trong phạm vi 7 ngày.

### EXPIRING_14_DAYS

BHYT gần hết hạn trong phạm vi 14 ngày.

### EXPIRING_30_DAYS

BHYT gần hết hạn trong phạm vi 30 ngày.

### EXPIRING_60_DAYS

BHYT gần hết hạn trong phạm vi 60 ngày.

### UNRECEIVED_COMMISSION

BHYT chưa nhận hoa hồng:

```text
da_nhan_hoa_hong = 0
```

---

## 3.3.4. Quy tắc khoảng ngày

Agent **không được tự suy đoán** cách xử lý trường hợp biên.

Ví dụ:

```text
han_the = today + 7 days
```

có thuộc cả:

```text
EXPIRING_7_DAYS
EXPIRING_14_DAYS
```

hay không?

Nếu requirement chưa xác định rõ thì phải hỏi User trước khi implement.

Nếu User xác nhận các filter là khoảng độc lập, Agent phải implement đúng khoảng đã xác nhận.

---

# 3.3.5. Thêm BHYT

Backend cung cấp API thêm BHYT.

Request phải chứa các thông tin cần thiết để tạo BHYT.

Backend phải:

1. Validate request.
2. Kiểm tra khách hàng tồn tại.
3. Kiểm tra khách hàng có bị xóa hay không.
4. Tính `han_the` theo nghiệp vụ.
5. Tính `so_tien_thu` theo nghiệp vụ.
6. Xử lý `so_lan_mua_cua_ho`.
7. Xử lý `bhyt_moi_nhat`.
8. Xử lý trạng thái BHYT cũ nếu nghiệp vụ yêu cầu.
9. Lưu dữ liệu.
10. Ghi action log.
11. Thực hiện trong transaction nếu có nhiều thay đổi dữ liệu.

---

# 3.3.6. Tính hạn thẻ

Theo tài liệu:

### Trường hợp có `ngay_co_han`

```text
han_the = ngay_co_han + so_thang_mua
```

### Trường hợp không có `ngay_co_han`

```text
han_the = ngay_mua + so_thang_mua
```

`han_the` là dữ liệu backend tự tính.

Frontend không được quyết định giá trị cuối cùng của `han_the`.

Nếu frontend gửi `han_the`, backend vẫn phải xác định giá trị hợp lệ theo business rule.

---

# 3.3.7. Tính số tiền BHYT

Backend chịu trách nhiệm tính:

```text
so_tien_thu
```

Các thông số trong tài liệu:

```text
t = 2.340.000
m = 4.5%
```

### Không có MSTB

Mức đóng theo thành viên:

```text
Người thứ nhất  = m × t × 100%
Người thứ hai   = m × t × 70%
Người thứ ba    = m × t × 60%
Người thứ tư    = m × t × 50%
Người thứ năm+  = m × t × 40%
```

### Có MSTB

Tài liệu hiện tại chỉ thể hiện một phần công thức:

```text
40% × m × t × ...
```

Phần công thức trong tài liệu bị thiếu nên Agent **KHÔNG ĐƯỢC tự hoàn thiện công thức**.

Khi implement tới phần này mà chưa có công thức đầy đủ:

**DỪNG VÀ HỎI USER.**

---

# 3.3.8. Cập nhật BHYT

Backend cung cấp API cập nhật BHYT.

Khi cập nhật:

1. Kiểm tra BHYT tồn tại.
2. Kiểm tra `da_xoa`.
3. Validate request.
4. Cập nhật dữ liệu được phép chỉnh sửa.
5. Tính lại các trường auto-calc nếu business rule yêu cầu.
6. Cập nhật `ngay_cap_nhat`.
7. Ghi action log.
8. Sử dụng transaction khi cập nhật nhiều bản ghi.

Không cho phép frontend tự quyết định các field auto-calc.

---

# 3.3.9. Xóa BHYT

Xóa BHYT mặc định sử dụng soft delete:

```text
da_xoa = 1
```

Không hard delete dữ liệu.

Sau khi xóa:

* Không xuất hiện trong danh sách BHYT thông thường.
* Không được tính vào thống kê thông thường.
* Không được xuất hiện trong search/filter thông thường.

Phải ghi action log:

```text
DELETE
```

---

# 3.3.10. Nhận hoa hồng

Backend phải hỗ trợ thay đổi trạng thái:

```text
da_nhan_hoa_hong
```

Giá trị:

```text
0 = chưa nhận
1 = đã nhận
```

Nếu chức năng xác nhận hoa hồng được thực hiện hàng loạt:

Backend phải hỗ trợ xử lý danh sách ID trong một request.

Ví dụ:

```json
{
  "ids": [1, 2, 3]
}
```

Backend phải validate tất cả ID trước khi thay đổi dữ liệu.

Nếu một phần thất bại và nghiệp vụ yêu cầu atomic operation:

toàn bộ transaction phải rollback.

---

# 3.4. Chi tiết BHYT

Khi FE yêu cầu chi tiết một khách hàng:

Backend phải trả về thông tin BHYT của khách hàng được chọn.

Nếu khách hàng thuộc hộ gia đình:

Backend phải trả thêm thông tin các thành viên trong cùng hộ.

Thông tin phải bao gồm:

## Khách hàng

* CCCD.
* Họ tên.
* Ngày sinh.
* Địa chỉ.
* Liên hệ.
* Các thông tin cần thiết khác.

## BHYT hiện tại

Lấy BHYT:

```text
bhyt_moi_nhat = 1
```

và chưa bị xóa.

## Lịch sử BHYT

Bao gồm các lần mua trước:

```text
bhyt_moi_nhat = 0
```

cũng như BHYT hiện tại nếu API contract yêu cầu.

Nếu cần phân biệt rõ "current" và "history", response DTO phải tách riêng.

---

# 3.5. Quản lý khách hàng / Hộ gia đình

## 3.5.1. Lấy danh sách khách hàng

Backend cung cấp API lấy danh sách khách hàng.

Hỗ trợ:

* Pagination.
* Search.
* Filter.

---

# 3.5.2. Tìm kiếm khách hàng

Search theo:

```text
ho_va_ten
cccd
```

Có thể kết hợp với filter.

---

# 3.5.3. Filter khách hàng

Backend hỗ trợ:

```text
ALL_CUSTOMERS
WITHOUT_HOUSEHOLD
WITHOUT_BHYT
```

### ALL_CUSTOMERS

Lấy tất cả khách hàng hợp lệ.

### WITHOUT_HOUSEHOLD

Lấy khách hàng hiện chưa thuộc hộ.

### WITHOUT_BHYT

Lấy khách hàng chưa có BHYT hiện tại.

Việc xác định "có BHYT" phải dựa trên BHYT hiện tại:

```text
bhyt_moi_nhat = 1
```

và:

```text
da_xoa = 0
```

Nếu business rule khác với cách xác định trên:

**Agent phải hỏi User.**

---

# 3.5.4. Thêm khách hàng

Backend cung cấp API thêm người mới.

Validate:

* CCCD.
* Họ và tên.
* Các field bắt buộc.
* Dữ liệu ngày tháng.
* Các field có constraint.

CCCD phải unique.

Nếu CCCD đã tồn tại:

trả ErrorCode phù hợp.

Sau khi thêm:

* Set default value.
* Set `da_xoa = 0`.
* Set ngày tạo.
* Set ngày cập nhật.
* Ghi action log.

---

# 3.5.5. Cập nhật khách hàng

Backend cung cấp API cập nhật khách hàng.

Phải:

* Kiểm tra khách hàng tồn tại.
* Kiểm tra `da_xoa`.
* Validate request.
* Kiểm tra duplicate CCCD nếu CCCD được phép thay đổi.
* Cập nhật `ngay_cap_nhat`.
* Ghi action log.

---

# 3.5.6. Xóa khách hàng

Mặc định sử dụng:

```text
da_xoa = 1
```

Không hard delete.

Tuy nhiên nếu khách hàng đang có:

* BHYT.
* Quan hệ hộ gia đình.

thì hành vi khi xóa phải tuân thủ business rule.

Nếu chưa được xác định:

**DỪNG VÀ HỎI USER.**

---

# 3.6. Hộ gia đình

Backend phải quản lý:

```text
ho_gia_dinh
thanh_vien_ho_gia_dinh
khach_hang
```

Quan hệ:

```text
KhachHang
    ↓
ThanhVienHoGiaDinh
    ↓
HoGiaDinh
```

---

# 3.6.1. Thông tin hộ

Backend cung cấp:

* ID hộ.
* Số thành viên.
* Hình ảnh VNeID.
* Ngày tạo.
* Ngày cập nhật.

---

# 3.6.2. Thành viên hộ

Một thành viên được xác định thông qua:

```text
id_thanh_vien
id_ho_gia_dinh
```

Thành viên đang thuộc hộ khi:

```text
ngay_ket_thuc IS NULL
```

Nếu:

```text
ngay_ket_thuc IS NOT NULL
```

thì thành viên đã rời hộ.

---

# 3.6.3. Số thành viên

Field:

```text
so_thanh_vien
```

phải được backend cập nhật dựa trên thành viên hiện tại của hộ.

Không tin giá trị do frontend gửi lên nếu field này là auto-calc.

---

# 3.6.4. Chi tiết hộ

Backend cung cấp API lấy chi tiết hộ.

Response phải có:

* Thông tin hộ.
* Danh sách thành viên hiện tại.
* Thông tin BHYT hiện tại của từng thành viên nếu cần.
* Thông tin cần thiết phục vụ FE hiển thị.

Nếu FE cần lịch sử BHYT của từng thành viên thì backend phải cung cấp dữ liệu tương ứng hoặc API chi tiết riêng.

---

# 3.6.5. Thêm thành viên vào hộ

Backend phải:

1. Kiểm tra hộ tồn tại.
2. Kiểm tra khách hàng tồn tại.
3. Kiểm tra khách hàng chưa bị xóa.
4. Kiểm tra trạng thái quan hệ hộ hiện tại.
5. Tạo quan hệ.
6. Cập nhật `so_thanh_vien`.
7. Cập nhật `ngay_cap_nhat`.
8. Ghi action log.
9. Transaction.

Nếu khách hàng đang thuộc một hộ khác và chưa rời hộ:

**Không được tự quyết định cách xử lý. Phải hỏi User.**

---

# 3.6.6. Xóa thành viên khỏi hộ

Không xóa lịch sử quan hệ.

Thay vào đó cập nhật:

```text
ngay_ket_thuc
```

Thời điểm rời hộ phải được xác định theo business rule.

Sau đó cập nhật:

```text
so_thanh_vien
```

và ghi audit log.

---

# 3.7. API phục vụ giao diện

Backend không cần biết FE sử dụng:

* Table.
* Modal.
* Tab.
* Icon.
* Checkbox.
* Excel.

Backend chỉ cần cung cấp API và DTO phù hợp.

Ví dụ:

```text
GET    /api/bhyt
GET    /api/bhyt/{id}
POST   /api/bhyt
PUT    /api/bhyt/{id}
DELETE /api/bhyt/{id}

GET    /api/khach-hang
GET    /api/khach-hang/{id}
POST   /api/khach-hang
PUT    /api/khach-hang/{id}
DELETE /api/khach-hang/{id}

GET    /api/ho-gia-dinh
GET    /api/ho-gia-dinh/{id}
POST   /api/ho-gia-dinh/{id}/thanh-vien
DELETE /api/ho-gia-dinh/{id}/thanh-vien/{customerId}

GET    /api/dashboard
```

Tên endpoint cuối cùng phải tuân theo convention của source hiện tại.

---

# 3.8. Excel — Không thuộc Backend

Các chức năng sau **không thuộc phạm vi Backend hiện tại**:

* Hiển thị bảng xuất Excel.
* Checkbox chọn dữ liệu để xuất Excel.
* Tạo file Excel.
* Tạo sheet Excel.
* Download Excel.
* Undo lần xuất Excel trên giao diện.
* Quản lý UI của trang Excel.

Các nội dung này thuộc Frontend.

Tuy nhiên Backend phải cung cấp API để FE lấy dữ liệu cần thiết nếu FE không thể lấy trực tiếp từ dữ liệu đang có.

Ví dụ FE có thể gọi:

```text
GET /api/bhyt/export-data
```

nếu cần một response DTO chuyên dùng cho việc xuất Excel.

API này chỉ trả dữ liệu, không nhất thiết phải tạo `.xlsx`.

---

# 3.9. Audit Log cho chức năng

Các action nghiệp vụ phải được ghi audit log.

Ít nhất:

```text
CREATE_CUSTOMER
UPDATE_CUSTOMER
DELETE_CUSTOMER

CREATE_BHYT
UPDATE_BHYT
DELETE_BHYT

RECEIVE_COMMISSION

CREATE_HOUSEHOLD
ADD_HOUSEHOLD_MEMBER
REMOVE_HOUSEHOLD_MEMBER
```

Tên enum cuối cùng có thể thay đổi theo convention của project.

Mỗi log phải có tối thiểu:

```text
timestamp
action
module
entity
entityId
description
```

Nếu hệ thống có authentication/user thì bổ sung actor.

Không log dữ liệu nhạy cảm không cần thiết.

---

# 3.10. System Log

System log phải ghi:

* Exception.
* Database error.
* Validation error nghiêm trọng.
* Unexpected error.
* Application lifecycle.
* Các lỗi kỹ thuật quan trọng.

System log phục vụ developer/system administrator.

Không dùng system log thay thế audit log.

---

# 3.11. Transaction

Các nghiệp vụ thay đổi nhiều bảng phải sử dụng transaction.

Ví dụ:

### Thêm BHYT

Nếu việc thêm BHYT đồng thời cập nhật:

* BHYT cũ.
* BHYT mới.
* `bhyt_moi_nhat`.
* `so_lan_mua_cua_ho`.

thì phải nằm trong cùng transaction.

### Quản lý hộ

Nếu thêm/xóa thành viên đồng thời cập nhật:

```text
thanh_vien_ho_gia_dinh
ho_gia_dinh.so_thanh_vien
```

thì phải nằm trong cùng transaction.

---

# 3.12. Soft Delete

Các bảng có:

```text
da_xoa
```

phải được xử lý theo soft delete.

Các API thông thường không trả dữ liệu:

```text
da_xoa = 1
```

trừ khi có API quản trị riêng được yêu cầu.

---

# 3.13. Pagination

Các API danh sách phải hỗ trợ pagination.

Nếu project hiện tại đang sử dụng cursor pagination:

```text
lastId
pageSize
hasMore
```

thì tiếp tục sử dụng cơ chế đó.

Không tự ý chuyển architecture sang `Pageable` nếu project hiện tại đã có pagination convention.

---

# 3.14. Kết hợp Search + Filter + Pagination

Các API danh sách phải cho phép kết hợp:

```text
keyword
filterType
lastId
pageSize
```

Ví dụ:

```text
GET /api/bhyt
    ?keyword=nguyen
    &filterType=EXPIRING_30_DAYS
    &lastId=100
    &pageSize=20
```

Backend phải xử lý các điều kiện đồng thời.

Không yêu cầu FE gọi nhiều API rồi tự lọc dữ liệu nếu việc lọc có thể thực hiện chính xác tại database.

---

# 3.15. Không triển khai chức năng Excel trong Backend

Agent phải ghi nhớ:

> Excel là trách nhiệm của Frontend trong phạm vi yêu cầu hiện tại.

Không được tự tạo:

```text
ExcelService
ExcelExportService
Apache POI
XSSFWorkbook
```

chỉ vì tài liệu có nhắc đến chức năng Excel.

Chỉ triển khai phần backend phục vụ Excel nếu User yêu cầu rõ ràng.

---

# 3.16. Không tự bổ sung nghiệp vụ ngoài tài liệu

Các chức năng chưa được mô tả rõ không được tự động triển khai.

Ví dụ:

* Đăng nhập.
* Phân quyền.
* Quản lý user.
* Khôi phục dữ liệu đã xóa.
* Import Excel.
* Export Excel bằng backend.
* Lịch sử audit lưu vào database.
* Notification.
* Email.
* SMS.
* Redis.
* Cache.
* Backup database.

Nếu cần cho hệ thống nhưng tài liệu chưa xác định:

**Agent phải hỏi User trước.**
