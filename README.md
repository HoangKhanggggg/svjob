# SVJob – Nền tảng việc làm part-time cho sinh viên

Đồ án Chuyên đề tốt nghiệp 2 – Nhóm 4 – Đại học Văn Lang.

| Thành viên | Vai trò |
|---|---|
| Trần Huỳnh Minh Tuấn | Backend (Spring Boot) |
| Hà Đăng Khoa | Frontend (React) |
| Bùi Hoàng Khang | Cơ sở dữ liệu, tích hợp, kiểm thử |

## Công nghệ

- Backend: Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA
- Frontend: React 18 + Vite, Bootstrap 5
- CSDL: MySQL 8

## Cấu trúc thư mục (dự kiến)

```
backend/    mã nguồn Spring Boot
frontend/   mã nguồn React
database/   schema.sql, seed.sql
postman/    bộ test API
```

## Quy ước làm việc với Git

- `main`: bản ổn định, chỉ cập nhật khi kết thúc giai đoạn (buổi 9).
- `develop`: nhánh tích hợp, mọi Pull Request đều gửi vào đây.
- `feature/<tên-chức-năng>`: mỗi người làm trên nhánh riêng, xong thì tạo Pull Request vào `develop`.
- Commit message theo dạng `loại(phạm vi): mô tả`, ví dụ `feat(auth): API đăng nhập`, `fix(fe): sửa lỗi tràn ngang`.
- Không push thẳng lên `main` và `develop`. Khang review và merge Pull Request.
