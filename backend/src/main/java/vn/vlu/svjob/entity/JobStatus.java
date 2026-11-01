package vn.vlu.svjob.entity;

/**
 * Trạng thái tin tuyển dụng (BR-09):
 * OPEN (Đang tuyển) -> FILLED (Đã nhận người) -> COMPLETED (Hoàn thành).
 * CLOSED: NTD chủ động đóng tin. HIDDEN: Admin ẩn tin vi phạm.
 */
public enum JobStatus {
    OPEN, FILLED, COMPLETED, CLOSED, HIDDEN
}
