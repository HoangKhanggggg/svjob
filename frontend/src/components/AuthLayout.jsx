/** Khung trang đăng nhập / đăng ký: form bên trái, giới thiệu ngắn bên phải. */
export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="auth-wrap row g-0">
      <div className="col-lg-7 auth-form">
        <h4 className="fw-bold mb-1">{title}</h4>
        <p className="text-muted mb-4">{subtitle}</p>
        {children}
      </div>
      <div className="col-lg-5 auth-side d-none d-lg-block">
        <h5 className="mb-3">Tìm việc làm thêm dễ hơn với SVJob</h5>
        <ul className="list-unstyled mb-4">
          <li><i className="bi bi-check-circle-fill" /><span>Lọc việc theo ngành, khu vực và mức lương theo giờ</span></li>
          <li><i className="bi bi-check-circle-fill" /><span>Nộp đơn bằng CV PDF đã lưu sẵn trong hồ sơ</span></li>
          <li><i className="bi bi-check-circle-fill" /><span>Nhận thông báo khi nhà tuyển dụng duyệt đơn</span></li>
          <li><i className="bi bi-check-circle-fill" /><span>Xem đánh giá thật của người đã làm trước khi ứng tuyển</span></li>
        </ul>
        <div className="quote">
          <div className="mb-2">"Mình tìm được chỗ làm ca tối gần trường chỉ sau 2 ngày, lịch làm khớp với thời khóa biểu."</div>
          <div className="text-muted small">Sinh viên năm 3, Đại học Văn Lang</div>
        </div>
      </div>
    </div>
  );
}
