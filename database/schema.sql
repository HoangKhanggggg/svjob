-- =====================================================================
-- SVJob – CSDL MySQL 8 (theo ERD + từ điển dữ liệu buổi 5)
-- Phụ trách: Bùi Hoàng Khang
-- Chạy: mysql -u root -p < schema.sql   (hoặc mở bằng MySQL Workbench rồi Execute)
-- =====================================================================
SET NAMES utf8mb4;
DROP DATABASE IF EXISTS svjob;
CREATE DATABASE svjob CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE svjob;

-- 1. Tài khoản (FR-01, FR-02, FR-03, FR-20)
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(15),
    role          ENUM('STUDENT','EMPLOYER','ADMIN') NOT NULL,
    status        ENUM('ACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Hồ sơ sinh viên (FR-04) – quan hệ 1-1 với users
CREATE TABLE student_profiles (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE,
    school        VARCHAR(150),
    major         VARCHAR(100),
    year_of_study TINYINT CHECK (year_of_study BETWEEN 1 AND 6),
    skills        VARCHAR(500),
    bio           TEXT,
    cv_url        VARCHAR(255),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Hồ sơ nhà tuyển dụng (FR-05) – quan hệ 1-1 với users
CREATE TABLE employer_profiles (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE,
    company_name  VARCHAR(150) NOT NULL,
    address       VARCHAR(255),
    description   TEXT,
    contact_phone VARCHAR(15),
    logo_url      VARCHAR(255),
    CONSTRAINT fk_employer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Ngành nghề (FR-21)
CREATE TABLE job_categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. Tin tuyển dụng (FR-06 → FR-11, FR-16)
CREATE TABLE job_posts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employer_id   BIGINT NOT NULL,
    category_id   INT NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT NOT NULL,
    salary_min    INT NOT NULL CHECK (salary_min >= 0),
    salary_max    INT,
    salary_unit   ENUM('HOUR','SHIFT','MONTH') NOT NULL,
    location      VARCHAR(100) NOT NULL,
    work_time     VARCHAR(200) NOT NULL,
    quantity      INT NOT NULL CHECK (quantity >= 1),
    deadline      DATE NOT NULL,
    status        ENUM('OPEN','FILLED','COMPLETED','CLOSED','HIDDEN') NOT NULL DEFAULT 'OPEN',
    hidden_reason VARCHAR(255),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_employer FOREIGN KEY (employer_id) REFERENCES users(id),
    CONSTRAINT fk_job_category FOREIGN KEY (category_id) REFERENCES job_categories(id),
    CONSTRAINT chk_salary CHECK (salary_max IS NULL OR salary_max >= salary_min)
);
-- Index phục vụ tìm kiếm/lọc (NFR-01)
CREATE INDEX idx_job_status_created ON job_posts(status, created_at);
CREATE INDEX idx_job_category ON job_posts(category_id);
CREATE INDEX idx_job_location ON job_posts(location);

-- 6. Đơn ứng tuyển (FR-12 → FR-15)
CREATE TABLE applications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id       BIGINT NOT NULL,
    student_id   BIGINT NOT NULL,
    cover_letter VARCHAR(1000),
    cv_url       VARCHAR(255),
    status       ENUM('PENDING','ACCEPTED','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    applied_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT uq_app_job_student UNIQUE (job_id, student_id)          -- BR-05
);

-- 7. Đánh giá hai chiều (FR-18, FR-19)
CREATE TABLE reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id      BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(500),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_job FOREIGN KEY (job_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id),
    CONSTRAINT uq_review UNIQUE (job_id, reviewer_id, reviewee_id)    -- BR-10
);

-- 8. Thông báo in-app (FR-17)
CREATE TABLE notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    content    VARCHAR(255) NOT NULL,
    link       VARCHAR(255),
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_noti_user_read ON notifications(user_id, is_read);
