# SVJob – Nền tảng việc làm part-time cho sinh viên

Đồ án Chuyên đề tốt nghiệp 2, nhóm 4, lớp 261_71ITGR40303_06. GVHD: ThS. Nguyễn Văn Trung.

| Thành viên | MSSV | Phụ trách trong code |
|---|---|---|
| Trần Huỳnh Minh Tuấn | 2374802010543 | Backend: `backend/` (API, JWT, phân quyền, nghiệp vụ), unit test |
| Hà Đăng Khoa | 2374802010228 | Frontend: `frontend/` (giao diện, gọi API, validate phía client) |
| Bùi Hoàng Khang | 2374802010207 | CSDL + tích hợp + kiểm thử: `database/`, `FileStorageService`, `JobSpecs`, `postman/`, `docker-compose.yml` |

## 1. Công nghệ

- **Backend:** Java 17, Spring Boot 3.3, Spring Security + JWT (hết hạn sau 60 phút), BCrypt, Spring Data JPA
- **Frontend:** ReactJS 18 (Vite), React Router, Axios, Bootstrap 5 + Bootstrap Icons, font Be Vietnam Pro
- **CSDL:** MySQL 8
- **Kiểm thử:** JUnit 5 + Mockito, JaCoCo, Postman/Newman

## 2. Cấu trúc thư mục

```
svjob/
├── backend/                     Spring Boot
│   ├── db.properties.example    Mẫu khai báo mật khẩu MySQL riêng của từng máy
│   └── src/main/java/vn/vlu/svjob/
│       ├── config/              SecurityConfig: phân quyền theo URL, CORS
│       ├── security/            JwtUtil (tạo/đọc token), JwtAuthFilter (đọc token mỗi request)
│       ├── controller/          Nhận request, gọi service (không chứa nghiệp vụ)
│       ├── service/             Toàn bộ quy tắc nghiệp vụ BR-01 → BR-10
│       ├── repository/          Truy cập CSDL (JPA), JobSpecs = điều kiện lọc tin
│       ├── entity/              8 bảng theo ERD
│       ├── dto/                 Dữ liệu vào/ra của API
│       └── exception/           ApiException + GlobalExceptionHandler (mã lỗi 400/401/403/404/409)
├── frontend/src/
│   ├── api/                     axiosClient + mỗi nhóm API 1 file
│   ├── context/AuthContext.jsx  Lưu người dùng đang đăng nhập
│   ├── components/              Navbar, Footer, chuông thông báo, JobCard, Avatar, StatCard, ProtectedRoute...
│   ├── utils/                   format.js (tiền, ngày, trạng thái), ui.js (màu avatar, icon ngành nghề)
│   └── pages/                   auth/, student/, employer/, admin/ và các trang chung
├── database/                    schema.sql (tạo bảng), seed.sql (dữ liệu mẫu)
├── postman/                     Bộ test API theo 24 test case
└── docker-compose.yml           Chạy cả hệ thống bằng 1 lệnh
```

## 3. Cách chạy trên máy (Windows)

Làm lần lượt từ trên xuống. Mục **3.1 và 3.2 chỉ làm 1 lần** trên mỗi máy, những lần sau chỉ cần mục 3.4.

Mọi lệnh gõ trong **PowerShell** (bấm Start, gõ `PowerShell`, Enter). Lệnh có ghi *(Admin)* thì mở PowerShell bằng chuột phải → **Run as administrator**.

> **Mật khẩu MySQL:** dùng mật khẩu `root` có sẵn trên máy bạn, **không cần đổi** sang mật khẩu của người khác. Chỉ cần khai báo mật khẩu đó cho backend 1 lần ở mục 3.2.

### 3.1. Cài công cụ

| Công cụ | Dùng để | Tải ở đâu | Kiểm tra (gõ trong PowerShell mới) |
|---|---|---|---|
| JDK 17 trở lên (chạy được cả 21, 25) | Chạy backend Java | https://adoptium.net → Temurin 17 LTS, **Windows x64 .msi** | `java -version` ra `17` trở lên |
| Maven 3.9+ | Build và chạy backend | https://maven.apache.org/download.cgi → **Binary zip archive** | `mvn -v` ra `Apache Maven 3.9.x` |
| Node.js 20+ | Chạy frontend React | https://nodejs.org → bản **LTS**, file .msi | `node -v` ra `v20` trở lên |
| MySQL Server 8 | Cơ sở dữ liệu | https://dev.mysql.com/downloads/mysql/ (ZIP hoặc Installer) | `mysql --version` |
| MySQL Workbench *(không bắt buộc)* | Giao diện chạy SQL | https://dev.mysql.com/downloads/workbench/ | Mở lên kết nối được `root` |

Cài xong công cụ nào thì **đóng hết PowerShell rồi mở lại** mới gõ lệnh kiểm tra.

**a) JDK 17.** Chạy file .msi. Ở màn hình chọn tính năng, bật **Set JAVA_HOME variable** và **Add to PATH** (bấm biểu tượng ổ đĩa → *Will be installed on local hard drive*). Next đến hết.

**b) Maven.** Giải nén file zip, ví dụ ra `D:\apache-maven-3.9.9`, rồi thêm `D:\apache-maven-3.9.9\bin` vào PATH (xem mục **e**).
> Dùng IntelliJ IDEA thì có thể bỏ qua Maven: mở thư mục `backend` bằng IntelliJ, đợi tải thư viện xong, mở `SvjobApplication.java`, bấm nút ▶ Run.

**c) Node.js.** Chạy file .msi, Next đến hết (tự thêm vào PATH).

**d) MySQL.** Làm theo **một** trong hai trường hợp:

**d1. Máy đã có MySQL từ trước** (đã cài cho môn khác, đã có mật khẩu root):
- Không cài thêm, không đổi mật khẩu. Kiểm tra MySQL đang chạy: `Get-Service *mysql*` → cột Status phải là `Running`. Nếu là `Stopped` thì `Start-Service <tên service>` *(Admin)*, ví dụ `Start-Service MySQL80`.
- Nếu gõ `mysql` báo *not recognized* thì thêm thư mục `bin` của MySQL vào PATH (mục **e**). Bản Installer thường nằm ở `C:\Program Files\MySQL\MySQL Server 8.0\bin`.
- Nhớ mật khẩu root của bạn, sẽ dùng ở mục 3.2 và 3.3.

**d2. Máy chưa có MySQL, dùng bản ZIP** (ví dụ giải nén ở `D:\mysql-8.0.46-winx64`). Bản ZIP chỉ có chương trình, chưa có dữ liệu và chưa tự chạy, nên làm thêm:
1. **Kiểm tra thư mục giải nén không bị lồng 2 lớp.** Mở thư mục đó phải thấy ngay `bin`, `lib`, `share`... Nếu lại thấy một thư mục `mysql-8.0.46-winx64` bên trong thì dùng thư mục bên trong (hoặc kéo nội dung ra ngoài).
2. **Kiểm tra máy không có MySQL khác đang chạy:** `Get-Service *mysql*`. Nếu đã có service MySQL đang `Running` thì máy bạn thuộc trường hợp **d1**, không cần làm d2 (hai MySQL cùng dùng cổng 3306 sẽ đụng nhau).
3. Thêm `D:\mysql-8.0.46-winx64\bin` vào PATH (mục **e**), đóng mở lại PowerShell, gõ `mysql --version` để chắc chắn.
4. Tạo thư mục dữ liệu, **chỉ làm 1 lần duy nhất** (làm lại sẽ báo lỗi vì đã có thư mục `data`):
   ```powershell
   mysqld --initialize-insecure --console
   ```
   Lệnh này tạo `D:\mysql-8.0.46-winx64\data` và tài khoản `root` chưa có mật khẩu.
5. Cài MySQL thành service để tự chạy khi bật máy *(Admin)*:
   ```powershell
   mysqld --install MySQL80
   net start MySQL80
   ```
6. Đặt mật khẩu root **tùy ý bạn** (thay `MatKhauCuaBan` bằng mật khẩu muốn dùng):
   ```powershell
   mysql -u root --skip-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'MatKhauCuaBan';"
   ```
   Thử lại: `mysql -u root -p` → gõ mật khẩu → thấy `mysql>` là xong, gõ `exit`.

> Nếu chưa thêm PATH mà muốn chạy thẳng trong thư mục `bin` thì PowerShell bắt buộc gõ `.\` phía trước: `cd D:\mysql-8.0.46-winx64\bin` rồi `.\mysqld --initialize-insecure --console`, `.\mysql -u root -p`. Gõ `mysqld` không có `.\` sẽ báo *not recognized*.
> Nếu báo thiếu `VCRUNTIME140_1.dll` / `MSVCP140.dll`: cài https://aka.ms/vs/17/release/vc_redist.x64.exe rồi chạy lại.
> Dùng **MySQL Installer** (.msi) thay cho ZIP thì bỏ qua bước 4–6, mật khẩu root nhập trong lúc cài.

**e) Cách thêm thư mục vào PATH.** Start → gõ `environment` → **Edit the system environment variables** → **Environment Variables…** → khung **System variables** chọn `Path` → **Edit…** → **New** → dán đường dẫn thư mục `bin` → OK cả 3 cửa sổ → **đóng hết PowerShell, mở lại**.

### 3.2. Khai báo mật khẩu MySQL cho backend (1 lần)

Backend cần biết mật khẩu MySQL trên máy bạn. Cách đơn giản nhất:

1. Vào thư mục `backend`, chép file `db.properties.example` thành `db.properties` (cùng thư mục).
2. Mở `db.properties` bằng Notepad, sửa dòng `DB_PASSWORD=` thành mật khẩu root của bạn, ví dụ `DB_PASSWORD=abc123`. Lưu lại.

> Lưu ý: Windows hay ẩn đuôi file nên file có thể bị thành `db.properties.txt`. Trong File Explorer bật **View → Show → File name extensions** để kiểm tra, tên đúng phải là `db.properties`.

Mỗi máy có file `db.properties` riêng, file này đã được bỏ qua trong Git nên không ảnh hưởng máy khác. Không có file này thì backend dùng mật khẩu mặc định `Svjob@2026`.

> Cách khác, không cần tạo file: trước khi chạy backend gõ `$env:DB_PASSWORD="abc123"` trong cùng cửa sổ PowerShell (chỉ có tác dụng ở cửa sổ đó).

### 3.3. Tạo cơ sở dữ liệu (1 lần, hoặc khi muốn reset dữ liệu về ban đầu)

Chạy 2 file trong thư mục `database` **đúng thứ tự**: `schema.sql` (tạo database `svjob` và 8 bảng) rồi `seed.sql` (dữ liệu mẫu). `schema.sql` xóa database `svjob` cũ nếu có, nên chạy lại là về dữ liệu ban đầu.

**Cách A – PowerShell.** Mở PowerShell **tại thư mục gốc project** (thư mục chứa `backend`, `frontend`, `database`): trong File Explorer mở thư mục đó, bấm vào thanh địa chỉ, gõ `powershell`, Enter. Rồi chạy:
```powershell
mysql -u root -p --default-character-set=utf8mb4 -e "source database/schema.sql"
mysql -u root -p --default-character-set=utf8mb4 -e "source database/seed.sql"
```
Mỗi lệnh hỏi `Enter password:` → gõ mật khẩu MySQL **của bạn** (gõ không hiện ký tự, cứ gõ rồi Enter).
> Không dùng `mysql -u root -p < database/schema.sql` trong PowerShell vì PowerShell không hỗ trợ dấu `<`.

**Cách B – MySQL Workbench.** Mở kết nối `root` → **File → Open SQL Script…** → chọn `database/schema.sql` → bấm tia sét ⚡. Làm tiếp y như vậy với `database/seed.sql`.

**Kiểm tra:** `mysql -u root -p -e "SELECT COUNT(*) FROM svjob.job_posts;"` ra `60` là đúng.

### 3.4. Chạy chương trình (mỗi lần dùng)

Mở **2 cửa sổ PowerShell** tại thư mục gốc project, chạy song song, không tắt trong lúc dùng web.

**Cửa sổ 1 – Backend** (http://localhost:8080):
```powershell
cd backend
mvn spring-boot:run
```
Lần đầu Maven tải thư viện mất vài phút. Chạy xong khi thấy dòng `Started SvjobApplication in ... seconds`.
Kiểm tra: mở http://localhost:8080/api/categories trên trình duyệt, thấy danh sách 10 ngành nghề là được.

**Cửa sổ 2 – Frontend** (http://localhost:5173):
```powershell
cd frontend
npm install      # chỉ cần lần đầu, hoặc khi package.json thay đổi
npm run dev
```
Mở http://localhost:5173 và đăng nhập bằng tài khoản ở mục 4.

**Tắt chương trình:** bấm `Ctrl + C` trong từng cửa sổ.

### 3.5. Lỗi thường gặp

| Thông báo lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| `'mysql' / 'mvn' / 'node' is not recognized` | Chưa thêm PATH, chưa mở lại PowerShell, hoặc đang đứng trong `bin` mà thiếu `.\` | Làm lại mục 3.1-e và mở PowerShell mới, hoặc gõ `.\mysql` |
| `Access denied for user 'root'@'localhost'` khi chạy backend | Backend đang dùng sai mật khẩu | Kiểm tra `backend/db.properties` (mục 3.2): đúng tên file, đúng thư mục, đúng mật khẩu |
| `Access denied` khi gõ `mysql -u root -p` | Gõ sai mật khẩu hoặc quên mật khẩu | Xem mục "Quên mật khẩu root" bên dưới |
| `Unknown database 'svjob'` | Chưa chạy `schema.sql` | Làm mục 3.3 |
| `Communications link failure` / `Can't connect to MySQL server` | MySQL chưa chạy | `Get-Service *mysql*` rồi `Start-Service <tên>` *(Admin)* |
| `Port 8080 was already in use` | Đang có backend khác chạy | Tắt cửa sổ backend cũ (Ctrl + C) rồi chạy lại |
| `mysqld --install` báo service đã tồn tại / `net start` lỗi | Máy đã có MySQL khác | Dùng MySQL đó (trường hợp d1), không cài thêm |
| `release version 17 not supported` | Maven đang dùng JDK cũ | Cài JDK 17, `mvn -v` dòng `Java version` phải ≥ 17 |
| Web mở được nhưng không có dữ liệu | Backend chưa chạy hoặc đã lỗi | Xem lại cửa sổ backend |
| Tiếng Việt trong dữ liệu bị lỗi font | Chạy SQL thiếu `--default-character-set=utf8mb4` | Chạy lại mục 3.3 đúng lệnh |

**Quên mật khẩu root MySQL** (đặt lại bằng `--init-file`):
1. Tắt MySQL *(Admin)*: `Stop-Service MySQL80` (tên service xem bằng `Get-Service *mysql*`).
2. Tạo file `C:\mysql-init.txt` có đúng 1 dòng: `ALTER USER 'root'@'localhost' IDENTIFIED BY 'MatKhauMoi';`
3. Chạy *(Admin)*:
   - MySQL bản ZIP: `mysqld --init-file=C:\mysql-init.txt --console`
   - MySQL bản Installer: `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --init-file=C:\mysql-init.txt --console`
4. Đợi dòng `ready for connections` thì bấm `Ctrl + C`, xóa file `C:\mysql-init.txt`, rồi `Start-Service MySQL80`.
5. Đăng nhập lại bằng `MatKhauMoi` và sửa `backend/db.properties` cho khớp.

**Cách nhanh nếu máy có Docker Desktop:** chạy `docker compose up --build` ở thư mục gốc rồi mở http://localhost:5173 (không cần cài JDK, Maven, Node, MySQL).

## 4. Tài khoản demo

Mật khẩu chung của tất cả tài khoản là `Demo@2026`.

| Vai trò | Email | Dữ liệu sẵn có |
|---|---|---|
| Sinh viên | sinhvien.demo@svjob.vn | Có đơn Chờ duyệt, Bị từ chối, Được nhận; có 1 việc đã Hoàn thành để thử đánh giá |
| Nhà tuyển dụng | ntd.demo@svjob.vn | The Coffee Corner, 5 tin, tin “Nhân viên phục vụ quán cà phê” có 3 ứng viên chờ duyệt |
| Quản trị viên | admin@svjob.vn | Xem toàn bộ tin và người dùng |
| SV khác | an.sv@vanlanguni.vn, binh.sv@…, cuong.sv@… | `dung.sv@vanlanguni.vn` đang bị khóa |
| NTD khác | giasu.anhduong@gmail.com, hoamai.mart@gmail.com và 8 đơn vị khác (trà sữa, nhà hàng, sự kiện, shop, trung tâm Anh ngữ, kho TMĐT, studio thiết kế, khách sạn) | Xem hết trong trang Admin → Người dùng |

**Dữ liệu mẫu** (`seed.sql`): 1 Admin, 11 nhà tuyển dụng, 17 sinh viên, 10 ngành nghề, 60 tin tuyển dụng (40 Đang tuyển, 6 Đã nhận người, 7 Hoàn thành, 6 Đã đóng, 1 Bị ẩn), 153 đơn ứng tuyển và 13 đánh giá.

## 5. Kiểm thử

```bash
cd backend
mvn test                     # 54 test (JUnit + Mockito, kèm 4 test API chạy trên H2)
# Báo cáo độ phủ: backend/target/site/jacoco/index.html (tầng service đạt khoảng 91%, yêu cầu NFR-04 là ≥ 70%)
```

**Postman:** Import `postman/SVJob.postman_collection.json`, nạp lại `schema.sql` + `seed.sql` cho dữ liệu sạch, rồi chạy cả bộ bằng Collection Runner theo thứ tự. Riêng request “Tải CV PDF” phải tự chọn file PDF ở tab Body. Có thể chạy bằng dòng lệnh: `npx newman run postman/SVJob.postman_collection.json`.

Tên test bắt đầu bằng mã test case (TC-01 … TC-24) để đối chiếu với file `Kiem_thu.xlsx`.

## 6. Chức năng ↔ nơi viết code

| FR | Chức năng | Backend (service) | Frontend (page) |
|---|---|---|---|
| FR-01, 02 | Đăng ký, đăng nhập | AuthService | auth/RegisterPage, auth/LoginPage |
| FR-03 | Phân quyền 3 vai trò | config/SecurityConfig | components/ProtectedRoute, AppNavbar |
| FR-04 | Hồ sơ SV + CV | ProfileService, FileStorageService | student/StudentProfilePage |
| FR-05 | Hồ sơ NTD | ProfileService | employer/EmployerProfilePage |
| FR-06, 07 | Đăng, sửa, đóng, xóa tin | JobService | employer/JobFormPage, employer/MyJobsPage |
| FR-08 | Admin ẩn tin vi phạm | JobService.hide | admin/AdminJobsPage |
| FR-09, 10 | Danh sách, tìm kiếm, lọc | JobService.search + JobSpecs | HomePage |
| FR-11 | Chi tiết tin | JobService.detail | JobDetailPage |
| FR-12, 13 | Ứng tuyển, đơn của tôi, rút đơn | ApplicationService | JobDetailPage, student/MyApplicationsPage |
| FR-14, 15 | Xem ứng viên, duyệt/từ chối | ApplicationService | employer/ApplicantsPage |
| FR-16 | Trạng thái công việc | JobService.changeStatus | employer/MyJobsPage |
| FR-17 | Thông báo | NotificationService | components/NotificationBell |
| FR-18, 19 | Đánh giá, điểm trung bình | ReviewService | components/ReviewModal, ReviewsPage |
| FR-20 | Khóa/mở khóa tài khoản | AdminUserService | admin/AdminUsersPage |
| FR-21 | Danh mục ngành nghề | CategoryService | admin/AdminCategoriesPage |

## 7. Deploy (buổi 9)

- **CSDL:** tạo MySQL miễn phí trên Aiven hoặc Railway, chạy `schema.sql` + `seed.sql`. Lưu ý xóa 2 dòng `DROP DATABASE` / `CREATE DATABASE` nếu dịch vụ đã tạo sẵn database.
- **Backend (Render):** tạo Web Service từ thư mục `backend` (Render tự dùng `Dockerfile`), khai báo `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (chuỗi ngẫu nhiên ≥ 32 ký tự) và `CORS_ORIGIN` (link Vercel).
- **Frontend (Vercel):** Root Directory = `frontend`, khai báo `VITE_API_URL = https://<backend>.onrender.com/api`.
- File CV lưu trên ổ đĩa của server. Gói miễn phí của Render sẽ mất file khi khởi động lại, đây là hạn chế đã biết và được ghi vào báo cáo.

## Tính năng bổ sung theo góp ý giảng viên (đợt 30–32)
- **Lọc theo ca làm việc** (sáng / trưa / chiều / tối / linh hoạt) và **khung giờ làm** (`fromHour`, `toHour`, ví dụ 17h–22h) và **thời gian đăng** (24 giờ / 7 ngày / 30 ngày): `GET /api/jobs?shift=EVENING&postedWithin=7`.
- **Tuyển gấp**: NTD bật khi đăng tin, tin hiện huy hiệu GẤP và được xếp lên đầu danh sách.
- **Email khi đơn được nhận**: mặc định tắt (chỉ ghi log). Bật bằng cách khai báo `MAIL_ENABLED=true`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` trong `backend/db.properties` (mẫu trong `db.properties.example`, file thật không đưa lên Git). Gmail dùng "Mật khẩu ứng dụng".
- DB đã có dữ liệu: chạy `database/migration_30_shift_urgent.sql`. DB mới chỉ cần `schema.sql` + `seed.sql`.
