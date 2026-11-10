import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="sv-footer">
      <div className="container">
        <div className="row g-4">
          <div className="col-md-4">
            <div className="logo mb-2">SV<span>Job</span></div>
            <p className="mb-0">Việc làm thêm cho sinh viên TP.HCM. Tin tuyển dụng được kiểm duyệt, có đánh giá từ người đã làm.</p>
          </div>
          <div className="col-6 col-md-2">
            <h6>Sinh viên</h6>
            <Link to="/">Tìm việc làm</Link>
            <Link to="/student/applications">Đơn đã ứng tuyển</Link>
            <Link to="/student/profile">Hồ sơ & CV</Link>
          </div>
          <div className="col-6 col-md-3">
            <h6>Nhà tuyển dụng</h6>
            <Link to="/employer/jobs/new">Đăng tin tuyển dụng</Link>
            <Link to="/employer/jobs">Quản lý tin đăng</Link>
            <Link to="/register">Tạo tài khoản doanh nghiệp</Link>
          </div>
          <div className="col-md-3">
            <h6>Liên hệ</h6>
            <div className="mb-1"><i className="bi bi-envelope me-1" />hotro@svjob.vn</div>
            <div><i className="bi bi-clock me-1" />Thứ 2 - Thứ 6, 8h00 - 17h00</div>
          </div>
        </div>
        <div className="bottom d-flex flex-wrap justify-content-between gap-2">
          <span>© 2026 SVJob. Đồ án Chuyên đề tốt nghiệp 2, Nhóm 4, Đại học Văn Lang.</span>
          <span>Điều khoản sử dụng · Chính sách bảo mật</span>
        </div>
      </div>
    </footer>
  );
}
