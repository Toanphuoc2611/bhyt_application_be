# Mô tả Backend — Phần mềm quản lý BHYT

> Tài liệu này mô tả kiến trúc và toàn bộ REST API của backend sau khi được viết lại
> (rework) để khớp với `api-design.md` + `features-spec.md`, kèm các điều chỉnh theo
> yêu cầu bổ sung của khách hàng. Toàn bộ comment trong mã nguồn bằng tiếng Việt.

---

## 1. Công nghệ

| Thành phần | Lựa chọn |
|---|---|
| Ngôn ngữ / JDK | Java 17 |
| Framework | Spring Boot **3.3.5** (LTS) |
| Truy cập DB | Spring Data JPA + Hibernate 6.5 |
| CSDL | **SQLite** (`database.db`), dialect `org.hibernate.community.dialect.SQLiteDialect` |
| Quản lý schema | `spring.jpa.hibernate.ddl-auto=update` (Hibernate tự tạo/cập nhật bảng) |
| Validation | `spring-boot-starter-validation` (Bean Validation) |
| Xuất Excel | Apache POI (`poi-ooxml`, `SXSSFWorkbook`) |
| Giảm boilerplate | Lombok |

Chạy server: `./mvnw spring-boot:run` → `http://localhost:8080`.
Nhập dữ liệu từ Excel (chạy tay 1 lần): `java -jar bhyt.jar --nhap-excel="<đường dẫn>.xlsm"` (xem §8).

### Tham số nghiệp vụ — file `bhyt-config.properties` đặt CẠNH file .exe/.jar

Toàn bộ "giá tiền" và tham số công thức nằm trong một file text ngoài, đọc lúc chạy,
để đổi được mà **không build lại** (khai báo bằng `spring.config.import` — nếu file
không có thì dùng giá trị mặc định đóng gói sẵn).

```properties
bhyt.luong-co-so=2530000              # lương cơ sở t (VNĐ) - file Excel khách hàng đang dùng
bhyt.muc-dong=0.045                   # mức đóng m (4,5%)
bhyt.so-thang-mac-dinh=12             # số tháng khi ô "Tháng mua" trống
bhyt.ty-le-theo-bac=1.0,0.7,0.6,0.5,0.4   # tỷ lệ theo "Người thứ"; phần tử cuối cho bậc >= 5
bhyt.ty-le-mstb=0.40                  # người có mstb: luôn 40%
bhyt.the-moi-cong-them-thang=1        # thẻ mới gói mặc định: hạn = ngày mua + (12+1) tháng
bhyt.the-moi-cong-them-ngay=29        # thẻ mới gói "N tháng": hạn = ngày mua + N tháng + 29 ngày
bhyt.gia-han-nguong-ngay-mua-som=31   # gia hạn: mua sớm hơn hạn cũ > 31 ngày -> giữ nguyên hạn cũ
bhyt.backup.enabled=true
bhyt.backup.dir=backup
```

---

## 2. Kiến trúc & cấu trúc package

Phân lớp cổ điển `controller → service → repository → entity`, DTO tách khỏi entity.

```
com.application.bhyt
├── config/          BhytProperties, WebConfig (CORS), SaoLuuKhoiDongRunner
├── controller/      5 REST controller (base path /api/v1)
├── service/         Nghiệp vụ + BaoHiemTinhToanService (công thức tiền/hạn thẻ)
├── repository/      Spring Data JPA repository (mọi query lọc da_xoa = 0)
├── mapper/          Entity -> DTO (BaoHiemYTeMapper, KhachHangMapper, ThanhVienMapper)
├── entity/          4 entity ánh xạ 4 bảng
├── enums/           LoaiBHYT, TrangThaiBHYT, BoLocKhachHang, ErrorCode, ActionType
├── dto/request/     DTO đầu vào (có annotation validation)
├── dto/response/    DTO đầu ra + ApiResponse (vỏ bọc) + PageResponse (phân trang)
├── exception/       MyException + GlobalExceptionHandler
└── util/            DateUtils (chuỗi dd/MM/yyyy <-> LocalDate)
```

### Luồng một request điển hình

```
HTTP → Controller (nhận DTO, @Valid)
     → Service (@Transactional: kiểm tra nghiệp vụ, gọi repository, gọi mapper)
       → BaoHiemTinhToanService (nếu cần tính hạn thẻ / số tiền thu)
       → Repository (JPQL, luôn kèm điều kiện da_xoa = 0)
     → Mapper (entity → DTO)
     ← ApiResponse<...> (JSON)
```

### Vỏ bọc response

Mọi endpoint trả về:

```json
{ "code": "00000", "message": "Thành công", "data": { ... } }
```

Danh sách có phân trang thì `data` là `PageResponse`:

```json
{ "content": [...], "totalElements": 120, "totalPages": 6, "page": 0, "size": 20 }
```

Lỗi: `code` là mã lỗi (bảng ở mục 6), HTTP status tương ứng, `data = null`.

---

## 3. Quy ước nghiệp vụ bắt buộc

| Quy ước | Ý nghĩa |
|---|---|
| **Xóa mềm** | `khach_hang.da_xoa` / `bao_hiem_y_te.da_xoa` = 1 nghĩa là "ẩn", không xóa vật lý. Mọi danh sách/tìm kiếm/thống kê đều lọc `da_xoa = 0`. |
| **`bhyt_moi_nhat`** | `1` = thẻ hiện hành; `0` = thẻ cũ đã bị thay bằng lần gia hạn sau. Gia hạn KHÔNG xóa dòng cũ. |
| **`ngay_ket_thuc`** (thành viên hộ) | Rời hộ = set ngày kết thúc, không xóa dòng. Hộ hiện tại của khách hàng = dòng có `ngay_ket_thuc IS NULL`. Mỗi khách hàng tối đa 1 hộ. |
| **`so_thanh_vien`** | Luôn được service tính lại = số thành viên đang hoạt động; không nhận trực tiếp từ client. |
| **`so_lan_mua_cua_ho`** | **Do người dùng nhập ở FE ("Người thứ"), bắt buộc, không null.** Backend không tự tính. Khi gia hạn thì sao chép bậc từ thẻ được gia hạn. |
| **`goi_mac_dinh`** | `true` = ô "Tháng mua" để trống (dùng số tháng mặc định + cộng thêm 1 tháng cho thẻ mới). `false` = chọn "N tháng" cụ thể. |
| **`ngay_het_han_cu`** | Khi gia hạn, là hạn của thẻ đang có (cột "Ngày hết hạn" trong file Excel); mốc để tính hạn thẻ mới. Qua API gia hạn, mặc định = `han_the` của thẻ cũ. |

### Công thức (trong `BaoHiemTinhToanService`) — bám ĐÚNG file Excel "DS Mua BHYT"

Mọi hằng số lấy từ `bhyt-config.properties` (§1).

**Hạn thẻ:**
- **Thẻ mới**, gói mặc định (ô "Tháng mua" trống): `hanThe = ngayMua + (soThangMacDinh + 1) tháng` — Excel: `EDATE(ngày mua, 13)`.
- **Thẻ mới**, gói "N tháng": `hanThe = ngayMua + N tháng + 29 ngày`.
- **Gia hạn**: nếu `(ngayMua − ngayHetHanCu) ≥ −31 ngày` → `hanThe = ngayHetHanCu + (12 hoặc N) tháng`; ngược lại (mua quá sớm) → `hanThe = ngayHetHanCu` (giữ nguyên, không cộng thêm).

> ⚠️ Hệ quả của quy tắc gia hạn: nếu khách gia hạn sớm hơn hạn cũ **quá 31 ngày**, hạn thẻ mới = hạn cũ (không được cộng thêm tháng). Đây là đúng như công thức trong file Excel; phần lớn dòng trong file rơi vào trường hợp này.

**Số tiền thu:**
```
t = luong-co-so (2.530.000)   ;   m = muc-dong (0.045)
tyLe = coMstb ? ty-le-mstb(0.40) : ty-le-theo-bac[bậc]   (bậc >= số phần tử -> phần tử cuối)
soThang = goiMacDinh ? so-thang-mac-dinh(12) : N
soTienThu = round( tyLe * m * t * soThang )
```
Ví dụ bậc 2, 12 tháng: `round(0.70 × 0.045 × 2.530.000 × 12) = 956.340`.
Unit test tham số hóa: `BaoHiemTinhToanServiceTest` (6 test tiền + 4 test hạn thẻ).

---

## 4. Cơ sở dữ liệu

4 bảng theo `database-schema.md` (giữ nguyên tên cột tiếng Việt `snake_case`):

- **`khach_hang`** — `id, cccd (unique), ho_va_ten (NOT NULL), ngay_sinh, dia_chi, so_dien_thoai, lien_lac_khac, hinh_anh, ghi_chu, bhyt_khac, co_mstb, da_xoa, ngay_tao, ngay_cap_nhat`
- **`bao_hiem_y_te`** — `id, id_khach_hang (FK), ngay_mua (NOT NULL), ngay_het_han_cu, so_thang_mua (NOT NULL), goi_mac_dinh, han_the (tính), so_lan_mua_cua_ho (NOT NULL), so_tien_thu (tính), da_nhan_hoa_hong, bhyt_moi_nhat, da_xoa, loai, noi_dang_ky, ngay_tao, ngay_cap_nhat`
- **`ho_gia_dinh`** — `id, ten (nhãn hộ, từ cột "Hộ" khi nhập Excel), so_thanh_vien (dẫn xuất), hinh_anh, ngay_tao, ngay_cap_nhat`
- **`thanh_vien_ho_gia_dinh`** — `id, id_thanh_vien (FK), id_ho_gia_dinh (FK), ngay_tao, ngay_ket_thuc`

Đã thêm index: `khach_hang(ho_va_ten)`, `bao_hiem_y_te(id_khach_hang, bhyt_moi_nhat)`, `bao_hiem_y_te(han_the)`, `thanh_vien_ho_gia_dinh(id_thanh_vien, ngay_ket_thuc)`.

> **Không có** bảng `xuat_file_lot` / `xuat_file_lot_chi_tiet` — theo yêu cầu khách hàng, việc xuất Excel chọn lọc chuyển sang FE (xem mục 5.5).

---

## 5. REST API (base path `/api/v1`)

Ngày trao đổi dưới dạng chuỗi **`dd/MM/yyyy`**.

### 5.1. Khách hàng — `/khach-hang` (Trang 3)

| Method | Path | Mô tả |
|---|---|---|
| GET | `/khach-hang?search=&filter=&page=&size=` | Danh sách. `filter` ∈ `TAT_CA` \| `KHONG_CO_HO` \| `CHUA_MUA_BHYT`. `search` khớp `ho_va_ten` hoặc `cccd`. |
| GET | `/khach-hang/{id}` | Một khách hàng (kèm hộ hiện tại + thẻ hiện hành). |
| GET | `/khach-hang/{id}/chi-tiet` | Khách hàng + lịch sử mua BHYT + các thành viên cùng hộ (kèm thẻ hiện hành của họ). |
| POST | `/khach-hang` | Tạo mới. Body: `cccd*, hoVaTen*, ngaySinh, diaChi, soDienThoai, lienLacKhac, hinhAnh, ghiChu, bhytKhac, coMstb, idHoGiaDinh?` (nếu có `idHoGiaDinh` thì gán luôn vào hộ). |
| PUT | `/khach-hang/{id}` | Cập nhật (trường null = giữ nguyên). |
| DELETE | `/khach-hang/{id}` | Xóa mềm (`da_xoa = 1`). |

### 5.2. Hộ gia đình — `/ho-gia-dinh` (Trang 3)

| Method | Path | Mô tả |
|---|---|---|
| GET | `/ho-gia-dinh?page=&size=` | Danh sách hộ. |
| GET | `/ho-gia-dinh/{id}` | Chi tiết hộ + thành viên hiện tại. |
| POST | `/ho-gia-dinh` | Tạo hộ. Body (tùy chọn): `{ hinhAnh }`. |
| PUT | `/ho-gia-dinh/{id}` | Cập nhật ảnh hộ. |
| POST | `/ho-gia-dinh/{id}/thanh-vien` | Thêm thành viên. Body: `{ idKhachHang* }`. Lỗi nếu khách hàng đã thuộc hộ khác. |
| DELETE | `/ho-gia-dinh/{id}/thanh-vien/{idKhachHang}` | Thành viên rời hộ (set `ngay_ket_thuc`, không xóa dòng). |

### 5.3. Bảo hiểm y tế — `/bao-hiem-y-te` (Trang 2)

| Method | Path | Mô tả |
|---|---|---|
| GET | `/bao-hiem-y-te?search=&trangThai=&page=&size=` | Danh sách. `trangThai` ∈ `HET_HAN` \| `GAN_HET_HAN_7` \| `GAN_HET_HAN_14` \| `GAN_HET_HAN_30` \| `GAN_HET_HAN_60` \| `CHUA_NHAN_HOA_HONG`. `search` khớp tên/CCCD khách hàng. |
| GET | `/bao-hiem-y-te/{id}` | Một thẻ. |
| POST | `/bao-hiem-y-te` | Tạo thẻ mới (`loai = "mới"`). Body: `idKhachHang*, ngayMua*, soThangMua? (>0; trống = gói mặc định), soLanMuaCuaHo* (>0), noiDangKy?`. Server tính `hanThe`, `soTienThu`; lật thẻ hiện hành cũ về `bhyt_moi_nhat = 0`. |
| POST | `/bao-hiem-y-te/{id}/gia-han` | Gia hạn thẻ `{id}`: tạo dòng `loai = "gia hạn"`, **giữ nguyên `soLanMuaCuaHo`** của thẻ cũ, lật thẻ hiện hành cũ. Body: `ngayMua*, ngayHetHanCu? (trống = lấy hanThe của thẻ cũ), soThangMua? (>0), noiDangKy?`. |
| PUT | `/bao-hiem-y-te/{id}` | Cập nhật. Sửa `ngayHetHanCu`/`ngayMua`/`goiMacDinh` ⇒ tính lại `hanThe`; đổi `soLanMuaCuaHo`/`soThangMua` ⇒ tính lại `soTienThu`. |
| DELETE | `/bao-hiem-y-te/{id}` | Xóa mềm. |
| PATCH | `/bao-hiem-y-te/xac-nhan-hoa-hong` | Xác nhận nhận hoa hồng hàng loạt. Body: `{ ids: number[] }` ⇒ `da_nhan_hoa_hong = 1`. |

### 5.4. Thống kê — `/thong-ke` (Trang 1)

| Method | Path | Mô tả |
|---|---|---|
| GET | `/thong-ke/tong-quan` | 5 chỉ số: `bhytSapHetHan30Ngay`, `tongHoGiaDinh`, `bhytChuaNhanHoaHong` (thẻ hiện hành `bhyt_moi_nhat=1`), `tongKhachHang`, `khachHangChuaCoHo`. |
| GET | `/thong-ke/doanh-thu?tuNgay=&denNgay=` | Lọc trên `ngay_mua`. Trả: `tongTien`, `tongThe`, `soTheMoi`, `soTheGiaHan` (đủ để FE vẽ biểu đồ tách "mới"/"gia hạn"). |

### 5.5. Xuất file — `/xuat-file` (Trang 4)

| Method | Path | Mô tả |
|---|---|---|
| GET | `/xuat-file/cho-xuat` | Trả `{ danhSach, danhSachHo }`. `danhSach`: mỗi thẻ BHYT còn hiệu lực theo đúng thứ tự cột client (`HỘ, HỌ VÀ TÊN, NĂM SINH, ĐỊA CHỈ, NGÀY MUA, CCCD, LOẠI, NƠI ĐK, GHI CHÚ, SỐ LẦN MUA CỦA HỘ, THÀNH TIỀN`). `danhSachHo`: roster các hộ cho sheet "ds hộ". |

> **Việc sinh file `.xlsx` chọn lọc do FE thực hiện.** Backend chỉ cấp dữ liệu, không
> theo dõi "đã xuất" / "hoàn tác".

### 5.6. Sao lưu toàn bộ (không phải API)

Mỗi lần server khởi động xong (`ApplicationReadyEvent`) và sau khi nhập Excel,
`SaoLuuExcelService` ghi `backup/bhyt_backup_yyyyMMdd_HHmmss.xlsx` (file mới, không ghi đè).
Nếu lỗi: ghi log mức ERROR + in `System.err`, **server vẫn chạy**. Tắt bằng `bhyt.backup.enabled=false`.

**Định dạng file backup giống hệt `DS Mua BHYT.xlsm`** — có thể mở đọc quen thuộc VÀ
`--nhap-excel=<file backup>` để phục hồi lại DB:

| Sheet | Nội dung |
|---|---|
| `Danh_sach` | Dòng 1 tiêu đề + ô cấu hình `Z1` (lương cơ sở), `AA1=Z1×m`, `AB1=Z1×m×12`. Dòng 2 header 18 cột. Dòng 3+ mỗi khách hàng 1 dòng (thẻ hiện hành), **nhóm theo hộ** (tên hộ ở dòng đầu, để trống dòng sau). Cột "Thành tiền" / "Hạn thẻ" là **công thức Excel thật** (tái tạo y công thức file gốc, tham chiếu `$AA$1`/`$AB$1`) kèm sẵn giá trị đã tính. Khách hàng không thuộc hộ → cột "Hộ" = tên họ (nhập lại thành hộ 1 người). |
| `Het_han` | Cùng định dạng, chỉ gồm thẻ `han_the < hôm nay`. |
| `_khach_hang`, `_bao_hiem_y_te`, `_ho_gia_dinh`, `_thanh_vien` | Dump raw đầy đủ từng bảng — đảm bảo không mất dữ liệu (lịch sử thẻ, thành viên đã rời hộ…). Các sheet này **không** được `--nhap-excel` đọc; chỉ để tra cứu / phục hồi thủ công. |

Đã kiểm thử **round-trip**: nhập file gốc → sinh backup → `--nhap-excel` chính file backup đó
→ **49 hộ / 98 khách / 98 thẻ, 0 cảnh báo** (dữ liệu trùng khớp 100%).

---

## 6. Bảng mã lỗi (`ErrorCode`)

| Mã | HTTP | Ý nghĩa |
|---|---|---|
| `00000` | 200 | Thành công |
| `40000` / `40010` / `40011` | 400 | Yêu cầu / dữ liệu / khoảng ngày không hợp lệ |
| `40401` | 404 | Không tìm thấy khách hàng |
| `40402` | 400 | Khách hàng đã bị xóa |
| `40403` | 404 | Không tìm thấy thẻ BHYT |
| `40404` | 400 | Thẻ BHYT đã bị xóa |
| `40405` | 404 | Không tìm thấy hộ gia đình |
| `40406` | 404 | Không tìm thấy thành viên trong hộ |
| `40901` | 409 | CCCD đã tồn tại |
| `40902` | 409 | Khách hàng đã thuộc hộ khác |
| `50001` | 500 | Lỗi hệ thống |
| `50002` | 500 | Lỗi khi xuất file Excel |

---

## 7. Những thay đổi so với bản backend cũ

1. **Base path** `/api/*` → `/api/v1/*`; đổi tên/động từ theo `api-design.md`
   (`/detail` → `/chi-tiet`, `POST /receive-commission` → `PATCH /xac-nhan-hoa-hong`…).
2. **Phân trang** cursor (đang lỗi: `lastId` luôn = 0) → Spring `Page` chuẩn
   (`content/totalElements/totalPages/page/size`).
3. **Sửa công thức số tiền thu**: bản cũ lệch index bậc (bậc 1 ra 70%), `ArrayIndexOutOfBounds`
   khi bậc ≥ 5, **thiếu `× số tháng mua`**, không làm tròn. Nay tách hẳn vào
   `BaoHiemTinhToanService` + unit test.
4. **`so_lan_mua_cua_ho`**: thành `@NotNull`, được lưu vào entity (bản cũ không lưu),
   dùng đúng trong công thức.
5. **Thêm luồng gia hạn** `POST /{id}/gia-han` (giữ nguyên bậc) — bản cũ không có.
6. **Sửa lỗi runtime**: bản cũ `KhachHangService` inject `BaoHiemYTeService` bằng setter
   không được gọi ⇒ NPE ở `/khach-hang/{id}/detail`. Nay dùng mapper component, không có
   phụ thuộc vòng.
7. **Hằng số** `2.340.000` / `4.5%` chuyển vào `application.properties`.
8. **Bộ lọc** khách hàng (`KHONG_CO_HO`, `CHUA_MUA_BHYT`) và trạng thái thẻ được
   implement đầy đủ (bản cũ bỏ trống `filterType`).
9. **Spring Boot** 4.1.0 → 3.3.5 (LTS); `spring-boot-starter-webmvc` → `-web`;
   bỏ MapStruct (không dùng); thêm `validation` + `poi-ooxml`.
10. **Sao lưu Excel toàn bộ khi khởi động** — tính năng mới theo yêu cầu khách hàng.
11. Toàn bộ comment mã nguồn viết bằng **tiếng Việt**.

> Các file `IMPLEMENTATION_SUMMARY.md`, `API_DOCUMENTATION.md`, `QUICK_START.md` là
> tài liệu của bản cũ, **đã lỗi thời** — tài liệu này thay thế chúng.

---

## 8. Công cụ nhập dữ liệu từ file Excel

`NhapExcelService` + tham số dòng lệnh `--nhap-excel`.

```
java -jar bhyt.jar --nhap-excel="C:\...\DS Mua BHYT (1).xlsm"
```

- **Không mở web server.** Xóa sạch 4 bảng → nạp lại từ sheet `Danh_sach` (tiêu đề ở
  dòng 2) → sao lưu Excel → thoát. In báo cáo: số hộ / khách hàng / thẻ, dòng bị bỏ
  qua, và **cảnh báo lệch số liệu** (khi `han_the` / `so_tien_thu` tính lại khác giá
  trị trong sheet).
- **Ánh xạ cột:** `Hộ` → `ho_gia_dinh.ten` (ô trống = cùng hộ dòng trên); `Họ và tên,
  Năm sinh, Địa chỉ, CCCD, Liên hệ, SĐT, Ghi Chú` → `khach_hang`; `Mstb`="mstb" →
  `co_mstb=1`; `Ngày mua, Loại, Nơi ĐK, Người thứ` → `bao_hiem_y_te`; `Tháng mua`
  ("6 tháng"→6, trống→gói mặc định); `Ngày hết hạn` → `ngay_het_han_cu`.
  `han_the`, `so_tien_thu` được **tính lại** bằng `BaoHiemTinhToanService`.
- Đã chạy thử với file thật: **49 hộ, 98 khách hàng, 98 thẻ, 0 bỏ qua, 1 cảnh báo**
  (1 dòng có ô "Hạn thẻ" rỗng/lỗi trong file gốc — giá trị tính lại của backend đúng hơn).
  97/98 dòng khớp y hệt số liệu trong sheet → xác nhận công thức đã port đúng.

---

## 9. Lưu ý khi đóng gói thành .exe

| Vấn đề | Cách xử lý đề xuất |
|---|---|
| **Đường dẫn tương đối** (`database.db`, `backup/`, `logs/`, `bhyt-config.properties`) phụ thuộc thư mục làm việc — nhấp đúp .exe có thể chạy ở thư mục khác | Đặt working dir cố định (script `.bat` `cd /d %~dp0`), hoặc sửa cấu hình dùng đường dẫn tuyệt đối cạnh .exe. **Không** để DB trong `C:\Program Files` (chỉ đọc). |
| **Cần JRE** | Dùng `jpackage` (JDK 17+) để đóng gói kèm JRE thành 1 thư mục/`.exe` (~70–90 MB). GraalVM native-image gọn hơn nhưng POI + Hibernate cần nhiều cấu hình reflection — khó. |
| **Cổng 8080 có thể bị chiếm** | Cho phép đổi cổng qua `bhyt-config.properties` (`server.port=...`); bắt lỗi bind và báo rõ. |
| **Không có cửa sổ / khó tắt** | Kèm 1 shortcut mở `http://localhost:8080`, và 1 script/nút để tắt (gọi `POST /actuator/shutdown` nếu bật, hoặc kill theo cổng). |
| **File `.properties` sửa bằng Notepad** | Chỉ để **số + dấu chấm** trong `bhyt-config.properties` (đã thiết kế vậy). Không bỏ tiếng Việt có dấu. |
| **Encoding console Windows** | Chạy với `-Dfile.encoding=UTF-8` (mặc định từ Java 18) để log/thông báo tiếng Việt không lỗi. |
| **SQLite 1 ghi đồng thời + antivirus khóa file** | Đã giới hạn pool = 1. Loại trừ thư mục dữ liệu khỏi quét realtime nếu chậm. |
| **Sao lưu tích lũy** | `backup/` sinh 1 file mỗi lần khởi động — nên dọn định kỳ hoặc thêm cấu hình giữ N bản gần nhất. |
| **Nâng cấp phiên bản** | `ddl-auto=update` chỉ thêm cột/bảng, không xóa/đổi kiểu. Khi đổi schema lớn cần script tay hoặc chuyển Flyway. |
