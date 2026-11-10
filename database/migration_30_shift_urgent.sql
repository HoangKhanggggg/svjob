-- Đợt 30: thêm ca làm việc + tuyển gấp cho DB đã có dữ liệu (DB mới chỉ cần schema.sql + seed.sql)
ALTER TABLE job_posts
    ADD COLUMN shift  ENUM('MORNING','NOON','AFTERNOON','EVENING','FLEXIBLE') NOT NULL DEFAULT 'FLEXIBLE' AFTER work_time,
    ADD COLUMN start_hour TINYINT NULL AFTER shift,
    ADD COLUMN end_hour   TINYINT NULL AFTER start_hour,
    ADD COLUMN urgent BOOLEAN NOT NULL DEFAULT FALSE AFTER end_hour;
CREATE INDEX idx_job_shift ON job_posts(shift);
UPDATE job_posts SET shift = CASE
    WHEN work_time LIKE '%sáng%' THEN 'MORNING'
    WHEN work_time LIKE '%trưa%' THEN 'NOON'
    WHEN work_time LIKE '%chiều%' THEN 'AFTERNOON'
    WHEN work_time LIKE '%tối%' THEN 'EVENING'
    ELSE 'FLEXIBLE' END;
-- Giờ làm: lấy từ chuỗi dạng "18h-22h" trong work_time (không có thì để NULL)
UPDATE job_posts SET
    start_hour = CAST(REGEXP_SUBSTR(REGEXP_SUBSTR(work_time, '[0-9]{1,2}h([0-9]{2})?[-–][0-9]{1,2}h'), '^[0-9]+') AS UNSIGNED),
    end_hour   = CAST(REGEXP_SUBSTR(REGEXP_SUBSTR(work_time, '[0-9]{1,2}h([0-9]{2})?[-–][0-9]{1,2}h'), '[0-9]+(?=h$)') AS UNSIGNED)
WHERE work_time REGEXP '[0-9]{1,2}h([0-9]{2})?[-–][0-9]{1,2}h';
UPDATE job_posts SET start_hour = NULL, end_hour = NULL WHERE start_hour >= end_hour;
UPDATE job_posts SET urgent = TRUE WHERE id IN (1, 4, 12, 21, 33, 45);
